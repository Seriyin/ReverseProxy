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
import java.util.ArrayList;
import java.util.Arrays;
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
 * @author Andre, Matias, Nuno
 */
public class WorkerProcessor implements Runnable 
{
    private final int PollTime;
    private final int port;
    private final InetAddress IP;
    private final DatagramSocket RequestsSocket;
    private final ThreadData ThreadData;
    private final ConcurrentLinkedQueue<PriorityData> ConnectionPriorityMap;
    private DatagramPacket CurrentPacket;
    private final PriorityData PriorityData;
    private final ArrayBlockingQueue<DatagramPacket> PacketQueue;
    private final Map<InetAddress,ThreadData> ThreadDataMap;
    private final ArrayList<Long> timeStampWindow;
    private int windowSize;
    private int windowCounter;
    private int windowTimeouts;
    
    public WorkerProcessor(DatagramSocket RequestsSocket, 
                           ThreadData ThreadData,
                           StateManager StateManager,
                           Map<InetAddress,ThreadData> ThreadDataMap) 
    {
        PollTime=StateManager.getPacketTimeout();
        this.RequestsSocket = RequestsSocket;
        this.ThreadData = ThreadData;
        IP = ThreadData.getAddress();
        PacketQueue = ThreadData.getPacketQueue();
        PriorityData = new PriorityData(ThreadData.getAddress());
        ConnectionPriorityMap = StateManager.getConnectionPriorityMap();
        port = StateManager.getUDPPort();
        windowSize= StateManager.getWindowSize();
        ConnectionPriorityMap.add(PriorityData);
        CurrentPacket = null;
        timeStampWindow = new ArrayList<>();
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
     * Treat packet windows here. Need to accumulate RTTs, which requires
     * timestamping at the entrance of each packet. Need to count packet loss
     * and discard expired packets (if they are from previous windows). Need
     * to keep a current window counter as well.
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
                timeStampWindow.add(timestamp);
            }
            else if (window > windowCounter)
            {
                int lost = windowSize-timeStampWindow.size();
                long sum=0;
                for (int i=0; i< timeStampWindow.size(); i++) {
                    sum += (timeStampWindow.get(i))^2;
                }
                int mean;
                mean = (int)Math.sqrt(sum/timeStampWindow.size())/1000;
                //update database
                PriorityData.updateWindow(mean, lost, windowTimeouts);
                timeStampWindow.clear();
                this.windowCounter = window;
                timeStampWindow.add(timestamp);
                windowTimeouts=0;
            }
        }
    }


    /**
     * Sends a special flag and 4 bytes with the timeout interval in
     * seconds read from the JSON. Should try 3 times, if no ACK comes back
     * fail and return false.
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
