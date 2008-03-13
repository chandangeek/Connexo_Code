/*
 * OctetString.java
 *
 * Created on 3 april 2003, 17:23
 */

package com.energyict.dlms;

import java.io.Serializable;
import java.util.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProtocolUtils;
/**
 *
 * @author  Koen
 */
public class OctetString implements Serializable {
    private byte[] array;

    public OctetString(byte[] data) {
       array = (byte[])data.clone();
    }
    public byte[] getArray() {
       return array;   
    }
    public String toString() {
        String str= new String(array);
        return str;
    }
    public ObisCode toObisCode() {
        StringBuffer strBuff = new StringBuffer();
        for (int i=0;i<array.length;i++) {
            if (i>0) strBuff.append(".");
            strBuff.append(Integer.toString((int)array[i]&0xFF));
        }
        return ObisCode.fromString(strBuff.toString());
    }
    
    public Date toUTCDate() {
        return toDate(TimeZone.getTimeZone("GMT"));
    }
    
    public Date toDate(TimeZone timeZone) {
        return toCalendar(timeZone).getTime();
    }
    
    public Calendar toCalendar(TimeZone timeZone) {
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.clear();
        int year = (int)ProtocolUtils.getShort(array,0)&0x0000FFFF;
        if (year != 0xFFFF) calendar.set(calendar.YEAR,year);
        int month = (int)array[2]&0xFF;
        if (month != 0xFF) calendar.set(calendar.MONTH,month-1);
        int date = (int)array[3]&0xFF;
        if (date != 0xFF) calendar.set(calendar.DAY_OF_MONTH,date);
        int hour = (int)array[5]&0xFF;
        if (hour != 0xFF) calendar.set(calendar.HOUR_OF_DAY,hour);
        int minute = (int)array[6]&0xFF;
        if (minute != 0xFF) calendar.set(calendar.MINUTE,minute);
        int seconds = (int)array[7]&0xFF;
        if (seconds != 0xFF) calendar.set(calendar.SECOND,seconds);
        return calendar;
    }
    
    public Date toDate(Date date, TimeZone timeZone) {
        Calendar calendar = Calendar.getInstance(timeZone);
        if (date != null)
           calendar.setTime(date);
        else
           calendar .clear();
        int year = (int)ProtocolUtils.getShort(array,0)&0x0000FFFF;
        if (year != 0xFFFF) calendar.set(calendar.YEAR,year);
        int month = (int)array[2]&0xFF;
        if (month != 0xFF) calendar.set(calendar.MONTH,month-1);
        int day = (int)array[3]&0xFF;
        if (day != 0xFF) calendar.set(calendar.DAY_OF_MONTH,day);
        int hour = (int)array[5]&0xFF;
        if (hour != 0xFF) calendar.set(calendar.HOUR_OF_DAY,hour);
        int minute = (int)array[6]&0xFF;
        if (minute != 0xFF) calendar.set(calendar.MINUTE,minute);
        int seconds = (int)array[7]&0xFF;
        if (seconds != 0xFF) calendar.set(calendar.SECOND,seconds);
        return calendar.getTime();
    }

} // class OctetString    
