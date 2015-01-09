
package edu.nps.moves.ais;

import java.net.*;
import java.io.*;

import edu.nps.moves.dis.*;

/**
 * A class that allows you to send DIS PDUs, bcast. The broadcast address should
 * be specific to your network. (Too lazy to write the code to automatically find
 * the bcast address.)
 * 
 * @author DMcG
 */
public class DISNetwork 
{
    /** Heartbeat period, in ms */
    public static final int HEARTBEAT = 5000;
    public static final int DIS_PORT = 3000;
    public static final String BROADCAST_ADDRESS = "172.20.81.255";
    public InetAddress bcastAddress;
    
    DatagramSocket socket = null;

    private static DISNetwork instance = null;
    
    public static synchronized DISNetwork getInstance()
    {
        if(instance == null)
        {
            instance = new DISNetwork();
        }
        
        return instance;
    }
    
    private DISNetwork()
    {
        try
        {
            socket = new DatagramSocket(DIS_PORT);
            socket.setBroadcast(true);
            bcastAddress = InetAddress.getByName(BROADCAST_ADDRESS);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.out.println(e);
            System.out.println("Probable cause: socket port already used");
        }
    }
    
    public void sendPDU(Pdu aPdu)
    {
        try
        {
            byte[] disData = aPdu.marshalWithDisAbsoluteTimestamp();
            DatagramPacket packet = new DatagramPacket(disData, disData.length, bcastAddress, DIS_PORT);
            socket.send(packet);
        }
        catch(Exception e)
        {
            System.out.println("Could not send packet; is the bcast address correct?");
            e.printStackTrace();
            System.out.println(e);
        }
    }
}
