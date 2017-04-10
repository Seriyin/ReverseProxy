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
import java.util.Comparator;
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
    private int MaxServerConnections;
    private int WindowSize;
    private float PacketTimeout;
    private final ConcurrentSkipListSet<PriorityData> ConnectionPriorityMap;
    
    private StateManager() 
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
            PacketTimeout = jso.getJsonNumber("PacketTimeout").bigDecimalValue().floatValue();
        } 
        catch (Exception ex) 
        {
            MaxServerConnections=64;
            WindowSize=10;
            PacketTimeout=5;
        }
        ConnectionPriorityMap = new ConcurrentSkipListSet<>(Comparator.comparing(PriorityData::calculatePriority));
    }

    public ConcurrentSkipListSet<PriorityData> getConnectionPriorityMap() 
    {
        return ConnectionPriorityMap;
    }

    public int getMaxConnections() 
    {
        return MaxServerConnections;
    }
}
