/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reverseproxy.tcpmanager;

import java.net.DatagramSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import reverseproxy.StateManager;
import reverseproxy.udpmanager.ThreadData;

/**
 * A worker factory contains a fixed thread pool
 * to run a fixed maximum new WorkerThreads(N) 
 * for dedicated processing for a given number of servers N.
 * Returns a future for thread interrupting.
 *
 * Future.cancel(true) launches a single thread interrupt.
 *
 * @author Andre, Matias, Nuno
 */
public class WorkerFactory 
{
    private final ExecutorService FixedThreadPool;
    
    /**
     * Constructor constructs a fixed ThreadPool with the maximum TCP connections
     * defined in the config.json
     * @param StateManager contains all the read json parameters.
     */
    public WorkerFactory(StateManager StateManager)
    {
        FixedThreadPool = Executors.newFixedThreadPool(StateManager.getMaxTCPConnections());
    }
    
    /**
     * Builds a TCP worker thread for a host TCP connection.
     * @param RequestsSocket the TCP socket for the host.
     * @param StateManager contains the priority data table.
     * @return 
     */
    public Future<?> buildSocketWorker(Socket RequestsSocket,
                                       StateManager StateManager) 
    {
        return FixedThreadPool.submit(new TCPWorker(RequestsSocket,
                                                    StateManager));
    }    

}
