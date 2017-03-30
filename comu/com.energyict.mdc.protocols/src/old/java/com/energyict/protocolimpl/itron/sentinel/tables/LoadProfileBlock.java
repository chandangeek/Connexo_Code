/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * LoadProfileBlock.java
 *
 * Created on 8 november 2006, 15:08
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.sentinel.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
/**
 *
 * @author Koen
 */
public class LoadProfileBlock {

    static private final int INTERVALS_PER_BLOCK=128;

    private Date blockEndTime;
    private BigDecimal[] values;
    private IntervalDataEntry[] intervaldatas=null;

    /** Creates a new instance of RecordTemplate */
    public LoadProfileBlock(byte[] tableData,int offset,ManufacturerTableFactory manufacturerTableFactory, boolean headerOnly) throws IOException {
        //ActualRegisterTable art = manufacturerTableFactory.getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable();
        //ActualTimeAndTOUTable atatt = manufacturerTableFactory.getC12ProtocolLink().getStandardTableFactory().getActualTimeAndTOUTable();
        //ConfigurationTable cfgt = manufacturerTableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        //ActualLoadProfileTable alpt = manufacturerTableFactory.getC12ProtocolLink().getStandardTableFactory().getActualLoadProfileTable();
        //ActualLogTable alt = manufacturerTableFactory.getC12ProtocolLink().getStandardTableFactory().getActualLogTable();
        int dataOrder = manufacturerTableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        long secondsSince01012000 = C12ParseUtils.getLong(tableData,offset, 4, dataOrder);
        offset+=4;
        setBlockEndTime(com.energyict.protocolimpl.itron.sentinel.logicalid.Utils.parseTimeStamp(secondsSince01012000, manufacturerTableFactory.getC12ProtocolLink().getTimeZone()));
        setValues(new BigDecimal[manufacturerTableFactory.getDataReadFactory().getCapabilitiesDataRead().getNumberOfLoadProfileChannels()]);
        for(int channel=0;channel<getValues().length;channel++) {
            getValues()[channel] = new BigDecimal(Double.longBitsToDouble(C12ParseUtils.getLong(tableData, offset, 8, dataOrder)));
            offset+=8;
        } // for(int channel=0;channel<values.length;channel++)
        if (!headerOnly) {
            setIntervaldatas(new IntervalDataEntry[INTERVALS_PER_BLOCK]);
            for (int interval = 0;interval<getIntervaldatas().length;interval++) {
                getIntervaldatas()[interval] = new IntervalDataEntry(tableData, offset, manufacturerTableFactory);
                offset += IntervalDataEntry.getSize(manufacturerTableFactory);
            }
        }

    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("LoadProfileBlock:\n");
        strBuff.append("   blockEndTime="+getBlockEndTime()+"\n");
        for (int i=0;i<getValues().length;i++) {
            strBuff.append("       values["+i+"]="+getValues()[i]+"\n");
        }
        if (getIntervaldatas()!=null) {
            for (int i=0;i<getIntervaldatas().length;i++) {
                strBuff.append("       intervaldatas["+i+"]="+getIntervaldatas()[i]+"\n");
            }
        }
        return strBuff.toString();
    }

    static public int getSize(ManufacturerTableFactory manufacturerTableFactory) throws IOException {
//        ActualRegisterTable art = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable();
//        ActualTimeAndTOUTable atatt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualTimeAndTOUTable();
//        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
//        ActualLoadProfileTable alpt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualLoadProfileTable();
        int nrOfChannels = manufacturerTableFactory.getDataReadFactory().getCapabilitiesDataRead().getNumberOfLoadProfileChannels();
        return 4+nrOfChannels*8+INTERVALS_PER_BLOCK*IntervalDataEntry.getSize(manufacturerTableFactory);
    }

    public Date getBlockEndTime() {
        return blockEndTime;
    }

    public void setBlockEndTime(Date blockEndTime) {
        this.blockEndTime = blockEndTime;
    }

    public BigDecimal[] getValues() {
        return values;
    }

    public void setValues(BigDecimal[] values) {
        this.values = values;
    }

    public IntervalDataEntry[] getIntervaldatas() {
        return intervaldatas;
    }

    public void setIntervaldatas(IntervalDataEntry[] intervaldatas) {
        this.intervaldatas = intervaldatas;
    }
}
