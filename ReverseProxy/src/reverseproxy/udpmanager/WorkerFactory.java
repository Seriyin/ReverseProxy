/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reverseproxy.udpmanager;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import reverseproxy.StateManager;

/**
 * A worker factory contains a fixed thread pool
 * to run a fixed maximum new WorkerThreads (N) for dedicated processing
 * for a given number of servers N.
 * Returns a future for thread interrupting
 *
 * Future.cancel(true) launches a thread interrupt.
 *
 * @author Andre, Matias, Nuno
 */
public class WorkerFactory 
{
    private final ExecutorService FixedThreadPool;
    
    public WorkerFactory(StateManager StateManager)
    {
        FixedThreadPool = Executors.newFixedThreadPool(StateManager.getMaxServerConnections());
    }
    
    public Future<?> buildSocketWorker(ThreadData ThreadData,
                                       DatagramSocket RequestsSocket,
                                       StateManager StateManager,
                                       Map<InetAddress,ThreadData> ThreadDataMap) 
    {
        return FixedThreadPool.submit(new WorkerProcessor(RequestsSocket,
                                                          ThreadData,
                                                          StateManager,
                                                          ThreadDataMap));
    }
    
    public Future<?> buildSocketProber(ThreadData ThreadData,
                                       DatagramSocket RequestsSocket,
                                       StateManager StateManager)
    {
        return FixedThreadPool.submit(new WorkerProber(ThreadData,
                                                       RequestsSocket,
                                                       StateManager));
    }
    
    
}
