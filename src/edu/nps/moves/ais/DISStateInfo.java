
package edu.nps.moves.ais;

import nl.esi.metis.aisparser.AISMessagePositionReport;
import java.util.Date;
import edu.nps.moves.dis.*;

/**
 * Contains state information about one entity in the world. This is primarily
 * state information related to DIS.<p>
 * 
 * This can and should contain more info than just that contained in the AIS
 * position update. Info about cargo, ship name, destination, etc can be
 * included here.
 * 
 * @author DMcG
 */
public class DISStateInfo 
{
    /** The last AIS Position report we received */
    AISMessagePositionReport lastAISPositionReport;
    
    /** The time at which we created this state info record */
    Date creationTime;
    
    /** Time at which we received the last AIS report */
    Date lastAISReport;
    
    /** Entity state pdu info for one entity */
    EntityStatePdu espdu;
    
    /** Time at which we sent the last ESPDU update. We must send at least every heartbeat cycle. */
    Date lastEspduUpdate;
    
    /** AIS ID (userID) */
    int aisID;
    

    /** Constructor */
    public DISStateInfo(AISMessagePositionReport positionReport)
    {
        this.lastAISPositionReport = positionReport;
        creationTime = new Date();
        lastAISReport = new Date();
        lastEspduUpdate = null;
        aisID = positionReport.getUserID();
    }
    
    
}
