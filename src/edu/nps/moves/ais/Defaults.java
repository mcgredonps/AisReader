
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
    public static int PORT = 9010;
    
    /** Host for AIS feed on campus */
    public static String AIS_HOST = "172.20.70.143";

    /** Default amount of time to collect data, in seconds, before exiting */
    public static int LISTEN_TIME_SECONDS = 30;
}
