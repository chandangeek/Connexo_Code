/*
 * EventRecordType.java
 *
 * Created on 8 december 2006, 15:57
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author Koen
 */
public class EventRecordType {
    
    private Date eventTimeStamp;
    private EventId eventId;
    private int checkSum;
    private int id;
    
    
    /**
     * Creates a new instance of EventRecordType 
     */
    public EventRecordType(byte[] data,int offset, TimeZone timeZone) throws IOException {
        


        setEventTimeStamp(Utils.getDateFromDateTimeEventLogEntry(data,offset,timeZone));
        offset+=Utils.getDateTimeSize();
        int temp = data[1]&0x0F;
        setId(ProtocolUtils.getInt(data,offset, 1) | (temp<<8));
        setEventId(EventIdFactory.findEventId(getId()));
        offset++;
        setCheckSum(ProtocolUtils.getInt(data,offset, 2));
        offset+=2;
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("EventRecordType:\n");
        strBuff.append("   checkSum="+getCheckSum()+"\n");
        strBuff.append("   eventId="+getEventId()+"\n");
        strBuff.append("   id="+getId()+"\n");
        strBuff.append("   eventTimeStamp="+getEventTimeStamp()+"\n");
        return strBuff.toString();
    }
    
    static public int size() {
        return 9;
    }

    public Date getEventTimeStamp() {
        return eventTimeStamp;
    }

    public void setEventTimeStamp(Date eventTimeStamp) {
        this.eventTimeStamp = eventTimeStamp;
    }

    public EventId getEventId() {
        return eventId;
    }

    public void setEventId(EventId eventId) {
        this.eventId = eventId;
    }

    public int getCheckSum() {
        return checkSum;
    }

    public void setCheckSum(int checkSum) {
        this.checkSum = checkSum;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    

    
}
