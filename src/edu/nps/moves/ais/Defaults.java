
package edu.nps.moves.ais;

/**
 * Various configuration defaults, such as the address of the AIS server,
 * port number, and so on. This should be a properties file. 
 * 
 * @author DMcG
 */
public class Defaults 
{
    /** Default port for AIS TCP feed on campus */
    public static int AIS_PORT = 9010;
    
    /** Host for AIS feed on campus */
    public static String AIS_HOST = "172.20.70.143";

    /** Default amount of time to collect data, in seconds, before exiting */
    public static int LISTEN_TIME_SECONDS = 30;
    
    /** Frequency at which to send out DIS updates */
    public static final int HEARTBEAT = 5000;
    
    /** Port on which DIS is sent */
    public static final int DIS_PORT = 3000;
    
    /** This is hardcoded, but it should be found at runtime by walking the
     * NetworkInterface objects.
     */
    public static final String DIS_BROADCAST_ADDRESS = "172.20.159.255";
}
