/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.nps.moves.ais;

import nl.esi.metis.aisparser.AISMessagePositionReport;
import java.util.Date;
import edu.nps.moves.dis.*;

/**
 *
 * @author DMcG
 */
public class DISInfo 
{
    AISMessagePositionReport lastAISPositionReport;
    Date creationTime;
    Date lastAISReport;
    EntityStatePdu espdu;
    Date lastEspduUpdate;
    int aisID;
    

    public DISInfo(AISMessagePositionReport positionReport)
    {
        this.lastAISPositionReport = positionReport;
        creationTime = new Date();
        lastAISReport = new Date();
        lastEspduUpdate = null;
        aisID = positionReport.getUserID();
    }
    
    
}
