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
import reverseproxy.StateManager;

/**
 *
 * @author Andre
 */
public class WorkerProber implements Runnable 
{
    private final DatagramSocket UDPSocket;
    private final DatagramPacket ProbePacket;
    private final ByteBuffer bb;
    private final InetAddress ServerIP;
    private final ThreadData ThreadData;

    public WorkerProber(ThreadData ThreadData, DatagramSocket DatagramSocket,
                        StateManager StateManager) 
    {
        this.ThreadData = ThreadData;
        ServerIP = ThreadData.getAddress();
        UDPSocket = DatagramSocket;
        bb = ByteBuffer.allocate(9).put((byte)2);
        ProbePacket = new DatagramPacket(bb.array(),
                                         bb.capacity(),
                                         ServerIP,
                                         StateManager.getUDPPort());
    }

    @Override
    public void run() 
    {
        ThreadData.registerProberThreadHandle(Thread.currentThread());
        boolean bWoken=false;
        try 
        {
            //Sleep during negotiation
            Thread.sleep(30000);
        } 
        catch (InterruptedException ex) 
        {
            bWoken=true;
        }
        if (bWoken) 
        {
            try
            {
                while(true) 
                {
                    Thread.sleep(2000);
                    try 
                    {
                        long timestamp = System.currentTimeMillis();
                        System.out.println("Stamp for " + ServerIP + " : " + timestamp);
                        bb.putLong(1, timestamp);
                        UDPSocket.send(ProbePacket);
                    }  
                    catch (IOException ex) 
                    {
                        System.err.println(ex.getMessage());
                    }
                }
            }
            catch(InterruptedException e) 
            {
                System.out.println("Prober for " + ServerIP +  " exiting");
            }
        }
    }
    
}
