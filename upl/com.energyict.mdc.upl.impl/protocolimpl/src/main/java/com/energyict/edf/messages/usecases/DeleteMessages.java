package com.energyict.edf.messages.usecases;

import java.sql.SQLException;
import java.util.Iterator;

import com.energyict.cbo.BusinessException;
import com.energyict.mdw.core.*;

public class DeleteMessages extends AbstractFolderAction {
    
    public void execute(Folder folder)
        throws SQLException, BusinessException {
    
        try {
            
            Iterator i = folder.getRtus().iterator();
            
            while( i.hasNext() ) {
            
                Rtu rtu = (Rtu)i.next();
                
                Iterator ir = rtu.getMessages().iterator();
                
                while( ir.hasNext() ) {
                    RtuMessage msg = (RtuMessage) ir.next();
                    
                    msg.delete();
                    
                }
                
                
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
