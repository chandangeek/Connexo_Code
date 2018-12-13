/*
 * DataRecordTypeF_CP32.java
 *
 * Created on 3 oktober 2007, 11:32
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.mbus.core;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

/**
 *
 * @author kvds
 */
public class DataRecordTypeK extends AbstractDataRecordType {
    
    private Calendar calendarDSTBegin;
    private Calendar calendarDSTEnd;
    TimeZone timeZone;
    private boolean dstEnabled;
    private int deviationFromGMT;
    private int dstShiftInHours;
    
    /**
     * Creates a new instance of DataRecordTypeJ 
     */
    public DataRecordTypeK(TimeZone timeZone) {
        this.timeZone=timeZone;
    }
    
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("DataRecordTypeJ");
        strBuff.append("   calendarDSTBegin="+getCalendarDSTBegin().getTime()+"\n");
        strBuff.append("   calendarDSTEnd="+getCalendarDSTEnd().getTime()+"\n");
        strBuff.append("   dstEnabled="+dstEnabled+"\n");
        strBuff.append("   deviationFromGMT="+deviationFromGMT+"\n");
        strBuff.append("   dstShiftInHours="+dstShiftInHours+"\n");
        return strBuff.toString();
    }  
    
    protected void doParse(byte[] data) throws IOException {
        setCalendarDSTBegin(Calendar.getInstance(timeZone));
        setCalendarDSTEnd(Calendar.getInstance(timeZone));
        
        setDstEnabled((ProtocolUtils.getInt(data,1,1)&0x80) == 0x80);
        setDeviationFromGMT((((ProtocolUtils.getInt(data,1,1)&0xC0)>>3)|((ProtocolUtils.getInt(data,0,1)&0xe0)>>5)));
                
        getCalendarDSTBegin().set(Calendar.HOUR_OF_DAY,ProtocolUtils.getInt(data,0,1)&0x1F);
        getCalendarDSTBegin().set(Calendar.DATE,ProtocolUtils.getInt(data,1,1)&0x1F);
        getCalendarDSTBegin().set(Calendar.MONTH,ProtocolUtils.getInt(data,3,1)&0x0F);
        
        getCalendarDSTEnd().set(Calendar.DATE,ProtocolUtils.getInt(data,2,1)&0x1F);
        getCalendarDSTEnd().set(Calendar.MONTH,(ProtocolUtils.getInt(data,3,1)&0xF0)>>4);
        
        int temp = (ProtocolUtils.getInt(data,1,1)&0x40)==0x40 ? 1:-1;
        setDstShiftInHours(temp * ((ProtocolUtils.getInt(data,5,1)&0xC0)>>6));
        
    }

    public boolean isDstEnabled() {
        return dstEnabled;
    }

    public void setDstEnabled(boolean dstEnabled) {
        this.dstEnabled = dstEnabled;
    }

    public int getDeviationFromGMT() {
        return deviationFromGMT;
    }

    public void setDeviationFromGMT(int deviationFromGMT) {
        this.deviationFromGMT = deviationFromGMT;
    }

    public int getDstShiftInHours() {
        return dstShiftInHours;
    }

    public void setDstShiftInHours(int dstShiftInHours) {
        this.dstShiftInHours = dstShiftInHours;
    }

    public Calendar getCalendarDSTBegin() {
        return calendarDSTBegin;
    }

    public void setCalendarDSTBegin(Calendar calendarDSTBegin) {
        this.calendarDSTBegin = calendarDSTBegin;
    }

    public Calendar getCalendarDSTEnd() {
        return calendarDSTEnd;
    }

    public void setCalendarDSTEnd(Calendar calendarDSTEnd) {
        this.calendarDSTEnd = calendarDSTEnd;
    }



}
