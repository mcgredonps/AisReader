
package edu.nps.moves.ais;

import java.net.*;
import java.io.*;
import java.util.*;


import nl.esi.metis.aisparser.*;
import nl.esi.metis.aisparser.provenance.*;

import org.apache.commons.cli.*;


/**
 * Main entry point. Read from a TCP socket on the AIS server, receive messages,
 * and decode them. 
 * <p>
 * 
 * @author DMcG
 */
public class TcpReader 
{
    private InetAddress address = null;
    private int port;
    private Socket socket = null;
    private boolean breakOutOfReadLoop = false;
    AISParser aisParser = null;
    
    public TcpReader(InetAddress address, int port)
    {
        this.address = address;
        this.port = port;
        
        // Configure the AIS parser to have a couple objects, one which will receive
        // parsed AIS messages, and one which will receive error messages when there
        // is a malformed or unparsable message.
        
        this.aisParser = new AISParser(new AisMessageHandler(), new AisErrorHandler());
    }
    
    /**
     * Establish a TCP connection to the AIS server.
     */
    public void establishConnection()
    {
        try
        {
            socket = new Socket(address, port);
            socket.setSoTimeout(30000);
        }
        catch (Exception e)
        {
            System.out.println("Failed to establish TCP connection to AIS server");
            System.out.println(e);
        }
    }
    
    /**
     * Read data from the AIS server line by line, and decode each line. This
     * is wrapped with some bogus XML that should be removed.
     */
    public void readData()
    {
        int lineNumber = 0;
        //while(breakOutOfReadLoop == false)
        //{
        try
        {
            BufferedReader socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            
            // Because the AIS server pumps out such a huge amount of data, by default
            // we read only for a timeout period, then exit. Which is sorta OK for a demo
            // program.
            
            AISEntityTable.getInstance();
            Thread updateThread = new Thread(new DISUpdater());
            updateThread.start();
        
            // Read line by line, and decode each line. "provenance" is where the data
            // came from, which doesn't really make sense in this context (it all comes
            // from the AIS server) but the box has to be checked.
            
            while(breakOutOfReadLoop == false)
            {
                String aisLine = socketReader.readLine();
                Provenance provenance = new FileSource(null, lineNumber,  aisLine, System.currentTimeMillis());
                aisParser.handleSensorData(provenance, aisLine);
                lineNumber++;
            }
        }
        catch(Exception e)
        {
            System.err.println("bad read " + e);
        }
        finally
        {
            //System.out.println("</AISPositionReports>");
        }
        //}
        
    }
    
    /**
     * Uses Apache commons command line library. Allows you to provide command
     * line switches on startup.
     * 
     * Command line args:
     *  -h, --host: IP of host with the AIS feed
     *  -p, --port: TCP port number
     *  -t, --time: length of time to run
     * 
     * @param args 
     */
    public static void main(String[] args)
    {
        String host = Defaults.AIS_HOST;
        int portInt = Defaults.PORT;
        int listenTime = Defaults.LISTEN_TIME_SECONDS;
        
        InetAddress hostIp = null;
        
        
        
        Options options = new Options();
        options.addOption("a", "ais-server", true, "IP of host with TCP AIS service" );
        options.addOption("p", "port", true, "TCP port on AIS host to connect to");
        options.addOption("t", "time", true, "Length of time to collect data, in seconds");
        options.addOption("h", "help", false, "Help");
        
        
        
        try
        {
            CommandLineParser parser = new PosixParser();
            CommandLine cmd = parser.parse( options, args);
                        
            if(cmd.hasOption("a"))
            {
                host = cmd.getOptionValue('a');
            }
            
            if(cmd.hasOption('p'))
            {
                portInt = Integer.parseInt(cmd.getOptionValue('p'));
            }
            
            if(cmd.hasOption('t'))
            {
                listenTime = Integer.parseInt(cmd.getOptionValue('t'));
            }
            
            if(cmd.hasOption('h'))
            {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp( "TcpReader", options );
                System.exit(0);
            }
            
        }
        catch(Exception e)
        {
            System.err.println(e);
            //e.printStackTrace();
        }
        
        // NPS default TCP address and port for AIS feed is 172.20.70.143 9010
        //System.out.println("Connecting to AIS server at " + host + ", " + portString);
        
        try
        {
            hostIp = InetAddress.getByName(host);
        }
        catch(Exception e)
        {
            System.err.println(e);
        }
        
        try
        { 
            final TcpReader reader = new TcpReader(hostIp, portInt);
            
            // Run for some amount of time, then flip a switch to break out of the
            // read loop on the AIS server. This is here only for demo program
            // purposes. Since the AIS server pumps out so much data I'm trying
            // to prevent a "run forever asking for the position of every ship in
            // the world" scenario, literally.
            
            Timer runLengthTimer = new Timer(true);
            TimerTask tt = new TimerTask()
                    {
                        @Override
                        public void run()
                        {
                            reader.breakOutOfReadLoop();
                        };
                    };
            
            runLengthTimer.schedule(tt, Defaults.LISTEN_TIME_SECONDS * 1000);
                    
                    
            reader.establishConnection();
            reader.readData();
        }
        catch(Exception e)
        {
            
        }
        
    }
    
    public void breakOutOfReadLoop()
    {
        breakOutOfReadLoop = true;
    }

}
