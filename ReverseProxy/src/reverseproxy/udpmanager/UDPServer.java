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
        CurrentPacket = new DatagramPacket(new byte[5],5);
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
        q.add(new DatagramPacket(CurrentPacket.getData().clone(),
                                 CurrentPacket.getLength(), 
                                 addr,
                                 CurrentPacket.getPort()));
        ThreadData t=new ThreadData(q,false,addr);
        t.registerProcessorThreadHandle(SocketWorkerFactory.buildSocketWorker
                                                   (t,ServerSocket,StateManager));
        t.registerProberThreadHandle(SocketWorkerFactory.buildSocketProber
                                                   (t,ServerSocket,StateManager));
        ThreadDataMap.put(CurrentPacket.getAddress(),t);
    }

    @Override
    public void run() 
    {
        while(true) 
        {
            try 
            {
                ServerSocket.receive(CurrentPacket);
            } 
            catch (IOException ex) 
            {
                //Should handle carefully packet corruption
                Logger.getLogger(UDPServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            if(ThreadDataMap.containsKey(CurrentPacket.getAddress()))
            {
                handleUDPPacket();
            }
            else
            {
                buildSocketWorkerForIP();
            }
        }
    }

    private void handleUDPPacket() 
    {
        DatagramPacket clone=new DatagramPacket
                                (CurrentPacket.getData().clone(),
                                 CurrentPacket.getLength(), 
                                 CurrentPacket.getAddress(),
                                 CurrentPacket.getPort()
                                );
        ThreadData currentT = ThreadDataMap.get(CurrentPacket.getAddress());
        ArrayBlockingQueue<DatagramPacket> pq = currentT.getPacketQueue();
        boolean bPostedSucessfully=pq.add(clone);
        //Failed to send to queue, must be congestion, wipe the whole queue
        //And keep going.
        if(!bPostedSucessfully) 
        {
            currentT.setUnderCongestion(true);
            pq.clear();
        }
    }
    
    
}
