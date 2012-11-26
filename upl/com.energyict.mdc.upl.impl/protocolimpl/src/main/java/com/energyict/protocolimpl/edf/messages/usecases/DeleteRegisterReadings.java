package com.energyict.protocolimpl.edf.messages.usecases;

import com.energyict.cbo.BusinessException;
import com.energyict.cpo.Environment;
import com.energyict.cpo.Transaction;
import com.energyict.mdw.amr.*;
import com.energyict.mdw.core.*;

import java.sql.SQLException;
import java.util.*;

public class DeleteRegisterReadings extends AbstractFolderAction implements Transaction {
    
    private Folder folder;
    
    public void execute(Folder folder)
        throws SQLException, BusinessException {
    
        this.folder = folder;
        
        Environment.getDefault().execute(this);
       
            
        
    }
    
    public Date lastWeek( ) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.WEEK_OF_YEAR, -1 );
        return calendar.getTime();
    }
    
    public Date lastMonth( ) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        return calendar.getTime();
    }
    
    public String getVersion() {
        return " $ Revision: 1 $ ";
    }

    public Object doExecute() throws BusinessException, SQLException {
        RtuRegisterReadingFactory factory = 
            MeteringWarehouse.getCurrent().getRtuRegisterReadingFactory();
        
        
        Iterator fi = folder.getRtus().iterator();
        
        while( fi.hasNext() ) {
        
            Device rtu = (Device)fi.next();
            
            
            rtu.updateLastReading( lastWeek() );
            rtu.updateLastLogbook( lastMonth() );
            
            Iterator ir = rtu.getRegisters().iterator();
            while( ir.hasNext() ) {

                RtuRegister rtuRegister = (RtuRegister) ir.next();
                rtuRegister.getReadingAfterOrEqual( new Date() );
            
                Iterator rrrI = factory.findByRegister( rtuRegister.getId() ).iterator();
                
                while( rrrI.hasNext() )     
                    ( (RtuRegisterReading) rrrI.next() ).delete();
                    
                
            }
            
            Iterator i = rtu.getChannels().iterator();
            while( i.hasNext() ) {
                
                Channel channel = (Channel) i.next();
                channel.removeAll(new Date(0), new Date());
                    
            }
            
            i = rtu.getEvents().iterator();
            while( i.hasNext() ) {
                
                RtuEvent event = (RtuEvent) i.next();
                event.delete();
                
            }
            
        }
        
        return null;
    }
    
}
