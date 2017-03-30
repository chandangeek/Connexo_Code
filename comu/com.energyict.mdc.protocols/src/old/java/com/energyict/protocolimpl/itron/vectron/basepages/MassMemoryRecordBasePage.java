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

package com.energyict.protocolimpl.itron.vectron.basepages;

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

    private int statusFlags;
    private long outageFlags;
    private Calendar calendar;
    private IntervalRecord[] intervalRecords;
    private BigDecimal[] registerValues;

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
        strBuff.append("   statusFlags=0x"+Integer.toHexString(statusFlags)+"\n");
        strBuff.append("   outageFlags=0x"+Long.toHexString(outageFlags)+"\n");
        return strBuff.toString();
    }

    protected BasePageDescriptor preparebuild() throws IOException {
        if (getRecordNr() != -1)
            return new BasePageDescriptor(((BasePagesFactory)((BasePagesFactory)getBasePagesFactory())).getMassMemoryBasePages().getLogicalStartAddress()+
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
        getCalendar().set(Calendar.MONTH,ProtocolUtils.BCD2hex(data[0])-1);
        getCalendar().set(Calendar.DAY_OF_MONTH,ProtocolUtils.BCD2hex(data[1]));
        getCalendar().set(Calendar.HOUR_OF_DAY,ProtocolUtils.BCD2hex(data[2]));
        Calendar now = ProtocolUtils.getCalendar(tz);
        ParseUtils.adjustYear2(now, getCalendar());
        offset+=3;

        setOutageFlags(ProtocolUtils.getLong(data,offset, 8));
        offset+=7;
        outageFlags >>= 4;

        setStatusFlags(ProtocolUtils.getInt(data,offset, 2));
        offset+=2;
        statusFlags &= 0x0fff;

        setRegisterValues(new BigDecimal[((BasePagesFactory)getBasePagesFactory()).getMassMemoryBasePages().getNrOfChannels()]);
        for (int i=0;i<((BasePagesFactory)getBasePagesFactory()).getMassMemoryBasePages().getNrOfChannels();i++) {
            getRegisterValues()[i] = new BigDecimal(""+Float.intBitsToFloat(ProtocolUtils.getInt(data,offset+2,4)));
            offset+=6;
        }


        int nibbleOffset = offset*2;

        setIntervalRecords(new IntervalRecord[60]);
        for (int i=0;i<60;i++) {
            getIntervalRecords()[i] = new IntervalRecord(data,nibbleOffset,((BasePagesFactory)getBasePagesFactory()).getMassMemoryBasePages().getNrOfChannels());
            nibbleOffset+=IntervalRecord.size(((BasePagesFactory)getBasePagesFactory()).getMassMemoryBasePages().getNrOfChannels());
        }

    }

    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
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

    public int getStatusFlags() {
        return statusFlags;
    }

    public void setStatusFlags(int statusFlags) {
        this.statusFlags = statusFlags;
    }

    public long getOutageFlags() {
        return outageFlags;
    }

    public void setOutageFlags(long outageFlags) {
        this.outageFlags = outageFlags;
    }

    public IntervalRecord[] getIntervalRecords() {
        return intervalRecords;
    }

    public void setIntervalRecords(IntervalRecord[] intervalRecords) {
        this.intervalRecords = intervalRecords;
    }

    public BigDecimal[] getRegisterValues() {
        return registerValues;
    }

    public void setRegisterValues(BigDecimal[] registerValues) {
        this.registerValues = registerValues;
    }


} // public class RealTimeBasePage extends AbstractBasePage
