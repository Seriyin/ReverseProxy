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
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andre, Matias, Nuno
 */
public class MonitorUDP implements Runnable
{
    private final DatagramSocket ServerSocket;
    private DatagramPacket CurrentPacket;
    private InetAddress IP;
    private final int port;
    private int sequence;


    public MonitorUDP(int port,String address) throws IOException 
    {
        ServerSocket=new DatagramSocket(port);
        this.port=port;
        try 
        {
            IP = InetAddress.getByName(address);
        }
        catch(UnknownHostException e) 
        {
            IP=null;
            System.err.println(e.getMessage());
        }
        
        if(IP!=null) 
        {
            CurrentPacket = new DatagramPacket(new byte[20],20,IP,port);
        }
        else 
        {
            CurrentPacket=null;
        }
        System.out.println("IP :" + IP);
    }

    public MonitorUDP(DatagramSocket ServerSocket, int port, InetAddress IP) 
    {
        this.ServerSocket = ServerSocket;
        this.port=port;
        this.IP=IP;
    }
    
    @Override
    public void run() 
    {
        try 
        {
            CurrentPacket = new DatagramPacket(new byte[5],5,IP,port);
            constructPacketHello();
            while(true) 
            {
                try 
                {
                    System.out.println("Current : " + CurrentPacket.getAddress());
                    ServerSocket.send(CurrentPacket);
                } 
                catch (IOException ex) 
                {
                    Logger.getLogger(MonitorUDP.class.getName()).log(Level.SEVERE, null, ex);
                }
                Thread.sleep(4000);
            }
        }
        catch(InterruptedException e) {}
    }

    
    
    /**
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        try
        {
            MonitorUDP Monitor = new MonitorUDP(5555,args[0]);
            
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
        System.out.println("Sending First hello");
        ServerSocket.send(CurrentPacket);
        ServerSocket.setSoTimeout(15000);
        try
        {
            System.out.println("Receiveing Timeout Packet");
            ServerSocket.receive(CurrentPacket);
        }
        catch(IOException e)
        {
            System.err.println(e.getMessage());
        }
        int timeout=getTimeoutPacket();
        if (timeout!=-1) 
        {
            sequence=0;
            ServerSocket.setSoTimeout(timeout);
            boolean bPacketReceiveFail=false;
            Thread hello=new Thread(new MonitorUDP(ServerSocket,port,IP));
            hello.start();
            int count=0;
            while(count<3) 
            {
                try
                {
                    System.out.println("Getting Packet - Timeout: " + timeout);
                    ServerSocket.receive(CurrentPacket);
                }
                catch(IOException e)
                {
                    System.err.println(e.getMessage());
                    bPacketReceiveFail = true;
                }
                if (!bPacketReceiveFail && CurrentPacket.getData()[0]==2) 
                {
                    constructPacketResponse();
                    ServerSocket.send(CurrentPacket);
                }
                else 
                {
                    count++;
                }
            }
            hello.interrupt();
            hello.join(15000);
        }
        else 
        {
            throw new RuntimeException("Failure receiving timeout");
        }
    }

    
    /**
     * Have to set the byte array to a single hello byte.
     */
    private void constructPacketHello() 
    {
        ByteBuffer.wrap(CurrentPacket.getData()).put(0,(byte)0);
        CurrentPacket.setAddress(IP);
        System.out.println("Packet :" + Arrays.toString(CurrentPacket.getData()));
    }

    /**
     * Read from the current packet the timeout if its there, otherwise
     * @return timeout time or -1 if no timeout read
     */
    private int getTimeoutPacket() 
    {
        int count=0;
        while(count<3) 
        {
            try 
            {
                ServerSocket.receive(CurrentPacket);
            }
            catch (IOException ex) 
            {
                Logger.getLogger(MonitorUDP.class.getName()).log(Level.SEVERE, null, ex);
            }
            if(CurrentPacket.getData()[0]!=(byte)1) 
            {
                System.out.println("Got THIS : " + Arrays.toString(CurrentPacket.getData()));
                count++;
            }
            else
            {
                count=400;
            }
        }
        if(count==400) 
        {
            ByteBuffer buf = ByteBuffer.wrap(CurrentPacket.getData());
            return buf.getInt(1)*1000;
        }
        else 
        {
            return -1;
        }
    }

    /**
     * Build a packet byte array with the new integer for the count and appropriate
     * flag.
     */
    private void constructPacketResponse() 
    {
        sequence++;
        ByteBuffer buf = 
                ByteBuffer.wrap(CurrentPacket.getData())
                          .put(0,(byte)3)
                          .putInt(8,sequence);
        long timestamp = buf.getLong(1);
        System.out.println("Stamp : " + timestamp);
        CurrentPacket.setAddress(IP);
        CurrentPacket.setData(buf.array());
        System.out.println("Current :" + CurrentPacket.getAddress());
    }
}
