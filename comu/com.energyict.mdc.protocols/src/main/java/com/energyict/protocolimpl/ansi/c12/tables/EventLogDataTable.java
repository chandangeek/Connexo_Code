/*
 * EventLogDataTable.java
 *
 * Created on 17 november 2005, 16:30
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
public class EventLogDataTable extends AbstractTable {

    private EventLog eventLog;
    private int nrOfEventEntriesToRequest;
    private int eventEntryNrOffset;
    private boolean header;

    /** Creates a new instance of EventLogDataTable */
    public EventLogDataTable(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(76));
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("EventLogDataTable: eventLog="+getEventLog()+"\n");
        return strBuff.toString();
    }

    protected void prepareBuild() throws IOException {
        // override to provide extra functionality...
    }

    public void prepareGetEventEntryHeader(int eventEntryNr) throws IOException {
        this.eventEntryNrOffset = eventEntryNr;
        this.nrOfEventEntriesToRequest = 1;
        int eventEntrySize = EventEntry.getSize(getTableFactory());
        PartialReadInfo partialReadInfo = new PartialReadInfo(11+eventEntrySize*eventEntryNr,6);
        setHeader(false);
        setPartialReadInfo(partialReadInfo);
    }

    public void prepareGetEventEntries(int eventEntryNrOffset, int nrOfEventEntriesToRequest) throws IOException {
        this.eventEntryNrOffset = eventEntryNrOffset;
        this.nrOfEventEntriesToRequest = nrOfEventEntriesToRequest;
        int eventEntrySize = EventEntry.getSize(getTableFactory());
        PartialReadInfo partialReadInfo = new PartialReadInfo(11+eventEntrySize*eventEntryNrOffset,eventEntrySize*nrOfEventEntriesToRequest);
        setHeader(false);
        setPartialReadInfo(partialReadInfo);
    }

    public void prepareGetHeader() throws IOException {
        PartialReadInfo partialReadInfo = new PartialReadInfo(0,11);
        setHeader(true);
        setPartialReadInfo(partialReadInfo);
    }

    protected void parse(byte[] tableData) throws IOException {
        setEventLog(new EventLog(tableData, 0, getTableFactory(), getNrOfEventEntriesToRequest(), isHeader()));
    }

    public EventLog getEventLog() {
        return eventLog;
    }

    public void setEventLog(EventLog eventLog) {
        this.eventLog = eventLog;
    }

    public int getNrOfEventEntriesToRequest() {
        return nrOfEventEntriesToRequest;
    }

    public void setNrOfEventEntriesToRequest(int nrOfEventEntriesToRequest) {
        this.nrOfEventEntriesToRequest = nrOfEventEntriesToRequest;
    }

    public int getEventEntryNrOffset() {
        return eventEntryNrOffset;
    }

    public void setEventEntryNrOffset(int eventEntryNrOffset) {
        this.eventEntryNrOffset = eventEntryNrOffset;
    }

    public boolean isHeader() {
        return header;
    }

    public void setHeader(boolean header) {
        this.header = header;
    }

}
