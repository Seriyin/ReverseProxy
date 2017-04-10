/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reverseproxy;

import java.net.InetAddress;

/**
 *
 * @author Andre, Matias, Nuno
 */
public class PriorityData 
{
    InetAddress ServerAddress;
    int windowdrop;
    float rttestimated;
    int activeconnections;
    
    public PriorityData(InetAddress addr) 
    {
        InetAddress ServerAddress=addr;
    }
    
    public float calculatePriority() 
    {
        return activeconnections*rttestimated*windowdrop;
    }
}
