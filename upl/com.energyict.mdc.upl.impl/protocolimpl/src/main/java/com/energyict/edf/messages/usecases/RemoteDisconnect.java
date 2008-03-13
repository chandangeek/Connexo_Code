package com.energyict.edf.messages.usecases;

import java.sql.SQLException;
import java.util.Iterator;

import com.energyict.cbo.BusinessException;
import com.energyict.edf.messages.*;
import com.energyict.mdw.core.Folder;
import com.energyict.mdw.core.Rtu;

public class RemoteDisconnect extends AbstractFolderAction {
    
    public void execute(Folder folder) throws SQLException, BusinessException {
    
        
        try {
            
            Iterator i = folder.getRtus().iterator();
            
            while( i.hasNext() ) {
            
                Rtu rtu = (Rtu)i.next();

                MessageContent mr = new MessageWriteRegister( "0.0.128.30.22.255", new Integer( 0 ) );
                mr.setOrdinal(0);
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
