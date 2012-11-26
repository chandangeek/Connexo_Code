package com.energyict.protocolimpl.edf.messages.usecases;

import com.energyict.cbo.BusinessException;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.Folder;
import com.energyict.protocolimpl.edf.messages.MessageContent;
import com.energyict.protocolimpl.edf.messages.MessageWriteRegister;

import java.sql.SQLException;
import java.util.Iterator;

public class RemoteDisconnect extends AbstractFolderAction {
    
    public void execute(Folder folder) throws SQLException, BusinessException {
    
        
        try {
            
            Iterator i = folder.getRtus().iterator();
            
            while( i.hasNext() ) {
            
                Device rtu = (Device)i.next();

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
