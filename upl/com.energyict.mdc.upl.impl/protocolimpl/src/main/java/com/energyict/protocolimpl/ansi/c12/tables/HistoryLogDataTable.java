/*
 * HistoryLogDataTable.java
 *
 * Created on 17 november 2005, 14:23
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocolimpl.ansi.c12.PartialReadInfo;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class HistoryLogDataTable extends AbstractTable {
    
    private HistoryLog historyLog;
    private boolean header;
    private int historyEntryNrOffset;
    private int nrOfHistoryEntriesToRequest;
        
    /** Creates a new instance of HistoryLogDataTable */
    public HistoryLogDataTable(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(74));
    }
    
    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("HistoryLogDataTable: historyLog="+getHistoryLog()+"\n");
        return strBuff.toString();
    }
    
    public void prepareGetHistoryEntries(int historyEntryNrOffset, int nrOfHistoryEntriesToRequest) throws IOException {
        this.historyEntryNrOffset = historyEntryNrOffset;
        this.nrOfHistoryEntriesToRequest = nrOfHistoryEntriesToRequest;
        int historyEntrySize = HistoryEntry.getSize(getTableFactory());
        PartialReadInfo partialReadInfo = new PartialReadInfo(11+historyEntrySize*historyEntryNrOffset,historyEntrySize*nrOfHistoryEntriesToRequest);
        setHeader(false);
        setPartialReadInfo(partialReadInfo);
    }
    
    public void prepareGetHeader() throws IOException {
        PartialReadInfo partialReadInfo = new PartialReadInfo(0,11);
        setHeader(true);
        setPartialReadInfo(partialReadInfo);
    }
    
    public void prepareGetHistoryEntryHeader(int historyEntryNr) throws IOException {
        this.historyEntryNrOffset = historyEntryNr;
        this.setNrOfHistoryEntriesToRequest(1);
        int historyEntrySize = HistoryEntry.getSize(getTableFactory());
        PartialReadInfo partialReadInfo = new PartialReadInfo(11+historyEntrySize*historyEntryNr,6);
        setHeader(false);
        setPartialReadInfo(partialReadInfo);
    }
    
    protected void parse(byte[] tableData) throws IOException {
        if (nrOfHistoryEntriesToRequest != 0) {
            setHistoryLog(new HistoryLog(tableData, 0, tableFactory, nrOfHistoryEntriesToRequest));
        } else {
            // Full read
            setHistoryLog(new HistoryLog(tableData, 0, tableFactory, header));
        }
    }         

    public HistoryLog getHistoryLog() {
        return historyLog;
    }

    public void setHistoryLog(HistoryLog historyLog) {
        this.historyLog = historyLog;
    }

    public boolean isHeader() {
        return header;
    }

    public void setHeader(boolean header) {
        this.header = header;
    }

    public int getHistoryEntryNrOffset() {
        return historyEntryNrOffset;
    }

    public void setHistoryEntryNrOffset(int historyEntryNrOffset) {
        this.historyEntryNrOffset = historyEntryNrOffset;
    }

    public int getNrOfHistoryEntriesToRequest() {
        return nrOfHistoryEntriesToRequest;
    }

    public void setNrOfHistoryEntriesToRequest(int nrOfHistoryEntriesToRequest) {
        this.nrOfHistoryEntriesToRequest = nrOfHistoryEntriesToRequest;
    }
}
