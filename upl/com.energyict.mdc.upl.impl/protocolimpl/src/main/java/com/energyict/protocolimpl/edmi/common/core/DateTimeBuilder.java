package com.energyict.protocolimpl.edmi.common.core;

import com.energyict.protocol.ProtocolUtils;

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

    public static int getSecondsSince1996FromDate(TimeZone timeZone, Date date) {
        Calendar jan1996Cal = ProtocolUtils.getCleanCalendar(timeZone);
        jan1996Cal.set(Calendar.YEAR, 1996);
        jan1996Cal.set(Calendar.MONTH, 0);
        jan1996Cal.set(Calendar.DAY_OF_MONTH, 1);
        jan1996Cal.set(Calendar.HOUR, 0);
        jan1996Cal.set(Calendar.MINUTE, 0);
        jan1996Cal.set(Calendar.SECOND, 0);
        jan1996Cal.set(Calendar.MILLISECOND, 0);

        Calendar dateCal = ProtocolUtils.getCleanCalendar(timeZone);
        dateCal.setTime(date);
        return (int) ((dateCal.getTimeInMillis() - jan1996Cal.getTimeInMillis()) / 1000);
    }

    public static long getEpochMillisFor1Jan1996(TimeZone timeZone) {
        Calendar jan1996Cal = ProtocolUtils.getCleanCalendar(timeZone);
        jan1996Cal.set(Calendar.YEAR, 1996);
        jan1996Cal.set(Calendar.MONTH, 0);
        jan1996Cal.set(Calendar.DAY_OF_MONTH, 1);
        jan1996Cal.set(Calendar.HOUR, 0);
        jan1996Cal.set(Calendar.MINUTE, 0);
        jan1996Cal.set(Calendar.SECOND, 0);
        jan1996Cal.set(Calendar.MILLISECOND, 0);
        return jan1996Cal.getTimeInMillis();
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
