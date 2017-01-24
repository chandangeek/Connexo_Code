/*
 * WeeklyScheduleRecord.java
 *
 * Created on 23 februari 2006, 14:36
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
public class WeeklyScheduleRecord {

    private DaysBitfield days;
    private Date startTime;
    private ControlBitfield controlBitfield;



    /** Creates a new instance of WeeklyScheduleRecord */
    public WeeklyScheduleRecord(byte[] data,int offset,TableFactory tableFactory) throws IOException {
        ActualRegisterTable art = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable();
        ActualTimeAndTOUTable atatt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualTimeAndTOUTable();
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        ActualLoadProfileTable alpt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualLoadProfileTable();
        ActualLogTable alt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualLogTable();

        days = new DaysBitfield(data,offset,tableFactory);
        offset+=DaysBitfield.getSize(tableFactory);
        startTime = C12ParseUtils.getDateFromTime(data,offset,cfgt.getTimeFormat(),tableFactory.getC12ProtocolLink().getTimeZone(),cfgt.getDataOrder());
        offset+=C12ParseUtils.getTimeSize(cfgt.getTimeFormat());
        controlBitfield = new ControlBitfield(data,offset,tableFactory);
        offset+=ControlBitfield.getSize(tableFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("WeeklyScheduleRecord:\n");
        strBuff.append("   controlBitfield="+getControlBitfield()+"\n");
        strBuff.append("   days="+getDays()+"\n");
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

    public DaysBitfield getDays() {
        return days;
    }

    public Date getStartTime() {
        return startTime;
    }

    public ControlBitfield getControlBitfield() {
        return controlBitfield;
    }
}
