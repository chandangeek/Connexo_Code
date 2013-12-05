/*
 * RecordTemplate.java
 *
 * Created on 28 oktober 2005, 17:28
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.sentinel.tables;


import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class IntervalDataEntry {

    private long[] values; // 16 bit signed or unsigned for number of channels
    private int status; // 16 bit statusword


    /** Creates a new instance of RecordTemplate */
    public IntervalDataEntry(byte[] tableData,int offset,ManufacturerTableFactory manufacturerTableFactory) throws IOException {
//        ActualRegisterTable art = manufacturerTableFactory.getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable();
//        ActualTimeAndTOUTable atatt = manufacturerTableFactory.getC12ProtocolLink().getStandardTableFactory().getActualTimeAndTOUTable();
//        ConfigurationTable cfgt = manufacturerTableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
//        ActualLoadProfileTable alpt = manufacturerTableFactory.getC12ProtocolLink().getStandardTableFactory().getActualLoadProfileTable();
//        ActualLogTable alt = manufacturerTableFactory.getC12ProtocolLink().getStandardTableFactory().getActualLogTable();
//
        int dataOrder = manufacturerTableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();

        setStatus(C12ParseUtils.getInt(tableData,offset,2, dataOrder));
        offset+=2;

        setValues(new long[manufacturerTableFactory.getDataReadFactory().getCapabilitiesDataRead().getNumberOfLoadProfileChannels()]);
        for (int channel=0;channel<getValues().length;channel++) {
            if (manufacturerTableFactory.getDataReadFactory().getLoadProfilePreliminaryDataRead().getLoadProfileIntervalData() == 0) {
                getValues()[channel] = C12ParseUtils.getLong(tableData,offset,2, dataOrder);
            }
            else {
                getValues()[channel] = C12ParseUtils.getExtendedLong(tableData,offset,2, dataOrder);
            }
            offset+=2;
        }

    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("IntervalData:\n");
        strBuff.append("   status=0x"+Integer.toHexString(getStatus())+"\n");
        for (int i=0;i<getValues().length;i++) {
            strBuff.append("       values["+i+"]="+getValues()[i]+"\n");
        }
        return strBuff.toString();
    }

    static public int getSize(ManufacturerTableFactory manufacturerTableFactory) throws IOException {
//        ActualRegisterTable art = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable();
//        ActualTimeAndTOUTable atatt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualTimeAndTOUTable();
//        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
//        ActualLoadProfileTable alpt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getActualLoadProfileTable();
        return manufacturerTableFactory.getDataReadFactory().getCapabilitiesDataRead().getNumberOfLoadProfileChannels()*2+2;
    }

    public long[] getValues() {
        return values;
    }

    public void setValues(long[] values) {
        this.values = values;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
