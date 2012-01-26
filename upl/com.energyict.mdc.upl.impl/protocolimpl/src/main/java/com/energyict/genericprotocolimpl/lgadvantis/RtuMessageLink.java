package com.energyict.genericprotocolimpl.lgadvantis;

import com.energyict.cbo.BusinessException;
import com.energyict.mdw.core.RtuMessage;
import com.energyict.protocolimpl.edf.messages.MessageContent;

import java.sql.SQLException;
import java.util.*;

public class RtuMessageLink {
    
    private MessageContent messageContent;
    private RtuMessage message;
    private List directPrimitives = new ArrayList();
    
    RtuMessageLink( MessageContent content ) {
        messageContent = content;
    }
    
    MessageContent getMessageContent() {
        return messageContent;
    }

    RtuMessage getMessage() {
        return message;
    }

    public List getDirectactions() {
        return directPrimitives;
    }

    RtuMessageLink setRtuMessage(RtuMessage rtuMessage) {
        this.message = rtuMessage;
        return this;
    }
    
    RtuMessageLink add( DirectPrimitive directPrimitive ) {
        directPrimitives.add(directPrimitive);
        return this;
    }
    
    RtuMessageLink addAll( List newDirectPrimitives ) {
        directPrimitives.addAll(newDirectPrimitives);
        return this;
    }
    
    Collection getDirectActions( ) {
        return directPrimitives;
    }
    
    boolean allCplStatusOk( ) {
        
        Iterator i = directPrimitives.iterator();
        while( i.hasNext() ) {

            DirectAction da = (DirectAction) i.next();

            if( ! da.isOk() )
                return false;
        
        }
        
        return true;
    }
    
    void setFailed( ) throws BusinessException, SQLException {
    	if (message != null)
    		message.setFailed();
    }
    
    void confirm( ) throws BusinessException, SQLException {
        if (message != null)
        	message.confirm();
    }
    
    public String toString() {
        StringBuffer result = new StringBuffer();
        
        result.append( "RtuMessageLink [" ); 
        
        result
            .append( "\n messageContent=" )
            .append( messageContent.getClass() );
        if (message != null){
        	result
        		.append( "\n rtuMessage=" )
        		.append(  message.getId() )
        		.append( "\n");
        }
            
        Iterator i = directPrimitives.iterator();
        while( i.hasNext() ) {
            result.append( "\t" + i.next() + "\n" );
        }
        
        result.append( "]\n");
        return result.toString();

    }
    
}
