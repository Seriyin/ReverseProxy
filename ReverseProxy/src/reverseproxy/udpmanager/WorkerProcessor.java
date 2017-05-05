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
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import reverseproxy.PriorityData;
import reverseproxy.StateManager;

/**
 *
 * @author Andre, Matias, Nuno
 */
public class WorkerProcessor implements Runnable 
{
    private final int PollTime;
    private final int port;
    private final InetAddress IP;
    private final DatagramSocket RequestsSocket;
    private final ThreadData ThreadData;
    private final ConcurrentSkipListSet ConnectionPriorityMap;
    private DatagramPacket CurrentPacket;
    private final PriorityData PriorityData;
    
    public WorkerProcessor(DatagramSocket RequestsSocket, 
                           ThreadData ThreadData,
                           StateManager StateManager) 
    {
        PollTime=StateManager.getPacketTimeout();
        this.RequestsSocket = RequestsSocket;
        this.ThreadData = ThreadData;
        IP = ThreadData.getAddress();
        PriorityData = new PriorityData(ThreadData.getAddress());
        ConnectionPriorityMap = StateManager.getConnectionPriorityMap();
        port = StateManager.getPort();
        ConnectionPriorityMap.add(PriorityData);
        CurrentPacket = null;
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
                ArrayBlockingQueue<DatagramPacket> PacketQueue= ThreadData.getPacketQueue();
                int count=0;
                //Three timeouts means kill server.
                while(count<3) 
                {
                    CurrentPacket=PacketQueue.poll(PollTime, TimeUnit.SECONDS);
                    System.out.println("Polled: " + CurrentPacket);
                    while(CurrentPacket!=null) 
                    {
                        handlePacket();
                        count=0;
                        CurrentPacket=PacketQueue.poll(PollTime, TimeUnit.SECONDS);
                        System.out.println("Polled: " + CurrentPacket);
                    }
                    if(!ThreadData.isUnderCongestion()) 
                    {
                        count++;
                    }
                    System.out.println("Failed Poll");
                }
            }
        }
        catch(InterruptedException e) 
        {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Treat packet windows here. Need to accumulate RTTs, which requires
     * timestamping at the entrance of each packet. Need to count packet loss
     * and discard expired packets (if they are from previous windows). Need
     * to keep a current window counter as well.
     * If it's still a new thread, check the packet window for rogue Server.
     * If it finds a rogue Server politely ask it to reset 3 times.
     */
    private void handlePacket()
    {        
//        long timestamp = System.currentTimeMillis();
        System.out.println("Pacote: " + CurrentPacket.getLength() + " " + Arrays.toString(CurrentPacket.getData()));
    }


    /**
     * Need to send a special flag and 4 bytes with the timeout interval in
     * milliseconds read from the JSON. Should try 3 times, if no ACK comes back
     * fail and return false.
     * @return whether the timeout was negotiated (true) or forgotten (false)
     */
    private boolean negotiateTimeout() 
    {
        ByteBuffer buf=ByteBuffer.allocate(5);
        buf.put((byte)1);
        buf.putInt(PollTime);
        byte bufb[] = buf.array();
        CurrentPacket = new DatagramPacket(bufb, bufb.length, IP ,port);
        CurrentPacket.setAddress(ThreadData.getAddress());
        try 
        {
            RequestsSocket.send(CurrentPacket);
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(WorkerProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
    
    
}
