/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * TimeOffsetTable.java
 *
 * Created on 30 oktober 2005, 2:36
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
public class TimeOffsetTable extends AbstractTable {

    private Date dstTimeShift; // time moment of shift
    private int dstShiftValue; // 8 bit, shift in minutes
    private int timeZoneOffset; // signed 16 bit, in minutes

    /** Creates a new instance of TimeOffsetTable */
    public TimeOffsetTable(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(53));
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("TimeOffsetTable: dstTimeShift="+dstTimeShift+", dstShiftValue="+dstShiftValue+", timeZoneOffset="+timeZoneOffset);
        return strBuff.toString();
    }

    protected void parse(byte[] tableData) throws IOException {
        int offset=0;
        ActualTimeAndTOUTable atatt = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualTimeAndTOUTable();
        ConfigurationTable cfg = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        int dataOrder = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();

        setDstTimeShift(C12ParseUtils.getDateFromTime(tableData, offset, cfg.getTimeFormat(), getTableFactory().getC12ProtocolLink().getTimeZone(),dataOrder));
        offset+=C12ParseUtils.getTimeSize(cfg.getTimeFormat());
        setDstShiftValue(C12ParseUtils.getInt(tableData,offset++));
        if (atatt.getTimeTOU().isTimeZoneOffsetCapability()) {
             setTimeZoneOffset((int)C12ParseUtils.getExtendedLong(tableData,offset,2,dataOrder));
             offset+=2;
        }
    }


    public Date getDstTimeShift() {
        return dstTimeShift;
    }

    public void setDstTimeShift(Date dstTimeShift) {
        this.dstTimeShift = dstTimeShift;
    }

    public int getDstShiftValue() {
        return dstShiftValue;
    }

    public void setDstShiftValue(int dstShiftValue) {
        this.dstShiftValue = dstShiftValue;
    }

    public int getTimeZoneOffset() {
        return timeZoneOffset;
    }

    public void setTimeZoneOffset(int timeZoneOffset) {
        this.timeZoneOffset = timeZoneOffset;
    }
}