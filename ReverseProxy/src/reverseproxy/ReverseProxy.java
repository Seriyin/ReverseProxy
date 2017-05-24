/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reverseproxy;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import reverseproxy.tcpmanager.TCPServer;
import reverseproxy.udpmanager.UDPServer;

/**
 * The main class which runs the UDPServer and TCPServer.
 * @author Andre, Matias, Nuno
 */
public class ReverseProxy 
{

    /**
     * Runs UDPServer and TCPServer.
     */
    public static void main(String[] args) 
    {
        int UDPPort = 5555;
        int TCPPort = 80;
        StateManager StateManager = new StateManager(UDPPort,TCPPort);
        UDPServer UDPServer;
        TCPServer TCPServer;
        try 
        {
            UDPServer = new UDPServer(StateManager);
            Thread UDPManager = new Thread(UDPServer);
            UDPManager.start();
            TCPServer = new TCPServer(StateManager);
            TCPServer.run();
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(ReverseProxy.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
