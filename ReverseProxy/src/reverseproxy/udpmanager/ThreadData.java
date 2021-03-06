/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reverseproxy.udpmanager;

import java.net.DatagramPacket;
import java.net.InetAddress;
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
    private Future<?> ProcessorThread;
    private Future<?> ProberThread;
    private Thread ProberThreadHandle;
    private final InetAddress associatedAddress;
    
    /**
     * Constructs a ThreadData with a packet queue and backend IP.
     * It leaves all thread data in unspecified state.
     * @param q the PacketQueue of a given IP
     * @param addr the backend IP
     */
    ThreadData(ArrayBlockingQueue<DatagramPacket> q, InetAddress addr) 
    {
        PacketQueue=q;
        bUnderCongestion=false;
        associatedAddress=addr;
    }

    ArrayBlockingQueue<DatagramPacket> getPacketQueue() 
    {
        return PacketQueue;
    }

    InetAddress getAddress()
    {
        return associatedAddress;
    }
    
    /**
     *  Congestion is needed only when the UDP server lags.
     *  Threads on wake-up might find they have no packets to read
     *  not due to lack of receiving, but rather due to dropped packets from
     *  emptying a queue when it's full.
     *  In that case, congestion is set to indicate packets have been dropped.
     * @return indicates whether the packet queue is under congestion.
     */
    public boolean isUnderCongestion() 
    {
        return bUnderCongestion;
    }

    public void setUnderCongestion(boolean uc) 
    {
        bUnderCongestion = uc;
    }

    
    public Future<?> getProcessorThread() 
    {
        return ProcessorThread;
    }

    public Future<?> getProberThread() 
    {
        return ProberThread;
    }
    
    public void registerProcessorThread(Future <?> th) 
    {
        ProcessorThread = th;
    }
    
    public Thread getProberThreadHandle() 
    {
        return ProberThreadHandle;
    }
    
    public void registerProberThread(Future <?> th) 
    {
        ProberThread = th;
    }

    public void registerProberThreadHandle(Thread th) 
    {
        ProberThreadHandle = th;
    }
}
