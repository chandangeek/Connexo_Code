/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * HistoryEntry.java
 *
 * Created on 17 november 2005, 14:28
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
public class HistoryEntry {

    private Date historyTime;
    private int eventNumber; // 2 bytes
    private int historySeqNumber; // 2 bytes
    private int userId; // 2 bytes;
    private TableIDBBitfield historyCode;
    private int[] historyArgument;

    /** Creates a new instance of HistoryEntry */
    public HistoryEntry(byte[] data,int offset,TableFactory tableFactory) throws IOException {
        ActualLogTable alt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualLogTable();
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        int dataOrder = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();

        if (alt.getLog().isHistDateTimeFlag()) {
            if (tableFactory.getC12ProtocolLink().getManufacturer().getMeterProtocolClass().compareTo("com.energyict.protocolimpl.itron.sentinel.Sentinel")==0)
                setHistoryTime(C12ParseUtils.getDateFromLTimeAndAdjustForTimeZone(data, offset, cfgt.getTimeFormat(), tableFactory.getC12ProtocolLink().getTimeZone(),dataOrder));
            else
                setHistoryTime(C12ParseUtils.getDateFromLTime(data, offset, cfgt.getTimeFormat(), tableFactory.getC12ProtocolLink().getTimeZone(),dataOrder));
            offset += C12ParseUtils.getLTimeSize(cfgt.getTimeFormat());
        }
        if (alt.getLog().isEventNumberFlag()) {
            setEventNumber(C12ParseUtils.getInt(data,offset, 2,dataOrder));
            offset += 2;
        }
        if (alt.getLog().isHistSeqNumberFlag()) {
            setHistorySeqNumber(C12ParseUtils.getInt(data,offset, 2,dataOrder));
            offset += 2;
        }
        setUserId(C12ParseUtils.getInt(data,offset, 2,dataOrder));
        offset += 2;
        setHistoryCode(new TableIDBBitfield(data,offset, dataOrder));
        offset += TableIDBBitfield.getSize();
        setHistoryArgument(new int[alt.getLog().getHistDataLength()]);
        for (int i=0;i<getHistoryArgument().length;i++) {
            getHistoryArgument()[i] = C12ParseUtils.getInt(data,offset++);
        }


    }
    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("HistoryEntry: historyTime="+getHistoryTime()+
                       ", eventNumber="+getEventNumber()+
                       ", historySeqNumber="+getHistorySeqNumber()+
                       ", userId="+getUserId()+
                       ", historyCode="+getHistoryCode()+
                       ", historyArgument="+getHistoryArgument()+"\n");

        return strBuff.toString();

    }

    static public int getSize(TableFactory tableFactory) throws IOException {
        int size=0;
        ActualLogTable alt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualLogTable();
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        if (alt.getLog().isHistDateTimeFlag()) {
            size += C12ParseUtils.getLTimeSize(cfgt.getTimeFormat());
        }
        if (alt.getLog().isEventNumberFlag()) {
            size += 2;
        }
        if (alt.getLog().isHistSeqNumberFlag()) {
            size += 2;
        }
        size += 2;
        size += TableIDBBitfield.getSize();
        size += alt.getLog().getHistDataLength();
        return size;
    }

    public Date getHistoryTime() {
        return historyTime;
    }

    public void setHistoryTime(Date historyTime) {
        this.historyTime = historyTime;
    }

    public int getEventNumber() {
        return eventNumber;
    }

    public void setEventNumber(int eventNumber) {
        this.eventNumber = eventNumber;
    }

    public int getHistorySeqNumber() {
        return historySeqNumber;
    }

    public void setHistorySeqNumber(int historySeqNumber) {
        this.historySeqNumber = historySeqNumber;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public TableIDBBitfield getHistoryCode() {
        return historyCode;
    }

    public void setHistoryCode(TableIDBBitfield historyCode) {
        this.historyCode = historyCode;
    }

    public int[] getHistoryArgument() {
        return historyArgument;
    }

    public void setHistoryArgument(int[] historyArgument) {
        this.historyArgument = historyArgument;
    }
}
