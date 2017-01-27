/*
 * DateTimeBuilder.java
 *
 * Created on 23 maart 2006, 10:03
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk6.core;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


/**
 *
 * @author koen
 */
public class DateTimeBuilder {
    
    /** Creates a new instance of DateTimeBuilder */
    public DateTimeBuilder() {
    }
    
    public static Date getDateFromSecondsSince1996(TimeZone timeZone, int secondsSince1996) {
        Calendar cal = ProtocolUtils.getCleanCalendar(timeZone);
        cal.set(Calendar.YEAR,1996);
        cal.add(Calendar.SECOND,secondsSince1996);
        return cal.getTime();
    }  
    
    public static Date getDateFromDDMMYYHHMMSS(TimeZone timeZone, byte[] data) {
        return getDateFromDDMMYYHHMMSS(timeZone,data,0);
    }
    
    public static Date getDateFromDDMMYYHHMMSS(TimeZone timeZone, byte[] data, int offset) {
        Calendar cal = ProtocolUtils.getCleanCalendar(timeZone);
        cal.set(Calendar.DAY_OF_MONTH,data[offset]);
        cal.set(Calendar.MONTH,data[offset+1]-1);
        cal.set(Calendar.YEAR,data[offset+2]<50?data[offset+2]+2000:data[offset+2]+1900);
        cal.set(Calendar.HOUR_OF_DAY,data[offset+3]);
        cal.set(Calendar.MINUTE,data[offset+4]);
        cal.set(Calendar.SECOND,data[offset+5]);
        return cal.getTime();
    }  
    
    public static int getDateFromDDMMYYHHMMSSSize() {
       return 6;    
    }
    
    public static byte[] getDDMMYYHHMMSSDataFromDate(Date date,TimeZone timeZone) {
        Calendar cal = ProtocolUtils.getCleanCalendar(timeZone);
        cal.setTime(date);
        byte[] data = new byte[6];
        data[0]=(byte)cal.get(Calendar.DAY_OF_MONTH);
        data[1]=(byte)(cal.get(Calendar.MONTH)+1);
        data[2]=(byte)(cal.get(Calendar.YEAR)-2000);
        data[3]=(byte)cal.get(Calendar.HOUR_OF_DAY);
        data[4]=(byte)cal.get(Calendar.MINUTE);
        data[5]=(byte)cal.get(Calendar.SECOND);
        return data;
    }
            
    public static Date getDateFromHHMMSS(TimeZone timeZone, byte[] data) {
        Calendar cal = ProtocolUtils.getCleanCalendar(timeZone);
        cal.set(Calendar.HOUR_OF_DAY,data[0]);
        cal.set(Calendar.MINUTE,data[1]);
        cal.set(Calendar.SECOND,data[2]);
        return cal.getTime();
    }  
    
    public static Date getDateFromDDMMYY(TimeZone timeZone, byte[] data) {
        Calendar cal = ProtocolUtils.getCleanCalendar(timeZone);
        cal.set(Calendar.DAY_OF_MONTH,data[0]);
        cal.set(Calendar.MONTH,data[1]-1);
        cal.set(Calendar.YEAR,data[2]<50?data[2]+2000:data[2]+1900);
        return cal.getTime();
    }  
}
