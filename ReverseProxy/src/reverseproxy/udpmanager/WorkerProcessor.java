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
    private final DatagramSocket RequestsSocket;
    private final ThreadData ThreadData;
    private final ConcurrentSkipListSet ConnectionPriorityMap;
    private DatagramPacket CurrentPacket;
    private final PriorityData PriorityData;
    
    public WorkerProcessor(DatagramSocket RequestsSocket, 
                           ThreadData ThreadData,
                           StateManager StateManager) 
    {
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
            int count=0;
            //Three timeouts means kill server.
            while(count<3) 
            {
                while((CurrentPacket=ThreadData.getPacketQueue().poll(5, TimeUnit.SECONDS))!=null) 
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
        catch(InterruptedException e) 
        {
        }
        killAssociatedServer();
    }

    private void handlePacket() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void killAssociatedServer() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
