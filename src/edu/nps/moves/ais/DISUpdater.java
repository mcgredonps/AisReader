
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
            AISEntityTable entityTable = AISEntityTable.getInstance();
            entityTable.doDISNetworkUpdates();
            try
            {
                Thread.sleep(5000);
            }
            catch(Exception e)
            {
                System.out.println("Unsettled sleep");
            }
        }
        
    }
}
