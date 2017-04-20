/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reverseproxy.udpmanager;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;
import reverseproxy.PriorityData;
import reverseproxy.StateManager;

/**
 *
 * @author Andre, Matias, Nuno
 */
public class WorkerProcessor implements Runnable 
{
    private final int PollTime;
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
        PriorityData = new PriorityData(ThreadData.getAddress());
        ConnectionPriorityMap = StateManager.getConnectionPriorityMap();
        ConnectionPriorityMap.add(PriorityData);
    }

    @Override
    public void run() 
    {
        try 
        {
            if(negotiateTimeout()) 
            {
                int count=0;
                //Three timeouts means kill server.
                while(count<3) 
                {
                    while((CurrentPacket=ThreadData.getPacketQueue()
                                                   .poll(PollTime, TimeUnit.SECONDS))!=null) 
                    {
                        handlePacket();
                        count=0;
                    }
                    if(!ThreadData.isUnderCongestion()) 
                    {
                        count++;
                    }
                }
            }
        }
        catch(InterruptedException e) 
        {
            System.err.println(e.getMessage());
        }
        killAssociatedServer();
    }

    /**
     * Treat packet windows here. Need to accumulate RTTs, which requires
     * timestamping at the entrance of each packet. Need to count packet loss
     * and discard expired packets (if they are from previous windows). Need
     * to keep a current window counter as well.
     */
    private void handlePacket()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Send a kill request. Probably won't make it. Doesn't really matter if it doesn't.
     * The monitor will timeout and die anyway.
     */
    private void killAssociatedServer() 
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Need to send a special flag and 4 bytes with the timeout interval in
     * milliseconds read from the JSON. Should try 3 times, if no ACK comes back
     * fail and return false.
     * @return whether the timeout was negotiated (true) or forgotten (false)
     */
    private boolean negotiateTimeout() 
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
}
