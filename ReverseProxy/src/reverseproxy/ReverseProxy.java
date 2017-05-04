/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reverseproxy;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import reverseproxy.udpmanager.UDPServer;

/**
 *
 * @author Andre, Matias, Nuno
 */
public class ReverseProxy 
{

    /**
     * For now just run UDPManager for 80 + 20 seconds.
     */
    public static void main(String[] args) 
    {
        int port = 5555;
        StateManager StateManager = new StateManager(port);
        try 
        {
            UDPServer UDPServer = new UDPServer(port,StateManager);
            Thread UDPManager = new Thread(UDPServer);
            UDPManager.start();
            try {
                Thread.sleep(80000);
            } 
            catch (InterruptedException ex) 
            {
                Logger.getLogger(ReverseProxy.class.getName()).log(Level.SEVERE, null, ex);
            }
            try 
            {
                UDPManager.join(20000);
            } 
            catch (InterruptedException ex) 
            {
                Logger.getLogger(ReverseProxy.class.getName()).log(Level.SEVERE, null, ex);
            }
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(ReverseProxy.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}
