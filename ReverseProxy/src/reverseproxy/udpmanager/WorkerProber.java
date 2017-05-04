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
import reverseproxy.StateManager;

/**
 *
 * @author Andre
 */
public class WorkerProber implements Runnable 
{
    private final DatagramSocket UDPSocket;
    private final DatagramPacket ProbePacket;

    public WorkerProber(ThreadData ThreadData, DatagramSocket DatagramSocket,
                        StateManager StateManager) 
    {
        InetAddress ServerIP = ThreadData.getAddress();
        UDPSocket = DatagramSocket;
        byte[] probe = new byte[1];
        probe[0] = 2;
        ProbePacket = new DatagramPacket(probe,1,ServerIP,StateManager.getPort());
    }

    @Override
    public void run() 
    {
        try
        {
            while(true) 
            {
                Thread.sleep(2000);
                try 
                {
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
        }
    }
    
}
