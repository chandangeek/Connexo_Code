/*
 * EventEntry.java
 *
 * Created on 17 november 2005, 16:33
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;

import java.io.IOException;
import java.util.Date;

/**
 *
 * @author Koen
 */
public class EventEntry {

    private Date eventTime;
    private int eventNumber; // 2 bytes
    private int eventSeqNumber; // 2 bytes
    private int userId; // 2 bytes;
    private TableIDBBitfield eventCode;
    private int[] eventArgument;

    /** Creates a new instance of EventEntry */
    public EventEntry(byte[] data,int offset,TableFactory tableFactory) throws IOException {
        ActualLogTable alt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualLogTable();
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        int dataOrder = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();

        if (tableFactory.getC12ProtocolLink().getManufacturer().getMeterProtocolClass().compareTo("com.energyict.protocolimpl.itron.sentinel.Sentinel")==0)
            setEventTime(C12ParseUtils.getDateFromLTimeAndAdjustForTimeZone(data, offset, cfgt.getTimeFormat(), tableFactory.getC12ProtocolLink().getTimeZone(), dataOrder));
        else
            setEventTime(C12ParseUtils.getDateFromLTime(data, offset, cfgt.getTimeFormat(), tableFactory.getC12ProtocolLink().getTimeZone(), dataOrder));

        offset += C12ParseUtils.getLTimeSize(cfgt.getTimeFormat());

        if (data.length > C12ParseUtils.getLTimeSize(cfgt.getTimeFormat())) {
            if (alt.getLog().isEventNumberFlag()) {
                setEventNumber(C12ParseUtils.getInt(data,offset, 2, dataOrder));
                offset += 2;
            }

            if (tableFactory.getC12ProtocolLink().getManufacturer().getMeterProtocolClass().compareTo("com.energyict.protocolimpl.ge.kv.GEKV")==0) {
                // absorb, no sequence nr
            }
            else {
                setEventSeqNumber(C12ParseUtils.getInt(data,offset, 2, dataOrder));
                offset += 2;
            }

            setUserId(C12ParseUtils.getInt(data,offset, 2, dataOrder));
            offset += 2;
            setEventCode(new TableIDBBitfield(data,offset,dataOrder));
            offset += TableIDBBitfield.getSize();
            setEventArgument(new int[alt.getLog().getEventDataLength()]);
            for (int i=0;i<getEventArgument().length;i++) {
                getEventArgument()[i] = C12ParseUtils.getInt(data,offset++);
            }
        }


    }
    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("EventEntry: eventTime="+getEventTime()+
                       ", eventNumber="+getEventNumber()+
                       ", eventSeqNumber="+getEventSeqNumber()+
                       ", userId="+getUserId()+
                       ", eventCode="+getEventCode()+"\n");
        for (int i=0;i<getEventArgument().length;i++) {
            strBuff.append("eventArgument["+i+"]="+eventArgument[i]+"\n");
        }


        return strBuff.toString();

    }

    static public int getSize(TableFactory tableFactory) throws IOException {
        int size=0;
        ActualLogTable alt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualLogTable();
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        size += C12ParseUtils.getLTimeSize(cfgt.getTimeFormat());
        if (alt.getLog().isEventNumberFlag()) {
            size += 2;
        }
        if (tableFactory.getC12ProtocolLink().getManufacturer().getMeterProtocolClass().compareTo("com.energyict.protocolimpl.ge.kv.GEKV")==0) {
            // absorb, no sequence nr
        }
        else {
            size += 2;
        }
        size += 2;
        size += TableIDBBitfield.getSize();
        size += alt.getLog().getEventDataLength();
        return size;
    }

    public Date getEventTime() {
        return eventTime;
    }

    public void setEventTime(Date eventTime) {
        this.eventTime = eventTime;
    }

    public int getEventNumber() {
        return eventNumber;
    }

    public void setEventNumber(int eventNumber) {
        this.eventNumber = eventNumber;
    }

    public int getEventSeqNumber() {
        return eventSeqNumber;
    }

    public void setEventSeqNumber(int eventSeqNumber) {
        this.eventSeqNumber = eventSeqNumber;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public TableIDBBitfield getEventCode() {
        return eventCode;
    }

    public void setEventCode(TableIDBBitfield eventCode) {
        this.eventCode = eventCode;
    }

    public int[] getEventArgument() {
        return eventArgument;
    }

    public void setEventArgument(int[] eventArgument) {
        this.eventArgument = eventArgument;
    }
}
