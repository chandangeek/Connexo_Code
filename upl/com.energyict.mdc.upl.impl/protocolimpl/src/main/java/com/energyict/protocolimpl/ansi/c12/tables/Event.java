/*
 * Event.java
 *
 * Created on 26 oktober 2005, 10:59
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class Event {
    
    
    static public final int SIZE=6;
    private int statusBitfield; // 1 byte 
    private byte[] eventStorage = new byte[5]; // 5 bytes
    
    
    /** Creates a new instance of Event */
    public Event(byte[] data) throws IOException {
        statusBitfield = C12ParseUtils.getInt(data,0);
        eventStorage= ProtocolUtils.getSubArray2(data,1, eventStorage.length);
    }
    
    public String toString() {
        return "Event: statusBitfield="+getStatusBitfield()+", eventStorage="+ProtocolUtils.getResponseData(getEventStorage());
    }

    public int getStatusBitfield() {
        return statusBitfield;
    }

    public void setStatusBitfield(int statusBitfield) {
        this.statusBitfield = statusBitfield;
    }

    public byte[] getEventStorage() {
        return eventStorage;
    }

    public void setEventStorage(byte[] eventStorage) {
        this.eventStorage = eventStorage;
    }
    
}
