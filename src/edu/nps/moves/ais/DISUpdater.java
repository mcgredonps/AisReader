
package edu.nps.moves.ais;

/**
 *
 * @author DMcG
 */
public class DISUpdater implements Runnable
{

    public void run()
    {
        while(true)
        {
            //System.out.println("Sending heartbeat for state table");
            AISEntityTable entityTable = AISEntityTable.getInstance();
            entityTable.doDISNetworkUpdates();
            try
            {
                Thread.sleep(Defaults.HEARTBEAT);
            }
            catch(Exception e)
            {
                System.out.println("Unsettled sleep");
            }
        }
        
    }
}
