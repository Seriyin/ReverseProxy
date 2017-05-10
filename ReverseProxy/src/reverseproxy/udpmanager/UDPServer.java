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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import reverseproxy.StateManager;

/**
 *
 * @author Andre, Matias, Nuno
 */
public class UDPServer implements Runnable 
{
    private final DatagramSocket ServerSocket;
    private final DatagramPacket CurrentPacket;
    private final Map<InetAddress,ThreadData> ThreadDataMap;
    private final WorkerFactory SocketWorkerFactory;
    private final StateManager StateManager;
    
    public UDPServer(int port,StateManager StateManager) throws IOException 
    {
        ServerSocket=new DatagramSocket(port);
        CurrentPacket = new DatagramPacket(new byte[40],40);
        ThreadDataMap = new HashMap<>(30);
        SocketWorkerFactory = new WorkerFactory(StateManager);
        this.StateManager = StateManager;
    }

    
    
    /**
     * Asks the worker factory to build a socket worker.
     */    
    private void buildSocketWorkerForIP() 
    {
        InetAddress addr=CurrentPacket.getAddress();
        ArrayBlockingQueue<DatagramPacket> q = new ArrayBlockingQueue<>(50);
        //First Packet is always an hello, drop it
        ThreadData t=new ThreadData(q,false,addr);
        t.registerProcessorThread(SocketWorkerFactory.buildSocketWorker
                                                   (t,ServerSocket,StateManager));
        t.registerProberThread(SocketWorkerFactory.buildSocketProber
                                                   (t,ServerSocket,StateManager));
        ThreadDataMap.put(CurrentPacket.getAddress(),t);
    }

    @Override
    public void run() 
    {
        try 
        {
            ServerSocket.setSoTimeout(4000);
        } 
        catch (SocketException ex) 
        {
            Logger.getLogger(WorkerProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Begin listen on " + StateManager.getPort());
        while(true) 
        {
            try 
            {
                ServerSocket.receive(CurrentPacket);
                System.out.println("Received Packet");
                if(ThreadDataMap.containsKey(CurrentPacket.getAddress()))
                {
                    System.out.println("Handling Packet");
                    handleUDPPacket();
                }
                else
                {
                    buildSocketWorkerForIP();
                }
            }
            catch(SocketException ex) {}
            catch (IOException ex) 
            {
                //Should handle carefully packet corruption
                Logger.getLogger(UDPServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void handleUDPPacket() 
    {
        InetAddress IP= CurrentPacket.getAddress();
        DatagramPacket clone=new DatagramPacket
                                (CurrentPacket.getData().clone(),
                                 CurrentPacket.getLength(), 
                                 IP,
                                 CurrentPacket.getPort()
                                );
        System.out.println("Cloned Packet: " + clone.getAddress());
        ThreadData currentT = ThreadDataMap.get(IP);
        ArrayBlockingQueue<DatagramPacket> pq = currentT.getPacketQueue();
        try
        {
            pq.add(clone);
            System.out.println("Top of Queue:" + currentT.getAddress() + " & " + pq.peek() );
        }
        //Failed to send to queue, must be congestion, wipe the whole queue
        //And keep going.
        catch(IllegalStateException e) 
        {
            Logger.getLogger(UDPServer.class.getName()).log(Level.SEVERE, null, e);
            currentT.setUnderCongestion(true);
            pq.clear();      
        }
    }
    
    
}
