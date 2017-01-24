/*
 * EventLogSummariesType.java
 *
 * Created on 8 december 2006, 15:57
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author Koen
 */
public class EventLogSummariesType {

    private EventId event; // EVENT_ID, // 16 bit
    private Date firstOccurence; // DATE_AND_TIME,
    private Date lastOccurence; // DATE_AND_TIME,
    private long numOccurences; // UNSIGNED32,
    private int checksum; // UNSIGNED16

    /**
     * Creates a new instance of EventLogSummariesType
     */
    public EventLogSummariesType(byte[] data,int offset,TimeZone timeZone) throws IOException {
        setEvent(EventIdFactory.findEventId(ProtocolUtils.getInt(data,offset,2)));
        offset+=2;
        setFirstOccurence(Utils.getDateFromDateTime(data, offset, timeZone));
        offset+=Utils.getDateTimeSize();
        setLastOccurence(Utils.getDateFromDateTime(data, offset, timeZone));
        offset+=Utils.getDateTimeSize();
        setNumOccurences(ProtocolUtils.getInt(data,offset,4));
        offset+=4;
        setChecksum(ProtocolUtils.getInt(data,offset,2));
        offset+=2;
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("EventLogSummariesType:\n");
        strBuff.append("   checksum="+getChecksum()+"\n");
        strBuff.append("   event="+getEvent()+"\n");
        strBuff.append("   firstOccurence="+getFirstOccurence()+"\n");
        strBuff.append("   lastOccurence="+getLastOccurence()+"\n");
        strBuff.append("   numOccurences="+getNumOccurences()+"\n");
        return strBuff.toString();
    }

    static public int size() {
        return 20;
    }

    public EventId getEvent() {
        return event;
    }

    public void setEvent(EventId event) {
        this.event = event;
    }

    public Date getFirstOccurence() {
        return firstOccurence;
    }

    public void setFirstOccurence(Date firstOccurence) {
        this.firstOccurence = firstOccurence;
    }

    public Date getLastOccurence() {
        return lastOccurence;
    }

    public void setLastOccurence(Date lastOccurence) {
        this.lastOccurence = lastOccurence;
    }

    public long getNumOccurences() {
        return numOccurences;
    }

    public void setNumOccurences(long numOccurences) {
        this.numOccurences = numOccurences;
    }

    public int getChecksum() {
        return checksum;
    }

    public void setChecksum(int checksum) {
        this.checksum = checksum;
    }



}
