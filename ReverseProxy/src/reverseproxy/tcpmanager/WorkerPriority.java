/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reverseproxy.tcpmanager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import reverseproxy.PriorityData;
import reverseproxy.StateManager;

/**
 *
 * @author Andre
 */
public class WorkerPriority implements Runnable {

    private final StateManager StateManager;
    private final Socket RequestsSocket;
    private final ExecutorService FixedThreadPool;

    public WorkerPriority(StateManager StateManager, Socket RequestsSocket, ExecutorService ftp) {
        this.StateManager = StateManager;
        this.RequestsSocket = RequestsSocket;
        this.FixedThreadPool = ftp;
    }

    @Override
    public void run() {
        PriorityData p = StateManager.getConnectionPriorityMap()
                .stream()
                .min((a, b) -> Integer.compare(
                a.calculatePriority(),
                b.calculatePriority()))
                .get();
        InetAddress IP = p.getServerAddress();
        Socket beSocket;
        try {
            beSocket = new Socket(IP, StateManager.getTCPPort());
            Future<?> f2 = FixedThreadPool.submit(new TCPWorker(RequestsSocket,
                    beSocket,
                    StateManager,
                    true
            ));
            Future<?> f1 = FixedThreadPool.submit(new TCPWorker(RequestsSocket,
                    beSocket,
                    StateManager,
                    false
            ));
            p.incActiveConnections();
            try {
                f1.get();
                f2.get();
            } 
            catch (Exception ex) 
            {
                Logger.getLogger(WorkerPriority.class.getName()).log(Level.SEVERE, null, ex);
            }
            p.decActiveConnections();
        } catch (IOException ex) {
            Logger.getLogger(TCPWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
