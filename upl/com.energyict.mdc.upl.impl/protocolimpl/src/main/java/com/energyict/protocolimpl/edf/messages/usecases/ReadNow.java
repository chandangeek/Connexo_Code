package com.energyict.protocolimpl.edf.messages.usecases;

import com.energyict.cbo.BusinessException;
import com.energyict.mdw.core.*;

import java.sql.SQLException;
import java.util.Iterator;

public class ReadNow extends AbstractFolderAction {
    
    public void execute(Folder folder)
        throws SQLException, BusinessException {
    
        try {
            
            Iterator i = folder.getDevices().iterator();
            
            while( i.hasNext() ) {
            
                Device rtu = (Device)i.next();
                
//                Iterator schi = rtu.getCommunicationSchedulers().iterator();
//
//                if( schi.hasNext() ) {
//                    CommunicationScheduler cs = (CommunicationScheduler)schi.next();
//                    cs.startReadingNow();
//                }
                
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
