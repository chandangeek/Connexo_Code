package com.energyict.edf.messages.usecases;

import java.sql.SQLException;
import java.util.Iterator;

import com.energyict.cbo.BusinessException;
import com.energyict.edf.messages.*;
import com.energyict.mdw.core.Folder;
import com.energyict.mdw.core.Rtu;

public class Service02 extends AbstractFolderAction {
    
    public void execute(Folder folder)
        throws SQLException, BusinessException {
    
        try {
            
            Iterator i = folder.getRtus().iterator();
            
            while( i.hasNext() ) {
            
                Rtu rtu = (Rtu)i.next();
                
                /* 1 Read indexes */
                MessageContent mr = new MessageReadIndexes( );
                mr.setOrdinal( 0 );
                createMessage( rtu, mr );
                
                /* 9 Read Daily Energy  */
                mr = new MessageReadBillingValues( );
                mr.setOrdinal( 1 );
                createMessage( rtu, mr);
                
                /* 10 Load profile energy */
                mr = new MessageReadLoadProfiles( );
                mr.setOrdinal( 1 );
                createMessage( rtu, mr);
                
                /* 11 Read Meter clock */
                addMessage(rtu, "0.0.1.0.0.255",        2);
                
                /* 12 Read Activity Calendar */
                addMessage(rtu, "0.0.13.0.0.255",       3);
                
                /* 13 Read Moving Peak */
                addMessage(rtu, "0.0.10.0.125.255",     4);
                
                /* 14 Read Demand management */
                addMessage(rtu, "0.0.16.0.1.255",       5);
                
                /* 15 Read Meter Identification */
                addMessage(rtu, "0.0.96.2.0.255",       6);
                
                /* 16 Read TIC configuration */
                addMessage(rtu, "0.0.96.3.2.255",       7);
                
                /* 17 Read threshold for sag */
                addMessage(rtu, "1.0.12.31.0.255",      8);
                
                /* 18 Read threshold for swell */
                addMessage(rtu, "1.0.12.35.0.255",      9);
                
                /* 19 Read Time integral for sag measurement */
                addMessage(rtu, "1.0.12.31.129.255",    10);
                
                /* 20 Time integral for for long power failure */
                addMessage(rtu, "0.0.96.7.20.255",      11);
                
                /* 21 Time integral for instantaneous demand  */ 
                addMessage(rtu, "1.0.0.8.2.255",        12);
                
                /* 22 Time integral for swell measurement */
                addMessage(rtu, "1.0.12.35.129.255",    13);
                
                /* 23 Meter status*/
                addMessage(rtu, "0.0.96.5.0.255",       14);
                
                /* 24 Error code register */
                addMessage(rtu, "0.0.97.97.0.255",      15);

                /* 25 Breaker status */
                addMessage(rtu, "0.0.128.30.22.255",    16);
                
                /* 26 Error code register*/
                mr = new MessageReadLogBook( );
                mr.setOrdinal(17);
                createMessage( rtu, mr );
                
                /* 27 Number of long power failures */
                addMessage(rtu, "0.0.96.7.5.255",       18);
                
                /* 28 Number of short power failures */
                addMessage(rtu, "0.0.96.7.0.255",       19);
                
                /* 29 Number of sags */
                addMessage(rtu, "1.0.12.32.0.255",      20);
                
                /* 30 Number of swells */
                addMessage(rtu, "1.0.12.36.0.255",      21);
                
                /* 31 Maximum voltage */
                addMessage(rtu, "1.0.12.38.0.255",      22);

                /* 32 Minimum voltage */
                addMessage(rtu, "1.0.12.34.0.255",      23);
                
                /* 33 Instantaneous demand */
                addMessage(rtu, "1.0.1.7.0.255",        24);
                
            }
            
        } catch( Exception ex ){
            
            ex.printStackTrace();
            throw new BusinessException( ex );
            
        }
        

        
    }
    
    public String getVersion() {
        return " $ Revision: 1 $ ";
    }
    
}
