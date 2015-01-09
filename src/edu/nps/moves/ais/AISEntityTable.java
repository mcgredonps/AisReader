
package edu.nps.moves.ais;

import java.util.*;
import java.util.concurrent.*;
import nl.esi.metis.aisparser.AISMessagePositionReport;
import edu.nps.moves.dis.*;
import edu.nps.moves.disutil.*;

/**
 * 
 * @author DMcG
 */
public class AISEntityTable 
{
    /**
     *  Single, shared instance
     */
    private static AISEntityTable instance = null;
        
    /** Hashmap, keyed by AIS entity ID, with a value containing DIS information. */
    private ConcurrentMap<Integer, DISInfo> aisEntities;
    
    /**
     * Site an application IDs, for the DIS entity ID triplet. This should be
     * moved to a properties file rather than hard coded.
     */
    private static final int SITE_ID        = 4242;
    private static int APPLICATION_ID = 4545;
    
    /** 
     * The next entity ID to assign. Note that this must be less than the
     * max unsigned short ( 64K). This may not be enough to represent all
     * ships currently at sea throughout the world. Maybe use multiple 
     * appIDs to increase the max number of entities.
     */
    private static int nextEntityID = 1;
    
    /**
     * Returns the single, shared instance of the AISEntityTable
     * @return shared instance
     */
    public static synchronized AISEntityTable getInstance()
    {
        if(instance == null)
        {
            instance = new AISEntityTable();
        }
        
        return instance;
    }
    
    /** Private constructor, so no one can create an instance of this class in
     * any other way than by using the getInstance() method
     */
    private AISEntityTable()
    {
        aisEntities = new ConcurrentHashMap<Integer, DISInfo>(); 
    }
    
    /**
     * Get the next-highest DIS entity ID. Note that the max is 2^16 - 1,
     * and this may not be enough to represent all ships. If that's the case,
     * you should rework this to use multiple app IDs. 
     * @return 
     */
    private int getNextEntityID()
    {
        int val = nextEntityID;
        nextEntityID++;
        
        // Detect rollover
        if(nextEntityID >= 65535 )
        {
            System.out.println("More ships at sea than can be counted.");
            APPLICATION_ID++;
            nextEntityID = 1;
            val = nextEntityID;
        }
        
        return val;
    }
    
    public void setAISPositionReport(AISMessagePositionReport aisPosition)
    {
        DISInfo info = aisEntities.get(aisPosition.getUserID());
        if(info == null)
        {
            info = new DISInfo(aisPosition);
            
            info.lastAISReport = new Date();
            // Set the entityID
            EntityStatePdu pdu = new EntityStatePdu();
            pdu.getEntityID().setSite(SITE_ID);
            pdu.getEntityID().setEntity(this.getNextEntityID());
            pdu.getEntityID().setApplication(APPLICATION_ID);

            // Set the entity type
            pdu.getEntityType().setEntityKind((short)1);  // Entity in world
            pdu.getEntityType().setDomain((short)3);      // surface ship
            pdu.getEntityType().setCountry((short)225);   // Need better country data
            pdu.getEntityType().setCategory((short)61);   // Noncombatant ship
            pdu.getEntityType().setSubcategory((short)3); // large fishing trawler
            
            info.espdu = pdu;
            
            // Close enough for now--should also set DR, speed, etc. Also marking
        }
        
        // Position
        Vector3Double position = info.espdu.getEntityLocation();
        double latitude = aisPosition.getLatitudeInDegrees();
        double longitude = aisPosition.getLongitudeInDegrees();
        double altitude = 0.0; // Bad assumption! Great Lakes!

        double[] disCoords = CoordinateConversions.getXYZfromLatLonDegrees(latitude, longitude, altitude);
        position.setX(disCoords[0]);
        position.setY(disCoords[1]);
        position.setZ(disCoords[2]);
        
    }
    
    /**
     * Walk through all the ais entities we know about, sending an update if our
     * heartbeat time has expired.<p>
     * 
     * This needs to be looked at for effeciency. There may be tens of thousands
     * of AIS entities, and the time required to send DIS updates for all of them
     * may exceed the heartbeat time.
     */
    public void doDISNetworkUpdates()
    {
        DISNetwork disNetwork = DISNetwork.getInstance();
        
        Collection values = aisEntities.values();
        Iterator it = values.iterator();
        while(it.hasNext())
        {
            DISInfo info = (DISInfo)it.next();
            Date now = new Date();
            if(info.lastEspduUpdate == null || info.lastEspduUpdate.getTime() + DISNetwork.HEARTBEAT > now.getTime())
            {
                info.lastEspduUpdate = now;
                disNetwork.sendPDU(info.espdu);
            }
        }
    }
    
}
