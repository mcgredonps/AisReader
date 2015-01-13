
package edu.nps.moves.ais;

import java.util.*;
import java.util.concurrent.*;
import nl.esi.metis.aisparser.AISMessagePositionReport;
import edu.nps.moves.dis.*;
import edu.nps.moves.disutil.*;
import nl.esi.metis.aisparser.AISMessage05;

/**
 * Contains a table that maps AIS user IDs to DIS state information. A singleton,
 * this maps AIS IDs to DIS state information, including position, entityID, 
 * ship name, and so on.<p>
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
    private ConcurrentMap<Integer, DISStateInfo> aisEntities;
    
    /**
     * Site an application IDs, for the DIS entity ID triplet. This should be
     * moved to an external properties file rather than hard coded.
     */
    private static final int SITE_ID        = 4242;
    private static int       APPLICATION_ID = 4545;
    
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
            System.out.println("Created AIS State table");
        }
        
        return instance;
    }
    
    /** Private constructor, so no one can create an instance of this class in
     * any other way than by using the getInstance() method
     */
    private AISEntityTable()
    {
        aisEntities = new ConcurrentHashMap<Integer, DISStateInfo>(); 
    }
    
    /**
     * Get the next-highest DIS entity ID. Note that the max is 2^16 - 1,
     * and this may not be enough to represent all ships. If we hit max
     * for the entityID, we increment the appID and reset the entity ID back
     * to one.<p>
     * 
     * @return next available entity ID
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
    
    /** 
     * We receive an AIS position report. Add it to the state table, either by
     * updating an existing state object for that AIS ID, or, if this is the
     * first time we've heard of that AIS ID, create a new entry in the table
     * for state information about that entity.
     * 
     * @param aisPosition AIS position update
     */
    public void setAISPositionReport(AISMessagePositionReport aisPosition)
    {
        // Retrieve state information for this AIS entity. If we get null back,
        //
        DISStateInfo info = aisEntities.get(aisPosition.getUserID());
        if(info == null)
        {
            info = new DISStateInfo(aisPosition);
            
            info.lastAISReport = new Date();
            // Set the entityID
            EntityStatePdu pdu = new EntityStatePdu();
            pdu.getEntityID().setSite(SITE_ID);
            pdu.getEntityID().setEntity(this.getNextEntityID());
            pdu.getEntityID().setApplication(APPLICATION_ID);
            //System.out.println("new DIS entity, " + pdu.getEntityID().getEntity());

            // Set the entity type
            pdu.getEntityType().setEntityKind((short)1);  // Entity in world
            pdu.getEntityType().setDomain((short)3);      // surface ship
            pdu.getEntityType().setCountry((short)225);   // Need better country data
            pdu.getEntityType().setCategory((short)61);   // Noncombatant ship
            pdu.getEntityType().setSubcategory((short)3); // large fishing trawler
            
            info.espdu = pdu;
            
            aisEntities.put(aisPosition.getUserID(), info);
            
            // Close enough for now--should also set DR, speed, etc. Also marking, with
            // messages from other AIS message types.
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
        
        info.canSendUpdate = true;
        
    }
    
    public void setStaticInfoReport(AISMessage05 staticAISInfo)
    {
        DISStateInfo info = aisEntities.get(staticAISInfo.getUserID());
        
        // Don't have a position report yet? Create an empty entry, and 
        // mark it as "don't send" because we have no position. 
        if(info == null)
        {
           info = new DISStateInfo(null);
            
            info.lastAISReport = new Date();
            // Set the entityID
            EntityStatePdu pdu = new EntityStatePdu();
            pdu.getEntityID().setSite(SITE_ID);
            pdu.getEntityID().setEntity(this.getNextEntityID());
            pdu.getEntityID().setApplication(APPLICATION_ID);
            //System.out.println("new DIS entity, " + pdu.getEntityID().getEntity());

            // Set the entity type
            pdu.getEntityType().setEntityKind((short)1);  // Entity in world
            pdu.getEntityType().setDomain((short)3);      // surface ship
            pdu.getEntityType().setCountry((short)225);   // Need better country data
            pdu.getEntityType().setCategory((short)61);   // Noncombatant ship
            pdu.getEntityType().setSubcategory((short)3); // large fishing trawler
            
            info.espdu = pdu;
        }
        
        info.shipName = staticAISInfo.getName();
        info.callSign = staticAISInfo.getCallSign();
        info.espdu.getMarking().setCharacters(info.shipName);
        
        // Other info here, such as draught, destination, cargo, etc.
        
        return;
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
        //System.out.println("Sending updates for state table, number of entries=" + values.size());
        
        int maxUpdates = 20;
        int updateCount = 0;
        
        while(it.hasNext())
        {
            DISStateInfo info = (DISStateInfo)it.next();
            Date now = new Date();
            
            if( (info.lastEspduUpdate == null || info.lastEspduUpdate.getTime() + DISNetwork.HEARTBEAT < now.getTime()) && info.canSendUpdate)
            {
                info.lastEspduUpdate = now;
                //System.out.println("Updating DIS for ship name:" + new String(info.espdu.getMarking().getCharacters()));
                
                //if(updateCount < maxUpdates)
                {
                    disNetwork.sendPDU(info.espdu);
                }

            }
            updateCount++;
        }
    }
    
}
