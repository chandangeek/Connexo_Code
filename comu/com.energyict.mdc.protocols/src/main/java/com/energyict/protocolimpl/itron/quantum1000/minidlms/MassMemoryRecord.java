/*
 * MassMemoryRecord.java
 *
 * Created on 8 december 2006, 15:57
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.mdc.common.interval.IntervalStateBits;
import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author Koen
 */
public class MassMemoryRecord {

    private Date date;
    private int statusBits; // 16 bit
/*
    (0) = Short Interval
    (1) = Long Interval
    (2) = Test Mode
    (3) = Outage
    (5) = DST active
    (6) = clock error
    (7) = external eoi
    (8) = invalid data
    (9) = time adjustment
    (14) = TOU enable
    (15) = checksum Error
 */
   private int[] pulseCount; // 16 bit
   private int checkValue; // do not read... no explanation in protocoldoc and manufacturer tool does not read this value either


    /**
     * Creates a new instance of MassMemoryRecord
     */
    public MassMemoryRecord(byte[] data, int offset, TimeZone timeZone, int nrOfChannels) throws IOException {
        setDate(Utils.getDateFromDateTime(data,offset, timeZone));
        offset+=Utils.getDateTimeSize();
        setStatusBits(ProtocolUtils.getInt(data,offset, 2));
        offset+=2;
        setPulseCount(new int[nrOfChannels]);
        for(int i=0;i<nrOfChannels;i++) {
            getPulseCount()[i] = ProtocolUtils.getInt(data,offset, 2);
            offset+=2;
        }
    }

    public int getEIStatus() {
        int eiStatus=0;
        if (isShortInterval())
            eiStatus|= IntervalStateBits.SHORTLONG;
        if (isLongInterval())
            eiStatus|=IntervalStateBits.SHORTLONG;
        if (isTestMode())
            eiStatus|=IntervalStateBits.TEST;
        if (isOutage())
            eiStatus|=IntervalStateBits.POWERDOWN;
        if (isClockError())
            eiStatus|=IntervalStateBits.DEVICE_ERROR;
        if (isTimeAdjustment())
            eiStatus|=IntervalStateBits.SHORTLONG;
        if (isCheckSumError())
            eiStatus|=IntervalStateBits.CORRUPTED;

        return eiStatus;
    }

    public boolean isShortInterval() {
        return (getStatusBits() & 0x0001)==0x0001;
    }
    public boolean isLongInterval() {
        return (getStatusBits() & 0x0002)==0x0002;
    }
    public boolean isTestMode() {
        return (getStatusBits() & 0x0004)==0x0004;
    }
    public boolean isOutage() {
        return (getStatusBits() & 0x0008)==0x0008;
    }
    public boolean isDSTActive() {
        return (getStatusBits() & 0x0010)==0x0010;
    }
    public boolean isClockError() {
        return (getStatusBits() & 0x0020)==0x0020;
    }
    public boolean isExternalEOI() {
        return (getStatusBits() & 0x0040)==0x0040;
    }
    public boolean isInvalidData() {
        return (getStatusBits() & 0x0080)==0x0080;
    }
    public boolean isTimeAdjustment() {
        return (getStatusBits() & 0x0100)==0x0100;
    }
    public boolean isTOUEnable() {
        return (getStatusBits() & 0x4000)==0x4000;
    }
    public boolean isCheckSumError() {
        return (getStatusBits() & 0x8000)==0x8000;
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("MassMemoryRecord:\n");
        strBuff.append("   checkValue="+getCheckValue()+"\n");
        strBuff.append("   date="+getDate()+"\n");
        for (int i=0;i<getPulseCount().length;i++) {
            strBuff.append("       pulseCount["+i+"]="+getPulseCount()[i]+"\n");
        }
        strBuff.append("   statusBits="+getStatusBits()+"\n");
        return strBuff.toString();
    }

    static public int size(int nrOfChannels) {
        return nrOfChannels*2+8;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getStatusBits() {
        return statusBits;
    }

    public void setStatusBits(int statusBits) {
        this.statusBits = statusBits;
    }

    public int[] getPulseCount() {
        return pulseCount;
    }

    public void setPulseCount(int[] pulseCount) {
        this.pulseCount = pulseCount;
    }

    public int getCheckValue() {
        return checkValue;
    }

    public void setCheckValue(int checkValue) {
        this.checkValue = checkValue;
    }



}
