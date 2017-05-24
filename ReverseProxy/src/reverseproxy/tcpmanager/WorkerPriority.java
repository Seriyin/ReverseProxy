/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reverseproxy.tcpmanager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        InetAddress IP = StateManager.getConnectionPriorityMap()
                .stream()
                .min((a, b) -> Integer.compare(
                a.calculatePriority(),
                b.calculatePriority()))
                .get()
                .getServerAddress();
        Socket beSocket;
        try {
            beSocket = new Socket(IP, StateManager.getTCPPort());
            FixedThreadPool.submit(new TCPWorker(RequestsSocket,
                    beSocket,
                    StateManager,
                    true
            ));
            FixedThreadPool.submit(new TCPWorker(RequestsSocket,
                    beSocket,
                    StateManager,
                    false
            ));
        } catch (IOException ex) {
            Logger.getLogger(TCPWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
