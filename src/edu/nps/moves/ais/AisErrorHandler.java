
package edu.nps.moves.ais;

import nl.esi.metis.aisparser.*;
import nl.esi.metis.aisparser.provenance.*;
/**
 * Handles errros that may be encountered when parsing AIS messages. This
 * may happen for a variety of reasons, including corrupted messages.<p>
 * 
 * It doesn't need to do much, but it needs to be there.
 * 
 * @author DMcG
 */
public class AisErrorHandler implements HandleInvalidInput
{

    @Override
    public void handleInvalidVDMMessage(VDMMessage invalidVDMMessage)
    {
        //System.err.println("Invalid VDM Message");
    }
    
    @Override
    public void	handleInvalidVDMLine(VDMLine invalidVDMLine) 
    {
        //System.err.println("Invalid VDM Line");
    }
    
    @Override
    public void	handleInvalidSensorData(Provenance source, String sensorData) 
    {
        //System.err.println("Invalid sensor data");
    }
    
}
