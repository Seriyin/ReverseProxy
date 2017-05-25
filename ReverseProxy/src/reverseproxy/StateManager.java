/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reverseproxy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.json.Json;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 * StateManager contains all globally(both to UDP and TCP managers) accessible
 * variables, which include the UDP and TCP ports, all the available JSON 
 * configurable parameters, read from config.json and a PriorityData Table.
 * 
 * The available configurable parameters are:
 *  MaxServerConnections - the maximum allowed backend servers.
 *  MaxTCPConnections - the maximum allowed host TCP connections.
 *  WindowSize - size of the packet window to consider till a priority update.
 *  PacketTimeout - timeout time for a packet in seconds.
 * @author Andre, Matias, Nuno
 */
public final class StateManager 
{
    private final int UDPPort;
    private final int TCPPort;
    private int MaxServerConnections;
    private int MaxTCPConnections;
    private int WindowSize;
    private int PacketTimeout;
    private final ConcurrentLinkedQueue<PriorityData> ConnectionPriorityMap;
    
    public StateManager(int UDPPort,int TCPPort) 
    {
        JsonReader jsr;
        try 
        {
            //
            jsr = Json.createReader(
                        new BufferedReader(
                            new InputStreamReader(
                                 new FileInputStream(
                                        new File("config.json")))));
            JsonObject jso;
            jso=jsr.readObject();
            JsonNumber jsn = jso.getJsonNumber("MaxServerConnections");
            if(jsn != null) 
            {
                MaxServerConnections = jsn.intValueExact();
            }
            else 
            {
                MaxServerConnections = 128;
            }
            jsn = jso.getJsonNumber("WindowSize");
            if(jsn != null)
            {    
                WindowSize = jsn.intValueExact();
            }
            else
            {
                WindowSize=30;
            }
            jsn = jso.getJsonNumber("PacketTimeout");
            if(jsn != null)
            {    
                PacketTimeout = jsn.intValueExact();
            }
            else
            {
                PacketTimeout = 5;
            }
            jsn = jso.getJsonNumber("MaxTCPConnections");            
            if(jsn != null)
            {    
                MaxTCPConnections = jsn.intValueExact();
            }
            else
            {
                MaxTCPConnections = 2048;
            }
        } 
        catch (Exception ex) 
        {
            MaxServerConnections=128;
            WindowSize=30;
            PacketTimeout=5;
            MaxTCPConnections=2048;
            ex.printStackTrace();
            System.err.println("Config not found - Reverting to defaults");
        }
        this.UDPPort = UDPPort;
        this.TCPPort = TCPPort;
        ConnectionPriorityMap = new ConcurrentLinkedQueue<>();
    }

    public int getMaxServerConnections() 
    {
        return MaxServerConnections;
    }

    public int getWindowSize() 
    {
        return WindowSize;
    }

    public int getPacketTimeout() 
    {
        return PacketTimeout;
    }

    public ConcurrentLinkedQueue<PriorityData> getConnectionPriorityMap() 
    {
        return ConnectionPriorityMap;
    }

    public int getUDPPort() 
    {
        return UDPPort;
    }

    public int getTCPPort() 
    {
        return TCPPort;
    }

    public int getMaxTCPConnections() 
    {
        return MaxTCPConnections;
    }

}
