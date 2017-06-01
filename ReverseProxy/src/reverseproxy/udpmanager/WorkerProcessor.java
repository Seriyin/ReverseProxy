/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reverseproxy.udpmanager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import reverseproxy.PriorityData;
import reverseproxy.StateManager;

/**
 * WorkerProcessor exclusively polls its queue of packets to negotiate
 * a timeout and update priority data for a single IP address associated
 * with one single server.
 * It will on startup, negotiate a timeout, after which it will interrupt
 * a sleeping prober to start probing.
 * @author Andre, Matias, Nuno
 */
public class WorkerProcessor implements Runnable 
{
    private final DatagramSocket RequestsSocket;
    private final int PollTime;
    private final int port;
    private final InetAddress IP;
    private final ThreadData ThreadData;
    private final ConcurrentLinkedQueue<PriorityData> ConnectionPriorityMap;
    private DatagramPacket CurrentPacket;
    private final PriorityData PriorityData;
    private final ArrayBlockingQueue<DatagramPacket> PacketQueue;
    private final Map<InetAddress,ThreadData> ThreadDataMap;
    private final Map<Integer,Long> timeStampWindow;
    private final int windowSize;
    private int windowCounter;
    private int windowTimeouts;
    
    
    /**
     * Constructs a worker dedicated to reading probe response packets
     * and using them to calculate and update the various parameters
     * involved in the priority calculation.
     * @param RequestsSocket the UDP socket through which to send packets.
     * @param ThreadData this thread and prober's thread handles + backend IP
     * @param StateManager used to retrieve configurable info read from JSON.
     * @param ThreadDataMap the Map of ThreadDatas for each backend IP to allow
     * deletion when a thread times out.
     */
    public WorkerProcessor(DatagramSocket RequestsSocket,
                           ThreadData ThreadData,
                           StateManager StateManager,
                           Map<InetAddress,ThreadData> ThreadDataMap) 
    {
        this.RequestsSocket = RequestsSocket;
        PollTime=StateManager.getPacketTimeout();
        this.ThreadData = ThreadData;
        IP = ThreadData.getAddress();
        PacketQueue = ThreadData.getPacketQueue();
        PriorityData = new PriorityData(ThreadData.getAddress());
        ConnectionPriorityMap = StateManager.getConnectionPriorityMap();
        port = StateManager.getUDPPort();
        windowSize= StateManager.getWindowSize();
        ConnectionPriorityMap.add(PriorityData);
        CurrentPacket = null;
        timeStampWindow = new HashMap<>(windowSize*2);
        windowCounter = 0;
        windowTimeouts = 0;
        this.ThreadDataMap = ThreadDataMap;
    }

    @Override
    public void run() 
    {
        System.out.println("Processor Alive for :" + IP);
        try 
        {
            if(negotiateTimeout())
            {
                System.out.println("Processor negotiated timeout: " + PollTime);
                int count=0;
                //Three timeouts means kill server.
                while(count<3) 
                {
                    while((CurrentPacket=PacketQueue.poll(PollTime, TimeUnit.SECONDS))!=null) 
                    {
                        handlePacket();
                        count=0;
                        System.out.println("Polled: " + Arrays.toString(CurrentPacket.getData()));
                    }
                    if(!ThreadData.isUnderCongestion()) 
                    {
                        count++;
                        windowTimeouts++;
                    }
                    else 
                    {
                        ThreadData.setUnderCongestion(false);
                    }
                    System.out.println("Failed Poll");
                }
                ThreadData.getProberThread().cancel(true);
            }
        }
        catch(InterruptedException e) 
        {
            System.err.println(e.getMessage());
        }
        ThreadDataMap.remove(IP);
    }

    /**
     * Treatment of packet windows here. Accumulate RTTs, which requires
     * timestamping at the entrance of each packet. Counts packet loss
     * and discard expired packets (if they are from previous windows). 
     * Keeps a current window counter, and on a new window, 
     * updates the priority data.
     */
    private void handlePacket()
    {
        long timestamp = System.currentTimeMillis();
        System.out.println("Pacote: " + CurrentPacket.getLength() + " " + Arrays.toString(CurrentPacket.getData()));
        
        if (CurrentPacket.getData()[0] == 2)
        {
            long rTT;
            long packetTimestamp = ByteBuffer.wrap(CurrentPacket.getData()).getLong(1);
            int sequenceNumber = ByteBuffer.wrap(CurrentPacket.getData()).getInt();
            int window = sequenceNumber/windowSize;
            timestamp -= packetTimestamp;
            
            if (window == windowCounter)
            {
                timeStampWindow.put(sequenceNumber,timestamp);
            }
            else if (window > windowCounter)
            {
                int lost = windowSize-timeStampWindow.size();
                long sum=0;
                for (Long time : timeStampWindow.values()) 
                {
                    sum += time^2;
                }
                int mean;
                mean = (int)Math.sqrt(sum/timeStampWindow.size())/1000;
                //update database
                PriorityData.updateWindow(mean, lost, windowTimeouts);
                timeStampWindow.clear();
                this.windowCounter = window;
                timeStampWindow.put(sequenceNumber,timestamp);
                windowTimeouts=0;
            }
        }
    }


    /**
     * Sends a special flag and 4 bytes with the timeout interval in
     * seconds read from the JSON. Tries 3 times, if no ACK(hello packet) 
     * comes back fail and return false.
     * @return whether the timeout was negotiated (true) or forgotten (false)
     */
    private boolean negotiateTimeout() 
    {
        int count=0;
        boolean bTimeoutNegotiated=false;
        while(count<3) 
        {
            ByteBuffer buf=ByteBuffer.allocate(16);
            buf.put((byte)1);
            buf.putInt(PollTime);
            byte bufb[] = buf.array();
            CurrentPacket = new DatagramPacket(bufb, bufb.length, IP ,port);
            try 
            {
                RequestsSocket.send(CurrentPacket);
                try 
                {
                    CurrentPacket=PacketQueue.poll(PollTime, TimeUnit.SECONDS);
                } 
                catch (InterruptedException ex) 
                {
                    Logger.getLogger(WorkerProcessor.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
                }
                //First Packet is an hello packet
                if(CurrentPacket!=null) 
                {
                    count=400;
                    bTimeoutNegotiated=true;
                    ThreadData.getProberThreadHandle().interrupt();
                    handlePacket();
                }
                else
                {
                    count++;
                }
            } 
            catch (IOException ex) 
            {
                Logger.getLogger(WorkerProcessor.class.getName()).log(Level.SEVERE, null, ex);
                count++;
            }
        }
        return bTimeoutNegotiated;
    }
    
    
}
