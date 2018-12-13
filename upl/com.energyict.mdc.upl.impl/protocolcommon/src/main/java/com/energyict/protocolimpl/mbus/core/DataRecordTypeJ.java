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
public class DataRecordTypeJ extends AbstractDataRecordType {
    
    private Calendar calendar;
    TimeZone timeZone;
    
    
    /**
     * Creates a new instance of DataRecordTypeJ 
     */
    public DataRecordTypeJ(TimeZone timeZone) {
        this.timeZone=timeZone;
    }
    
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("DataRecordTypeJ");
        strBuff.append("   calendar="+getCalendar().getTime()+"\n");
        return strBuff.toString();
    }  
    
    protected void doParse(byte[] data) throws IOException {
        setCalendar(Calendar.getInstance(timeZone));
        getCalendar().set(Calendar.SECOND,ProtocolUtils.getInt(data,0,1)&0x3F);
        getCalendar().set(Calendar.MINUTE,ProtocolUtils.getInt(data,1,1)&0x3F);
        getCalendar().set(Calendar.HOUR_OF_DAY,ProtocolUtils.getInt(data,2,1)&0x1F);
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

}
