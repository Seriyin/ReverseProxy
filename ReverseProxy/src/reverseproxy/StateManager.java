/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reverseproxy;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Andre, Matias, Nuno
 */
public class StateManager 
{
    private final Map<InetAddress,PriorityData> ConnectionPriorityMap;
    
    public StateManager() 
    {
        ConnectionPriorityMap = new HashMap<>(30);
    }
    
}
