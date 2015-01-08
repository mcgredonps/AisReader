
package edu.nps.moves.ais;

import nl.esi.metis.aisparser.*;
import javax.xml.bind.*;


/**
 * An AIS message has been decoded. Extract the data from the message. <p>
 * 
 * @author DMcG
 */
public class AisMessageHandler implements HandleAISMessage
{

    public void handleAISMessage(AISMessage message)
    {
        
        switch(message.getMessageID())
        {
            // AIS message types 1, 2, and 3 are all variants of position reports.
            
            case 1:
            case 2:
            case 3:
                AISMessagePositionReport positionReport = (AISMessagePositionReport)message;
                
                int navStatus = positionReport.getNavigationalStatus();
                int rateOfTurn = positionReport.getRateOfTurn();
                int speedOverGround = positionReport.getSpeedOverGround();
                int timeStamp = positionReport.getTimeStamp();
                int trueHeading = positionReport.getTrueHeading();
                int messageId = positionReport.getMessageID();
                boolean positionAccuracy = positionReport.getPositionAccuracy();
                double latitude = positionReport.getLatitudeInDegrees();
                double longitude = positionReport.getLongitudeInDegrees();
                
                String line = "<PositionReport navStatus=\"" + navStatus + "\"" + " rateOfTurn=\"" + rateOfTurn + "\"";
                line = line + " speedOverGround=\"" + speedOverGround + "\"";
                line = line + " timeStamp=\"" + timeStamp + "\"";
                line = line + " trueHeading=\"" + trueHeading + "\"";
                line = line + " messageId=\"" + messageId + "\"";
                line = line + " positionAccuracy=\"" + positionAccuracy + "\"";
                line = line + " latitude=\"" + latitude + "\"";
                line = line + " longitude=\"" + longitude + "\"";
                line = line + "/>";
                System.out.println(line);
                
                
                break;
           
                // Punt on all the other message types for now
            
            default:
                break;
        }
    }
}
