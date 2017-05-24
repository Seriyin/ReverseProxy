/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reverseproxy.tcpmanager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
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
    private final InetAddress beServer;
    private final int backEndTCPport;
    private final boolean direction;

    public TCPWorker(Socket RequestsSocket, StateManager StateManager, boolean direc, InetAddress beServer ) {
        this.RequestsSocket = RequestsSocket;
        this.beServer = beServer;
        this.backEndTCPport = StateManager.getTCPPort();
        this.direction = direc;
    }

    @Override
    public void run() {
        byte buffer[] = new byte[1024];
        Socket beSocket = null;
        OutputStream Outputstream;
        InputStream Inputstream;
        if (direction == true) {
            try {
                beSocket = new Socket(beServer, backEndTCPport);
            } catch (IOException ex) {
                Logger.getLogger(TCPWorker.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (beSocket != null) {
                try {
                    Outputstream = beSocket.getOutputStream();
                    Inputstream = RequestsSocket.getInputStream();
                    while (Inputstream.read(buffer) != -1) {
                        Outputstream.write(buffer);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(TCPWorker.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    beSocket.close();
                } catch (IOException ex) {
                    Logger.getLogger(TCPWorker.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            try {
                RequestsSocket.close();
            } catch (IOException ex) {
                Logger.getLogger(TCPWorker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else{
            try {
                beSocket = new Socket(beServer, backEndTCPport);
            } catch (IOException ex) {
                Logger.getLogger(TCPWorker.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (beSocket != null) {
                try {
                    Outputstream = RequestsSocket.getOutputStream();
                    Inputstream = beSocket.getInputStream();
                    while (Inputstream.read(buffer) != -1) {
                        Outputstream.write(buffer);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(TCPWorker.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    beSocket.close();
                } catch (IOException ex) {
                    Logger.getLogger(TCPWorker.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            try {
                RequestsSocket.close();
            } catch (IOException ex) {
                Logger.getLogger(TCPWorker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

}
