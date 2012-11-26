package com.energyict.protocolimpl.edf.messages.usecases;

import com.energyict.cbo.BusinessException;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.Folder;

import java.sql.SQLException;
import java.util.Iterator;

public class ReadAllRegisters extends AbstractFolderAction {
    
    public void execute(Folder folder)
        throws SQLException, BusinessException {
    
        try {
            
            Iterator i = folder.getRtus().iterator();
            
            while( i.hasNext() ) {
            
                Device rtu = (Device)i.next();
                
                addMessage(rtu, "0.0.1.0.0.255",        0);
                addMessage(rtu, "0.0.96.5.0.255",       1);
                addMessage(rtu, "0.0.97.97.0.255",      2);
                addMessage(rtu, "0.0.13.0.0.255",       3);
                addMessage(rtu, "0.0.10.0.125.255",     4);
                addMessage(rtu, "0.0.128.30.22.255",    5);
                addMessage(rtu, "0.0.96.2.0.255",       6);
                
                addMessage(rtu, "0.0.16.0.1.255",       7);
                addMessage(rtu, "0.0.96.3.2.255",       8);
                
                addMessage(rtu, "1.0.1.8.0.255",        9);
                addMessage(rtu, "1.0.1.8.1.255",        10);
                addMessage(rtu, "1.0.1.8.2.255",        11);
                addMessage(rtu, "1.0.1.8.3.255",        12);
                addMessage(rtu, "1.0.1.8.4.255",        13);
                addMessage(rtu, "1.0.1.8.5.255",        14);
                addMessage(rtu, "1.0.1.8.6.255",        15);
                
                addMessage(rtu, "1.0.12.31.0.255",      16);
                addMessage(rtu, "1.0.12.35.0.255",      17);
                addMessage(rtu, "1.0.12.31.129.255",    18);
                
                addMessage(rtu, "0.0.96.7.20.255",      19);
                addMessage(rtu, "1.0.0.8.2.255",        20);
                addMessage(rtu, "1.0.12.35.129.255",    21);
                
                addMessage(rtu, "0.0.96.7.5.255",       22);
                addMessage(rtu, "0.0.96.7.0.255",       23);
                
                addMessage(rtu, "1.0.12.32.0.255",      24);
                addMessage(rtu, "1.0.12.36.0.255",      25);
                addMessage(rtu, "1.0.12.38.0.255",      26);
                addMessage(rtu, "1.0.12.34.0.255",      27);
                addMessage(rtu, "1.0.1.7.0.255",        28);
                
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
