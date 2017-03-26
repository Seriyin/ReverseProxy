/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reverseproxy.udpmanager;

import java.net.DatagramPacket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;

/**
 *  Class that contains information that needs to be shared
 *  with threads that handle UDP monitoring and the UDP server.
 * @author Andre, Matias, Nuno
 */
public class ThreadData 
{
    private final ArrayBlockingQueue<DatagramPacket> PacketQueue;
    private boolean bUnderCongestion;
    private final Future<?> ThreadHandle;
    
    ThreadData(ArrayBlockingQueue<DatagramPacket> q, boolean b, Future<?> th) 
    {
        PacketQueue=q;
        bUnderCongestion=b;
        ThreadHandle=th;
    }

    ArrayBlockingQueue<DatagramPacket> getPacketQueue() 
    {
        return PacketQueue;
    }

    
    /**
     *  Congestion is needed only when the UDP server lags.
     *  Threads on wake-up might find they have no packets to read
     *  not due to lack of receiving, but rather due to dropped packets.
     *  In that case, congestion is set to indicate packets have been dropped.
     * @return indicates whether the packet queue is under congestion.
     */
    public boolean isUnderCongestion() 
    {
        return bUnderCongestion;
    }

    public void setUnderCongestion(boolean uc) {
        bUnderCongestion = uc;
    }

    
    public Future<?> getThreadHandle() {
        return ThreadHandle;
    }
    
}
