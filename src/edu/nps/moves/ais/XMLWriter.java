/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.nps.moves.ais;

import java.io.*;
import nl.esi.metis.aisparser.*;

/**
 *
 * @author DMcG
 */
public class XMLWriter 
{
    public static XMLWriter xmlWriter = null;
    private static final String fileName = "positionReports.xml";
    
    private PrintWriter pw = null;
    
    public static XMLWriter getXMLWriter()
    {
        if(xmlWriter == null)
        {
            xmlWriter = new XMLWriter();
        }
        
        return xmlWriter;
    }
    
    private XMLWriter()
    {
        try
        {
            pw = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
        }
        catch (Exception e)
        {
            System.out.println("Can't create output file " + fileName + " exiting");
            System.exit(0);
        }
    }
    
    public void writePositionReport(AISMessagePositionReport pr)
    {
        pw.print("<AISMessage messageType=" + pr.getMessageID() + ">" );
        pw.print("");
    }

}
