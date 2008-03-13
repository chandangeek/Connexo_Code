package com.energyict.edf.messages.usecases;

import java.sql.SQLException;
import java.util.Iterator;

import com.energyict.cbo.BusinessException;
import com.energyict.edf.messages.*;
import com.energyict.mdw.core.Folder;
import com.energyict.mdw.core.Rtu;

public class Service05_connect extends AbstractFolderAction {
    
    public void execute(Folder folder) throws SQLException, BusinessException {
    
        
        try {
            
            Iterator i = folder.getRtus().iterator();
            
            while( i.hasNext() ) {
            
                Rtu rtu = (Rtu)i.next();
                
                /* 1-8 -> read indexes */
                MessageContent mr = new MessageReadIndexes( );
                mr.setOrdinal(0);
                createMessage( rtu, mr);
         
                /* 9 -> read profile */
                mr = new MessageReadBillingValues( );
                mr.setOrdinal(1);
                createMessage( rtu, mr);
                
                /* 9 -> read profile */
                mr = new MessageReadLoadProfiles( );
                mr.setOrdinal(1);
                createMessage( rtu, mr);
                
                /* 10 -> disconnect */
                mr = new MessageWriteRegister( "0.0.128.30.22.255", new Integer( 2 ) );
                mr.setOrdinal(2);
                createMessage( rtu, mr);
                
            }
        
        } catch( Exception ex ) {
            
            
            ex.printStackTrace();
            throw new BusinessException( ex );
            
        }
        
            
    }
    
    public String getVersion() {
        return " $ Revision: 1 $ ";
    }
    
}
