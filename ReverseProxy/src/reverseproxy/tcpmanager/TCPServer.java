/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reverseproxy.tcpmanager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import reverseproxy.StateManager;
import reverseproxy.udpmanager.UDPServer;
import reverseproxy.udpmanager.WorkerProcessor;

/**
 *
 * @author Andre
 */
public class TCPServer implements Runnable
{
    private final ServerSocket ServerSocket;
    private final WorkerFactory SocketWorkerFactory;
    private final StateManager StateManager;
    
    public TCPServer(StateManager StateManager) throws IOException 
    {
        ServerSocket=new ServerSocket(StateManager.getTCPPort());
        SocketWorkerFactory = new WorkerFactory(StateManager);
        this.StateManager=StateManager;
    }



    @Override
    public void run() 
    {
        System.out.println("TCP begin listen on " + StateManager.getTCPPort());
        while(true) 
        {
            try 
            {
                Socket s=ServerSocket.accept();
                SocketWorkerFactory.buildSocketWorker(s, StateManager);
            }
            catch (IOException ex) 
            {
                //Should handle carefully packet corruption
                Logger.getLogger(UDPServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
