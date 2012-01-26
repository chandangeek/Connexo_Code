package com.energyict.protocolimpl.edf.messages.usecases;

import com.energyict.cbo.BusinessException;
import com.energyict.mdw.core.*;
import com.energyict.mdw.shadow.RtuMessageShadow;
import com.energyict.protocolimpl.edf.messages.MessageContent;
import com.energyict.protocolimpl.edf.messages.MessageReadRegister;

import java.sql.SQLException;
import java.util.*;

public abstract class AbstractFolderAction implements FolderAction {
    
    public abstract void execute(Folder folder) throws SQLException, BusinessException;

    void addMessage( Rtu rtu, String obis, int ordinal ) 
        throws Exception {
        
        RtuMessageShadow shadow = new RtuMessageShadow( rtu.getId() );
        shadow.setReleaseDate( new Date() );
        
        MessageReadRegister mrr = new MessageReadRegister( obis );
        mrr.setOrdinal( ordinal );
        
        shadow.setContents( mrr.xmlEncode() );
        
        rtu.createMessage( shadow );
        
    }
    
    void createMessage( Rtu rtu, MessageContent content ) 
        throws Exception {
        
        RtuMessageShadow shadow = new RtuMessageShadow(rtu.getId());
        shadow.setReleaseDate( new Date() );
        
        shadow.setContents( content.xmlEncode() );
        rtu.createMessage(shadow);
        
    }
    
    public boolean isEnabled(Folder folder) {
        return true;
    }
    
    public void addProperties(Properties properties) { 
    }
    
    public List getOptionalKeys() {
        return new ArrayList();
    }
    
    public List getRequiredKeys() {
        return new ArrayList();
    }
    
}
