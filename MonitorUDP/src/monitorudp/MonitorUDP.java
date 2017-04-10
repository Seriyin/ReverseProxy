/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monitorudp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 *
 * @author Andre, Matias, Nuno
 */
public class MonitorUDP 
{
    private final DatagramSocket ServerSocket;
    private final DatagramPacket CurrentPacket;


    public MonitorUDP(int port,String address) throws IOException 
    {
        ServerSocket=new DatagramSocket(port);
        InetAddress IP;
        try 
        {
            IP = InetAddress.getByName(address);
        }
        catch(Exception e) 
        {
            IP=null;
            System.err.println(e.getMessage());
        }
        if(IP==null) 
        {
            CurrentPacket = new DatagramPacket(new byte[40],40,IP,port);
        }
        else 
        {
            CurrentPacket=null;
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        try
        {
            MonitorUDP Monitor = new MonitorUDP(5555,args[1]);
            Monitor.runMonitor();
        }
        catch(IOException e) 
        {
            System.err.println(e.getMessage());
        }
    }
    
    private void runMonitor() throws IOException
    {
        while(true) 
        {
            constructPacket();
            ServerSocket.send(CurrentPacket);
        }
    }

    private void constructPacket() 
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
