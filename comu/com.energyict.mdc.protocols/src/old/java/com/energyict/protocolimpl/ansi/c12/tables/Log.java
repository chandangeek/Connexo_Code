/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * Log.java
 *
 * Created on 17 november 2005, 11:22
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class Log {

    private int logFlags; // 8 bit
    private boolean eventNumberFlag; // bit 0
    private boolean histDateTimeFlag; // bit 1
    private boolean histSeqNumberFlag; // bit 2
    private boolean histInhibitOvfFlag; // bit 3
    private boolean eventInhibitOvfFlag; // bit 4

    private int nrOfStdEvents; // 1 byte
    private int nrOfMfgEvents; // 1 byte
    private int histDataLength; // 1 byte
    private int eventDataLength; // 1 byte
    private int nrOfHistoryEntries; // 2 bytes
    private int nrOfEventEntries; // 2 bytes

    /** Creates a new instance of Log */
    public Log(byte[] data,int offset,TableFactory tableFactory) throws IOException {
        int dataOrder = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();

        setLogFlags(C12ParseUtils.getInt(data, offset++));
        setEventNumberFlag((getLogFlags() & 0x01) == 0x01);
        setHistDateTimeFlag((getLogFlags() & 0x02) == 0x02);
        setHistSeqNumberFlag((getLogFlags() & 0x04) == 0x04);
        setHistInhibitOvfFlag((getLogFlags() & 0x08) == 0x08);
        setEventInhibitOvfFlag((getLogFlags() & 0x10) == 0x10);

        setNrOfStdEvents(C12ParseUtils.getInt(data, offset++));
        setNrOfMfgEvents(C12ParseUtils.getInt(data, offset++));
        setHistDataLength(C12ParseUtils.getInt(data, offset++));
        setEventDataLength(C12ParseUtils.getInt(data, offset++));
        setNrOfHistoryEntries(C12ParseUtils.getInt(data, offset, 2,dataOrder));
        offset+=2;
        setNrOfEventEntries(C12ParseUtils.getInt(data, offset, 2,dataOrder));
        offset+=2;
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("Log: logFlags=0x"+Integer.toHexString(logFlags)+", nrOfStdEvents="+nrOfStdEvents+", nrOfMfgEvents="+nrOfMfgEvents+", histDataLength="+histDataLength+", eventDataLength="+eventDataLength+", nrOfHistoryEntries="+nrOfHistoryEntries+", nrOfEventEntries="+nrOfEventEntries+"\n");
        return strBuff.toString();
    }

    static public int getSize(TableFactory tableFactory) throws IOException {
        return 9;
    }

    public int getLogFlags() {
        return logFlags;
    }

    public void setLogFlags(int logFlags) {
        this.logFlags = logFlags;
    }

    public boolean isEventNumberFlag() {
        return eventNumberFlag;
    }

    public void setEventNumberFlag(boolean eventNumberFlag) {
        this.eventNumberFlag = eventNumberFlag;
    }

    public boolean isHistDateTimeFlag() {
        return histDateTimeFlag;
    }

    public void setHistDateTimeFlag(boolean histDateTimeFlag) {
        this.histDateTimeFlag = histDateTimeFlag;
    }

    public boolean isHistSeqNumberFlag() {
        return histSeqNumberFlag;
    }

    public void setHistSeqNumberFlag(boolean histSeqNumberFlag) {
        this.histSeqNumberFlag = histSeqNumberFlag;
    }

    public boolean isHistInhibitOvfFlag() {
        return histInhibitOvfFlag;
    }

    public void setHistInhibitOvfFlag(boolean histInhibitOvfFlag) {
        this.histInhibitOvfFlag = histInhibitOvfFlag;
    }

    public boolean isEventInhibitOvfFlag() {
        return eventInhibitOvfFlag;
    }

    public void setEventInhibitOvfFlag(boolean eventInhibitOvfFlag) {
        this.eventInhibitOvfFlag = eventInhibitOvfFlag;
    }

    public int getNrOfStdEvents() {
        return nrOfStdEvents;
    }

    public void setNrOfStdEvents(int nrOfStdEvents) {
        this.nrOfStdEvents = nrOfStdEvents;
    }

    public int getNrOfMfgEvents() {
        return nrOfMfgEvents;
    }

    public void setNrOfMfgEvents(int nrOfMfgEvents) {
        this.nrOfMfgEvents = nrOfMfgEvents;
    }

    public int getHistDataLength() {
        return histDataLength;
    }

    public void setHistDataLength(int histDataLength) {
        this.histDataLength = histDataLength;
    }

    public int getEventDataLength() {
        return eventDataLength;
    }

    public void setEventDataLength(int eventDataLength) {
        this.eventDataLength = eventDataLength;
    }

    public int getNrOfHistoryEntries() {
        return nrOfHistoryEntries;
    }

    public void setNrOfHistoryEntries(int nrOfHistoryEntries) {
        this.nrOfHistoryEntries = nrOfHistoryEntries;
    }

    public int getNrOfEventEntries() {
        return nrOfEventEntries;
    }

    public void setNrOfEventEntries(int nrOfEventEntries) {
        this.nrOfEventEntries = nrOfEventEntries;
    }
}
