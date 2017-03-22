/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reverseproxy.udpmanager;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ArrayBlockingQueue;
import reverseproxy.StateManager;

/**
 *
 * @author Andre, Matias, Nuno
 */
public class WorkerProcessor implements Runnable {
    private final DatagramSocket RequestsSocket;
    private final ArrayBlockingQueue<DatagramPacket> PacketQueue;
    private final StateManager StateManager;
    
    public WorkerProcessor(DatagramSocket RequestsSocket, ArrayBlockingQueue<DatagramPacket> PacketQueue, StateManager StateManager) 
    {
        this.StateManager = StateManager;
        this.RequestsSocket = RequestsSocket;
        this.PacketQueue = PacketQueue;
    }

    @Override
    public void run() 
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
