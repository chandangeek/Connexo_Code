package com.energyict.protocolimpl.edf.messages.usecases;

import com.energyict.cbo.BusinessException;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.Folder;
import com.energyict.protocolimpl.edf.messages.*;

import java.sql.SQLException;
import java.util.Iterator;

public class Service05 extends AbstractFolderAction {
    
    public void execute(Folder folder) throws SQLException, BusinessException {
    
        
        try {
            
            Iterator i = folder.getRtus().iterator();
            
            while( i.hasNext() ) {
            
                Device rtu = (Device)i.next();
                
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
                mr = new MessageWriteRegister( "0.0.128.30.22.255", new Integer( 0 ) );
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
