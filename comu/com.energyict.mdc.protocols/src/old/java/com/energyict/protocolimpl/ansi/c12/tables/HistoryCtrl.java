/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * HistoryCtrl.java
 *
 * Created on 17 november 2005, 16:22
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
public class HistoryCtrl {

    private byte[] stdEventsMonitoredFlags;
    private byte[] mfgEventsMonitoredFlags;
    private byte[] stdTablesMonitoredFlags;
    private byte[] mfgTablesMonitoredFlags;
    private byte[] stdProcMonitoredFlags;
    private byte[] mfgProcMonitoredFlags;

    /** Creates a new instance of HistoryCtrl */
    public HistoryCtrl(byte[] data,int offset,TableFactory tableFactory) throws IOException {
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        ActualLogTable alt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualLogTable();
        setStdEventsMonitoredFlags(ProtocolUtils.getSubArray2(data, offset, alt.getLog().getNrOfStdEvents()));
        offset+=alt.getLog().getNrOfStdEvents();
        setMfgEventsMonitoredFlags(ProtocolUtils.getSubArray2(data, offset, alt.getLog().getNrOfMfgEvents()));
        offset+=alt.getLog().getNrOfMfgEvents();
        setStdTablesMonitoredFlags(ProtocolUtils.getSubArray2(data, offset, cfgt.getDimStdTablesUsed()));
        offset+=cfgt.getDimStdTablesUsed();
        setMfgTablesMonitoredFlags(ProtocolUtils.getSubArray2(data, offset, cfgt.getDimMfgTablesUsed()));
        offset+=cfgt.getDimMfgTablesUsed();
        setStdProcMonitoredFlags(ProtocolUtils.getSubArray2(data, offset, cfgt.getDimStdProcUsed()));
        offset+=cfgt.getDimStdProcUsed();
        setMfgProcMonitoredFlags(ProtocolUtils.getSubArray2(data, offset, cfgt.getDimMfgProcUsed()));
        offset+=cfgt.getDimMfgProcUsed();
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("HistoryCtrl: \n");
        strBuff.append("stdEventsMonitoredFlags = "+ProtocolUtils.getResponseData(getStdEventsMonitoredFlags())+"\n");
        strBuff.append("mfgEventsMonitoredFlags = "+ProtocolUtils.getResponseData(getMfgEventsMonitoredFlags())+"\n");
        strBuff.append("stdTablesMonitoredFlags = "+ProtocolUtils.getResponseData(getStdTablesMonitoredFlags())+"\n");
        strBuff.append("mfgTablesMonitoredFlags = "+ProtocolUtils.getResponseData(getMfgTablesMonitoredFlags())+"\n");
        strBuff.append("stdProcMonitoredFlags = "+ProtocolUtils.getResponseData(getStdProcMonitoredFlags())+"\n");
        strBuff.append("mfgProcMonitoredFlags = "+ProtocolUtils.getResponseData(getMfgProcMonitoredFlags())+"\n");
        return strBuff.toString();
    }

    static public int getSize(TableFactory tableFactory) throws IOException {
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        ActualLogTable alt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualLogTable();
        int size=0;
        size += alt.getLog().getNrOfStdEvents();
        size+=alt.getLog().getNrOfMfgEvents();
        size+=cfgt.getDimStdTablesUsed();
        size+=cfgt.getDimMfgTablesUsed();
        size+=cfgt.getDimStdProcUsed();
        size+=cfgt.getDimMfgProcUsed();
        return size;
    }

    public byte[] getStdEventsMonitoredFlags() {
        return stdEventsMonitoredFlags;
    }

    public void setStdEventsMonitoredFlags(byte[] stdEventsMonitoredFlags) {
        this.stdEventsMonitoredFlags = stdEventsMonitoredFlags;
    }

    public byte[] getMfgEventsMonitoredFlags() {
        return mfgEventsMonitoredFlags;
    }

    public void setMfgEventsMonitoredFlags(byte[] mfgEventsMonitoredFlags) {
        this.mfgEventsMonitoredFlags = mfgEventsMonitoredFlags;
    }

    public byte[] getStdTablesMonitoredFlags() {
        return stdTablesMonitoredFlags;
    }

    public void setStdTablesMonitoredFlags(byte[] stdTablesMonitoredFlags) {
        this.stdTablesMonitoredFlags = stdTablesMonitoredFlags;
    }

    public byte[] getMfgTablesMonitoredFlags() {
        return mfgTablesMonitoredFlags;
    }

    public void setMfgTablesMonitoredFlags(byte[] mfgTablesMonitoredFlags) {
        this.mfgTablesMonitoredFlags = mfgTablesMonitoredFlags;
    }

    public byte[] getStdProcMonitoredFlags() {
        return stdProcMonitoredFlags;
    }

    public void setStdProcMonitoredFlags(byte[] stdProcMonitoredFlags) {
        this.stdProcMonitoredFlags = stdProcMonitoredFlags;
    }

    public byte[] getMfgProcMonitoredFlags() {
        return mfgProcMonitoredFlags;
    }

    public void setMfgProcMonitoredFlags(byte[] mfgProcMonitoredFlags) {
        this.mfgProcMonitoredFlags = mfgProcMonitoredFlags;
    }
}
