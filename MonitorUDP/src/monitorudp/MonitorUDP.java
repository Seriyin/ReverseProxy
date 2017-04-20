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
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andre, Matias, Nuno
 */
public class MonitorUDP implements Runnable
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
        catch(UnknownHostException e) 
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

    public MonitorUDP(DatagramSocket ServerSocket, DatagramPacket CurrentPacket) 
    {
        this.ServerSocket = ServerSocket;
        this.CurrentPacket = CurrentPacket;
    }
    
    @Override
    public void run() 
    {
        try 
        {
            while(true) 
            {
                constructPacketHello();
                try 
                {
                    ServerSocket.send(CurrentPacket);
                } 
                catch (IOException ex) 
                {
                    Logger.getLogger(MonitorUDP.class.getName()).log(Level.SEVERE, null, ex);
                }
                Thread.sleep(2000);
            }
        }
        catch(InterruptedException e) {}
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
        catch(IOException | InterruptedException e) 
        {
            System.err.println(e.getMessage());
        }
    }
    
    private void runMonitor() throws IOException, InterruptedException
    {
        constructPacketHello();
        ServerSocket.send(CurrentPacket);
        ServerSocket.setSoTimeout(15000);
        try
        {
            ServerSocket.receive(CurrentPacket);
        }
        catch(IOException e)
        {
            System.err.println(e.getMessage());
        }
        int timeout=getTimeoutPacket();
        if (timeout!=-1) 
        {
            ServerSocket.setSoTimeout(timeout);
            boolean bPacketReceiveFail=false;
            Thread hello=new Thread(new MonitorUDP(ServerSocket,CurrentPacket));
            int count=0;
            while(count<3) 
            {
                try
                {
                    ServerSocket.receive(CurrentPacket);
                }
                catch(IOException e)
                {
                    bPacketReceiveFail = true;
                }
                if (!bPacketReceiveFail) 
                {
                    constructPacketResponse();
                }
                else 
                {
                    count++;
                }
            }
            ServerSocket.send(CurrentPacket);
            hello.interrupt();
            hello.join(15000);
        }
        else 
        {
            throw new RuntimeException();
        }
    }

    
    /**
     * Have to set the byte array to a single hello byte.
     */
    private void constructPacketHello() 
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Read from the current packet the timeout if its there, otherwise
     * @return 
     */
    private int getTimeoutPacket() 
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Build a packet byte array with the new integer for the count and appropriate
     * flag.
     */
    private void constructPacketResponse() 
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
