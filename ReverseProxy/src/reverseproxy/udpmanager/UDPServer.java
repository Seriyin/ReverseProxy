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
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import reverseproxy.StateManager;

/**
 *
 * @author Andre, Matias, Nuno
 */
public class UDPServer implements Runnable {
    private final DatagramSocket ServerSocket;
    private final DatagramPacket CurrentPacket;
    private final Map<InetAddress,ArrayBlockingQueue<DatagramPacket>> QueueMap;
    private final Map<InetAddress,Boolean> CongestionInQueueMap;
    private final Map<InetAddress,Future<?>> ThreadsActiveHandlersMap;
    private final WorkerFactory SocketWorkerFactory;
    private final StateManager StateManager;
    
    public UDPServer(int port,StateManager StateManager) throws IOException 
    {
        ServerSocket=new DatagramSocket(port);
        CurrentPacket = new DatagramPacket(new byte[40],40);
        QueueMap = new HashMap<>(30);
        CongestionInQueueMap = new HashMap<>(30);
        SocketWorkerFactory = new WorkerFactory();
        ThreadsActiveHandlersMap = new HashMap<>(30);
        this.StateManager = StateManager;
    }

    
    
    /**
     * Asks the worker factory to build a socket worker.
     */    
    private void buildSocketWorkerForIP() 
    {
        ArrayBlockingQueue<DatagramPacket> q = new ArrayBlockingQueue<>(50);
        q.add(new DatagramPacket(CurrentPacket.getData().clone(),
                                 CurrentPacket.getLength(), 
                                 CurrentPacket.getAddress(),
                                 CurrentPacket.getPort()));
        
        QueueMap.put(CurrentPacket.getAddress(), q);
        ThreadsActiveHandlersMap
                .put(CurrentPacket.getAddress(), 
                     SocketWorkerFactory.buildSocketWorker
                            (
                            QueueMap.get(CurrentPacket.getAddress()),
                            ServerSocket,
                            StateManager
                            )
                    );
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
            if(QueueMap.containsKey(CurrentPacket.getAddress()))
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
        
    }
    
    
}
