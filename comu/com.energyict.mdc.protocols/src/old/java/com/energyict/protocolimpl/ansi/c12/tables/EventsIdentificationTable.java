/*
 * EventsIdentificationTable.java
 *
 * Created on 17 november 2005, 11:46
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class EventsIdentificationTable extends AbstractTable {

    private byte[] stdEventsSupported;
    private byte[] mfgEventsSupported;

    /** Creates a new instance of EventsIdentificationTable */
    public EventsIdentificationTable(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(72));
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("EventsIdentificationTable: \n");
        strBuff.append("stdEventsSupported = "+ProtocolUtils.getResponseData(getStdEventsSupported())+"\n");
        strBuff.append("mfgEventsSupported = "+ProtocolUtils.getResponseData(getMfgEventsSupported())+"\n");
        return strBuff.toString();
    }

    protected void parse(byte[] tableData) throws IOException {
        ActualLogTable alt = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualLogTable();
        int offset=0;
        setStdEventsSupported(ProtocolUtils.getSubArray2(tableData, offset, alt.getLog().getNrOfStdEvents()));
        offset+=alt.getLog().getNrOfStdEvents();
        setMfgEventsSupported(ProtocolUtils.getSubArray2(tableData, offset, alt.getLog().getNrOfMfgEvents()));
        offset+=alt.getLog().getNrOfMfgEvents();
    }

    public byte[] getStdEventsSupported() {
        return stdEventsSupported;
    }

    public void setStdEventsSupported(byte[] stdEventsSupported) {
        this.stdEventsSupported = stdEventsSupported;
    }

    public byte[] getMfgEventsSupported() {
        return mfgEventsSupported;
    }

    public void setMfgEventsSupported(byte[] mfgEventsSupported) {
        this.mfgEventsSupported = mfgEventsSupported;
    }
}
