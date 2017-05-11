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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 *
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
                                        new File("./config.json")))));
            JsonObject jso;
            jso=jsr.readObject();
            MaxServerConnections = jso.getJsonNumber("MaxServerConnections").intValueExact();
            WindowSize = jso.getJsonNumber("WindowSize").intValueExact();
            PacketTimeout = jso.getJsonNumber("PacketTimeout").intValueExact();
            MaxTCPConnections = jso.getJsonNumber("MaxTCPConnections").intValueExact();
        } 
        catch (Exception ex) 
        {
            MaxServerConnections=128;
            WindowSize=30;
            PacketTimeout=5;
            MaxTCPConnections=2048;
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
