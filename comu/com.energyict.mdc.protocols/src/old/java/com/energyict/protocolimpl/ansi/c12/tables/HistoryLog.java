/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * HistoryLog.java
 *
 * Created on 17 november 2005, 15:58
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
public class HistoryLog {

    private ListStatusBitfield historyFlags;

    private int nrOfValidentries; // 2 bytes
    private int lastEntryElement; // 2 bytes
    private long lastEntrySeqNr; // 4 bytes
    private int nrOfUnreadEntries; // 2 bytes
    private HistoryEntry[] entries;


    /** Creates a new instance of HistoryLog */
    public HistoryLog(byte[] data,int offset,TableFactory tableFactory, boolean header) throws IOException {
        ActualLogTable alt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualLogTable();
        setHistoryFlags(new ListStatusBitfield(data, offset));
        offset+=ListStatusBitfield.getSize();
        int dataOrder = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();

        setNrOfValidentries(C12ParseUtils.getInt(data,offset,2,dataOrder));
        offset+=2;
        setLastEntryElement(C12ParseUtils.getInt(data,offset,2,dataOrder));
        offset+=2;
        long lastEntrySeqNr = C12ParseUtils.getLong(data,offset,4,dataOrder);
        offset+=4;
        int nrOfUnreadEntries = C12ParseUtils.getInt(data,offset,2,dataOrder);
        offset+=2;

        if (!header) { // if only requesting header, do not
            if (getNrOfValidentries() > 0) {
                setEntries(new HistoryEntry[alt.getLog().getNrOfHistoryEntries()]);
                for (int i=0;i<getEntries().length;i++) {
                    getEntries()[i] = new HistoryEntry(data, offset, tableFactory);
                    offset+= HistoryEntry.getSize(tableFactory);
                }
            }
        }

    }
    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("HistoryLog: historyFlags="+getHistoryFlags()+", nrOfValidentries="+getNrOfValidentries()+", lastEntryElement="+getLastEntryElement()+", nrOfUnreadEntries="+getNrOfUnreadEntries()+"\n");
        if (entries!=null) {
            strBuff.append("   entries=\n");
            for (int i=0;i<getEntries().length;i++) {
                strBuff.append(getEntries()[i]+"\n");
            }
        }
        return strBuff.toString();

    }

    static public int getSize(TableFactory tableFactory) throws IOException {
        int size=11;
        ActualLogTable alt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualLogTable();
        size += alt.getLog().getNrOfHistoryEntries()*HistoryEntry.getSize(tableFactory);
        return size;
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

    public HistoryEntry[] getEntries() {
        return entries;
    }

    public void setEntries(HistoryEntry[] entries) {
        this.entries = entries;
    }

    public ListStatusBitfield getHistoryFlags() {
        return historyFlags;
    }

    public void setHistoryFlags(ListStatusBitfield historyFlags) {
        this.historyFlags = historyFlags;
    }
}
