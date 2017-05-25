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
import java.util.Arrays;
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
    private final Socket beSocket;
    private final int backEndTCPport;
    private final boolean direction;

    public TCPWorker(Socket RequestsSocket, Socket beSocket ,StateManager StateManager, boolean direc ) {
        this.RequestsSocket = RequestsSocket;
        this.beSocket = beSocket;
        this.backEndTCPport = StateManager.getTCPPort();
        this.direction = direc;
    }

    @Override
    public void run() {
        byte buffer[] = new byte[1024];
        OutputStream Outputstream;
        InputStream Inputstream;
        if (direction == true) {
            if (beSocket != null) {
                try {
                    Outputstream = beSocket.getOutputStream();
                    Inputstream = RequestsSocket.getInputStream();
                    while (Inputstream.read(buffer) != -1) {
                        System.out.println("TCP get - " + Arrays.toString(buffer));
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
            if (beSocket != null) {
                try {
                    Outputstream = RequestsSocket.getOutputStream();
                    Inputstream = beSocket.getInputStream();
                    while (Inputstream.read(buffer) != -1) {
                        System.out.println("TCP write - " + Arrays.toString(buffer));
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
