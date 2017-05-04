/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * RecurringDateRecord.java
 *
 * Created on 23 februari 2006, 16:02
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;

import java.io.IOException;
import java.util.Date;

/**
 *
 * @author Koen
 */
public class RecurringDateRecord {

    private RDate originateDate;
    private Date startTime;
    private ControlBitfield controlBitfield;


    /** Creates a new instance of RecurringDateRecord */
    public RecurringDateRecord(byte[] tableData,int offset,TableFactory tableFactory) throws IOException {
        ActualRegisterTable art = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable();
        ActualTimeAndTOUTable atatt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualTimeAndTOUTable();
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        ActualLoadProfileTable alpt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualLoadProfileTable();
        ActualLogTable alt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualLogTable();
        originateDate = new RDate(tableData,offset,tableFactory);
        offset+=RDate.getSize(tableFactory);
        startTime = C12ParseUtils.getDateFromTime(tableData,offset,cfgt.getTimeFormat(),tableFactory.getC12ProtocolLink().getTimeZone(),cfgt.getDataOrder());
        offset+=C12ParseUtils.getTimeSize(cfgt.getTimeFormat());
        controlBitfield = new ControlBitfield(tableData,offset,tableFactory);
        offset+=ControlBitfield.getSize(tableFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("RecurringDateRecord:\n");
        strBuff.append("   controlBitfield="+getControlBitfield()+"\n");
        strBuff.append("   originateDate="+getOriginateDate()+"\n");
        strBuff.append("   startTime="+getStartTime()+"\n");
        return strBuff.toString();
    }

    static public int getSize(TableFactory tableFactory) throws IOException {
        ActualRegisterTable art = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable();
        ActualTimeAndTOUTable atatt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualTimeAndTOUTable();
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        ActualLoadProfileTable alpt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualLoadProfileTable();

        return 0;
    }

    public RDate getOriginateDate() {
        return originateDate;
    }

    public Date getStartTime() {
        return startTime;
    }

    public ControlBitfield getControlBitfield() {
        return controlBitfield;
    }
}