/*
 * MessagePair.java
 *
 * Created on 18 december 2007, 11:50
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.genericprotocolimpl.actarisplcc3g;

import com.energyict.mdw.core.OldDeviceMessage;
import com.energyict.protocolimpl.edf.messages.MessageContent;

/**
 *
 * @author kvds
 */
public class MessagePair implements Comparable {
    
    private OldDeviceMessage rtuMessage;
    private MessageContent messageContent;
    
    /**
     * Creates a new instance of MessagePair 
     */
    public MessagePair(OldDeviceMessage rtuMessage, MessageContent messageContent) {
        this.setRtuMessage(rtuMessage);
        this.setMessageContent(messageContent);
    }
    
    public String toString() {
        return "Device "+rtuMessage.getRtu().getName()+", message id "+messageContent.getOrdinal();
    }

    public OldDeviceMessage getRtuMessage() {
        return rtuMessage;
    }

    public void setRtuMessage(OldDeviceMessage rtuMessage) {
        this.rtuMessage = rtuMessage;
    }

    public MessageContent getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(MessageContent messageContent) {
        this.messageContent = messageContent;
    }
    
    /**
     * Compare another IntervalData to this IntervalData
     * @param o IntervalData
     * @return comparision result
     */
    public int compareTo(Object o) {
        MessagePair msg = (MessagePair)o;
        
        if (messageContent.getOrdinal()<msg.getMessageContent().getOrdinal()) return -1;
        
        if (messageContent.getOrdinal()>msg.getMessageContent().getOrdinal()) return 1;
        
        return 0;
        
    }    
}
