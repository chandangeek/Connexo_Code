/*
 * EventLog.java
 *
 * Created on 17 november 2005, 17:04
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
public class EventLog {

    private ListStatusBitfield eventFlags;
    private int nrOfValidentries; // 2 bytes
    private int lastEntryElement; // 2 bytes
    private long lastEntrySeqNr; // 4 bytes
    private int nrOfUnreadEntries; // 2 bytes
    private EventEntry[] entries;


    /** Creates a new instance of EventLog */
    public EventLog(byte[] data, int offset, TableFactory tableFactory, int nrOfLogEntries, boolean header) throws IOException {
        ActualLogTable alt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualLogTable();
        int dataOrder = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();

        if (header) {
            setEventFlags(new ListStatusBitfield(data, offset));
            offset+=ListStatusBitfield.getSize();
            setNrOfValidentries(C12ParseUtils.getInt(data,offset,2,dataOrder));
            offset+=2;
            setLastEntryElement(C12ParseUtils.getInt(data,offset,2,dataOrder));
            offset+=2;
            lastEntrySeqNr = C12ParseUtils.getLong(data,offset,4,dataOrder);
            offset+=4;
            nrOfUnreadEntries = C12ParseUtils.getInt(data,offset,2,dataOrder);
            offset+=2;
        }
        setEntries(new EventEntry[nrOfLogEntries]); //alt.getLog().getNrOfEventEntries()]);
        for (int i=0;i<getEntries().length;i++) {
            getEntries()[i] = new EventEntry(data, offset, tableFactory);
            offset+= EventEntry.getSize(tableFactory);
        }

    }
    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("EventLog: eventFlags="+eventFlags+", nrOfValidentries="+nrOfValidentries+", lastEntryElement="+lastEntryElement+", lastEntrySeqNr="+lastEntrySeqNr+", nrOfUnreadEntries="+nrOfUnreadEntries+"\n");
        for (int i=0;i<getEntries().length;i++) {
            strBuff.append("entries["+i+"]="+entries[i]+"\n");
        }
        return strBuff.toString();

    }

    static public int getSize(TableFactory tableFactory, boolean header) throws IOException {
        int size=header?11:0;
        ActualLogTable alt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualLogTable();
        size += alt.getLog().getNrOfEventEntries()*EventEntry.getSize(tableFactory);
        return size;
    }

    public ListStatusBitfield getEventFlags() {
        return eventFlags;
    }

    public void setEventFlags(ListStatusBitfield eventFlags) {
        this.eventFlags = eventFlags;
    }

    public int getNrOfValidentries() {
        return nrOfValidentries;
    }

    public void setNrOfValidentries(int nrOfValidentries) {
        this.nrOfValidentries = nrOfValidentries;
    }

    public int getLastEntryElement() {
        return lastEntryElement;
    }

    public void setLastEntryElement(int lastEntryElement) {
        this.lastEntryElement = lastEntryElement;
    }

    public long getLastEntrySeqNr() {
        return lastEntrySeqNr;
    }

    public void setLastEntrySeqNr(long lastEntrySeqNr) {
        this.lastEntrySeqNr = lastEntrySeqNr;
    }

    public int getNrOfUnreadEntries() {
        return nrOfUnreadEntries;
    }

    public void setNrOfUnreadEntries(int nrOfUnreadEntries) {
        this.nrOfUnreadEntries = nrOfUnreadEntries;
    }

    public EventEntry[] getEntries() {
        return entries;
    }

    public void setEntries(EventEntry[] entries) {
        this.entries = entries;
    }
}
