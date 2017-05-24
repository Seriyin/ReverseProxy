/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reverseproxy.tcpmanager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import reverseproxy.PriorityData;
import reverseproxy.StateManager;

/**
 *
 * @author Andre, Nuno, Matias
 */
public class TCPWorker implements Runnable {

    private final Socket RequestsSocket;
    private final ConcurrentLinkedQueue<PriorityData> ConnectionPriorityMap;
    private final int backEndTCPport;

    public TCPWorker(Socket RequestsSocket, StateManager StateManager) {
        this.RequestsSocket = RequestsSocket;
        this.ConnectionPriorityMap = StateManager.getConnectionPriorityMap();
        this.backEndTCPport = StateManager.getTCPPort();
    }

    @Override
    public void run() {
        byte buffer[] = new byte[1024];
        Socket beSocket = null;
        OutputStream beOutputstream;
        InputStream reqInputstream;
        PriorityData beServer
                = ConnectionPriorityMap
                        .stream()
                        .min(((s1, s2) -> Integer
                        .compare(s1.calculatePriority(), s2.calculatePriority())))
                        .get();
        try {
            beSocket = new Socket(beServer.getServerAddress(), backEndTCPport);
        } catch (IOException ex) {
            Logger.getLogger(TCPWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (beSocket != null) {
            try {
                beOutputstream = beSocket.getOutputStream();
                reqInputstream = RequestsSocket.getInputStream();
                while (reqInputstream.read(buffer) != -1) {
                    beOutputstream.write(buffer);
                }
            } catch (IOException ex) {
                Logger.getLogger(TCPWorker.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                RequestsSocket.close();
            } catch (IOException ex) {
                Logger.getLogger(TCPWorker.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                beSocket.close();
            } catch (IOException ex) {
                Logger.getLogger(TCPWorker.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                RequestsSocket.close();
            } catch (IOException ex) {
                Logger.getLogger(TCPWorker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

}
