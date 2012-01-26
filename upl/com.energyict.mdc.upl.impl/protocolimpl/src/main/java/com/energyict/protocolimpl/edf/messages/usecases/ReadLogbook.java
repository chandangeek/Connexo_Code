package com.energyict.protocolimpl.edf.messages.usecases;

import com.energyict.cbo.BusinessException;
import com.energyict.mdw.core.Folder;
import com.energyict.mdw.core.Rtu;
import com.energyict.protocolimpl.edf.messages.MessageContent;
import com.energyict.protocolimpl.edf.messages.MessageReadLogBook;

import java.sql.SQLException;
import java.util.Iterator;

public class ReadLogbook extends AbstractFolderAction {
    
    public void execute(Folder folder)
        throws SQLException, BusinessException {
    
        try {
            
            Iterator i = folder.getRtus().iterator();
            
            while( i.hasNext() ) {
            
                Rtu rtu = (Rtu)i.next();
                
                MessageContent mr = new MessageReadLogBook( );
                mr.setOrdinal(0);
                createMessage( rtu, mr);
    
                
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
