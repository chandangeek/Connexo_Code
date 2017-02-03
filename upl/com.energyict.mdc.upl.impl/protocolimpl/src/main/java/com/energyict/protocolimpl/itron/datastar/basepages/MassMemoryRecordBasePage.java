/*
 * MassMemoryRecordBasePage.java
 *
 * Created on 12 september 2006, 16:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.datastar.basepages;

import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.itron.protocol.AbstractBasePage;
import com.energyict.protocolimpl.itron.protocol.BasePageDescriptor;
import com.energyict.protocolimpl.utils.ProtocolUtils;

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
    private int offset=-1;
    
    private int statusFlags;
    private long outageFlags;
    private Calendar calendar;
    private IntervalRecord[] intervalRecords;
    private BigDecimal[] totals;
    private BigDecimal[] encoders; 
    
    private boolean onlyRegisters=false;
    
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
        if (getOffset() != -1) strBuff.append("   offset=0x"+Integer.toHexString(getOffset())+"\n");
        if (!isOnlyRegisters()) {
            for (int i=0;i<getIntervalRecords().length;i++) {
                strBuff.append("       intervalRecords["+i+"]="+getIntervalRecords()[i]+"\n");
            }
        }
        for (int i=0;i<getTotals().length;i++) {
            strBuff.append("       totals["+i+"]="+getTotals()[i]+"\n");
        }
        for (int i=0;i<getEncoders().length;i++) {
            strBuff.append("       encoders["+i+"]="+getEncoders()[i]+"\n");
        }
        strBuff.append("   statusFlags=0x"+Integer.toHexString(statusFlags)+"\n");
        strBuff.append("   outageFlags=0x"+Long.toHexString(outageFlags)+"\n");
        return strBuff.toString();
    }
    
    protected BasePageDescriptor preparebuild() throws IOException {
        if (getRecordNr() != -1)
            return new BasePageDescriptor(getBasePagesFactory().getMassMemoryBasePages().getMassMemoryStartOffset()+
                                          getRecordNr()* getBasePagesFactory().getMassMemoryBasePages().getMassMemoryRecordLength(),
                                          onlyRegisters? 35: getBasePagesFactory().getMassMemoryBasePages().getMassMemoryRecordLength());
        else if (getOffset() != -1)
            return new BasePageDescriptor(getOffset(),
                    onlyRegisters? 35:((BasePagesFactory)getBasePagesFactory()).getMassMemoryBasePages().getMassMemoryRecordLength());
        throw new IOException("MassMemoryRecordBasePage, preparebuild(), no offset descriptor...");
        
    } // protected BasePageDescriptor preparebuild() throws IOException
    
    protected void parse(byte[] data) throws IOException {
        
//System.out.println("KV_DEBUG> getRecordNr() "+getRecordNr()+", data.length="+data.length);
        
        int offset = 0;
        TimeZone tz = getBasePagesFactory().getProtocolLink().getTimeZone();
        if (!((BasePagesFactory)getBasePagesFactory()).getOperatingSetUpBasePage().isDstEnabled())
            tz = ProtocolUtils.getWinterTimeZone(tz);
        setCalendar(ProtocolUtils.getCleanCalendar(tz));
        getCalendar().set(Calendar.MONTH,ProtocolUtils.getInt(data,0,1)-1);
        getCalendar().set(Calendar.DAY_OF_MONTH,ProtocolUtils.getInt(data,1, 1));
        getCalendar().set(Calendar.HOUR_OF_DAY,ProtocolUtils.getInt(data,2,1)); 
        Calendar now = ProtocolUtils.getCalendar(tz);
        ParseUtils.adjustYear2(now, getCalendar());
        offset+=3;
        
        
        int nrOfChannels = ((BasePagesFactory)getBasePagesFactory()).getOperatingSetUpBasePage().getNrOfChannels();
        
        //int nrOfChannels = 4;
        
        setOutageFlags(ProtocolUtils.getLong(data,offset, 8));
        offset+=7;
        outageFlags >>= 4; // interval 1..56
        
        setStatusFlags(ProtocolUtils.getInt(data,offset, 1));
        offset++;
        outageFlags |= ((statusFlags>>4)&0x0F); // interval 57..60
        statusFlags &= 0x0f;

        
        totals = new BigDecimal[nrOfChannels]; 
        for (int i=0;i<getTotals().length;i++) {
            getTotals()[i] = BigDecimal.valueOf(ParseUtils.getBCD2Long(data, offset, 3)/10);
            offset+=3;
        }
        
        encoders = new BigDecimal[nrOfChannels];        
        for (int i=0;i<getEncoders().length;i++) {
            getEncoders()[i] = BigDecimal.valueOf(ParseUtils.getBCD2Long(data, offset, 3)/10);
            offset+=3;
        }
        
        if (!isOnlyRegisters()) {
            int nibbleOffset = offset*2;

            setIntervalRecords(new IntervalRecord[60]);
            for (int i=0;i<60;i++) {
                getIntervalRecords()[i] = new IntervalRecord(data,nibbleOffset,((BasePagesFactory)getBasePagesFactory()).getOperatingSetUpBasePage().getNrOfChannels());
                nibbleOffset+=IntervalRecord.size(((BasePagesFactory)getBasePagesFactory()).getOperatingSetUpBasePage().getNrOfChannels());
            }
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
    
    public int getOffset() {
        return offset;
    }
    
    public void setOffset(int offset) {
        this.offset = offset;
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

    public BigDecimal[] getTotals() {
        return totals;
    }

    public void setTotals(BigDecimal[] totals) {
        this.totals = totals;
    }

    public BigDecimal[] getEncoders() {
        return encoders;
    }

    public void setEncoders(BigDecimal[] encoders) {
        this.encoders = encoders;
    }

    public boolean isOnlyRegisters() {
        return onlyRegisters;
    }

    public void setOnlyRegisters(boolean onlyRegisters) {
        this.onlyRegisters = onlyRegisters;
    }


    
    
} // public class RealTimeBasePage extends AbstractBasePage
