/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * MassMemoryRecordBasePage.java
 *
 * Created on 12 september 2006, 16:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.fulcrum.basepages;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.itron.protocol.AbstractBasePage;
import com.energyict.protocolimpl.itron.protocol.BasePageDescriptor;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.TimeZone;

/**
 *
 * @author Koen
 */
public class MassMemoryRecordBasePage extends AbstractBasePage {



    private int recordNr=-1;
    private int address=-1;

    private long status;
    private Calendar calendar;
    private BigDecimal[] registerValues;
    private IntervalRecord[] intervalRecords;

    /** Creates a new instance of MassMemoryRecordBasePage */
    public MassMemoryRecordBasePage(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("MassMemoryRecordBasePage:\n");
        strBuff.append("   calendar="+getCalendar().getTime()+"\n");
        if (getRecordNr() != -1) strBuff.append("   recordNr="+getRecordNr()+"\n");
        if (getAddress() != -1) strBuff.append("   address=0x"+Integer.toHexString(getAddress())+"\n");
        for (int i=0;i<getIntervalRecords().length;i++) {
            strBuff.append("       intervalRecords["+i+"]="+getIntervalRecords()[i]+"\n");
        }
        for (int i=0;i<getRegisterValues().length;i++) {
            strBuff.append("       registerValues["+i+"]="+getRegisterValues()[i]+"\n");
        }
        strBuff.append("   status=0x"+Long.toHexString(getStatus())+"\n");
        return strBuff.toString();
    }

    protected BasePageDescriptor preparebuild() throws IOException {
        if (getRecordNr() != -1)
            return new BasePageDescriptor(((BasePagesFactory)((BasePagesFactory)getBasePagesFactory())).getMassMemoryBasePages().getLogicalMassMemoryStartAddress()+
                    getRecordNr()*((BasePagesFactory)((BasePagesFactory)getBasePagesFactory())).getMassMemoryBasePages().getMassMemoryRecordLength(),
                    ((BasePagesFactory)((BasePagesFactory)getBasePagesFactory())).getMassMemoryBasePages().getMassMemoryRecordLength());
        else if (getAddress() != -1)
            return new BasePageDescriptor(getAddress(),
                    ((BasePagesFactory)getBasePagesFactory()).getMassMemoryBasePages().getMassMemoryRecordLength());
        throw new IOException("MassMemoryRecordBasePage, preparebuild(), no address descriptor...");

    } // protected BasePageDescriptor preparebuild() throws IOException

    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        TimeZone tz = ((BasePagesFactory)getBasePagesFactory()).getProtocolLink().getTimeZone();
        if (!((BasePagesFactory)getBasePagesFactory()).getOperatingSetUpBasePage().isDstEnabled())
            tz = ProtocolUtils.getWinterTimeZone(tz);
        setCalendar(ProtocolUtils.getCleanCalendar(tz));
        getCalendar().set(Calendar.MONTH,data[0]-1);
        getCalendar().set(Calendar.DAY_OF_MONTH,data[1]);
        getCalendar().set(Calendar.HOUR_OF_DAY,data[2]);
        Calendar now = ProtocolUtils.getCalendar(tz);
        ParseUtils.adjustYear(now, getCalendar());
        //set fields again to deal with leap day issue
        getCalendar().set(Calendar.MONTH,data[0]-1);
        getCalendar().set(Calendar.DAY_OF_MONTH,data[1]);
        getCalendar().set(Calendar.HOUR_OF_DAY,data[2]);
        offset+=3;

        setStatus(ProtocolUtils.getLong(data,offset, 8));
        offset+=8;

        setRegisterValues(new BigDecimal[((BasePagesFactory)getBasePagesFactory()).getMassMemoryBasePages().getNumberOfChannels()]);
        for (int i=0;i<((BasePagesFactory)getBasePagesFactory()).getMassMemoryBasePages().getNumberOfChannels();i++) {
            getRegisterValues()[i] = new BigDecimal(""+Float.intBitsToFloat(ProtocolUtils.getInt(data,offset+2,4)));
            offset+=6;
        }


        int nibbleOffset = offset*2;

        setIntervalRecords(new IntervalRecord[60]);
        for (int i=0;i<60;i++) {
            getIntervalRecords()[i] = new IntervalRecord(data,nibbleOffset,((BasePagesFactory)getBasePagesFactory()).getMassMemoryBasePages().getNumberOfChannels());
            nibbleOffset+=IntervalRecord.size(((BasePagesFactory)getBasePagesFactory()).getMassMemoryBasePages().getNumberOfChannels());
        }

    }

    public long getStatus() {
        return status;
    }

    public void setStatus(long status) {
        this.status = status;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

    public BigDecimal[] getRegisterValues() {
        return registerValues;
    }

    public void setRegisterValues(BigDecimal[] registerValues) {
        this.registerValues = registerValues;
    }

    public IntervalRecord[] getIntervalRecords() {
        return intervalRecords;
    }

    public void setIntervalRecords(IntervalRecord[] intervalRecords) {
        this.intervalRecords = intervalRecords;
    }

    public int getRecordNr() {
        return recordNr;
    }

    public void setRecordNr(int recordNr) {
        this.recordNr = recordNr;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }


} // public class RealTimeBasePage extends AbstractBasePage
