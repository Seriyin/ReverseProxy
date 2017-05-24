/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reverseproxy;

import java.net.InetAddress;

/**
 * Priority Data contains a backend's priority parameters, as well as its IP.
 * The parameters considered for priority calculation are how many packets
 * dropped in the latest window, the geometric median of all the packets in the 
 * latest window's RTT, the number of active TCP connections for that backend
 * as well as the number of timeouts that occurred in the latest window.
 * @author Andre, Matias, Nuno
 */
public class PriorityData 
{
    private InetAddress ServerAddress;
    private int windowdrop;
    private int rttestimated;
    private int activeconnections;
    private int timeouts;
    
    /**
     * PriorityData is initialized with the backend IP and very high fictitious
     * values of each parameter, guaranteeing an incredibly low priority until
     * the actual state of the backend can be updated.
     * @param addr IP of the backend
     */
    public PriorityData(InetAddress addr) 
    {
        ServerAddress=addr;
        windowdrop=100;
        rttestimated=1000;
        activeconnections=0;
        timeouts=100;
    }
    
    /**
     * Calculates priority for a given server based on packet drops, 
     * rtt estimated, active connections and number of delays.
     * It's calculated through a simple multiplication of each parameter.
     * @return priority, lower is better.
     */
    public int calculatePriority() 
    {
        return (activeconnections+1)*rttestimated*(windowdrop+1)*((timeouts/3)+1);
    }
    
    /**
     * Call to update window, doesn't collide with active connections, will
     * only be called by one thread, no need to synchronize
     * @param rtt estimated rtt valid for current window
     * @param dropped estimated packet drops for current window
     * @param timeouts estimated number of delays for current window
    */
    public void updateWindow(int rtt, int dropped, int timeouts) 
    {
        rttestimated=rtt;
        windowdrop=dropped;
        this.timeouts=timeouts;
    }

    public synchronized void incActiveConnections() 
    {
        activeconnections++;
    }

    public synchronized void decActiveConnections() 
    {
        activeconnections--;
    }
}
