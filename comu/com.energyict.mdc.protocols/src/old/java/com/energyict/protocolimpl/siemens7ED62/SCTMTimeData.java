/*
 * SCTMTimeData.java
 *
 * Created on 5 februari 2003, 16:48
 */



package com.energyict.protocolimpl.siemens7ED62;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author  Koen
 */
public class SCTMTimeData {

    int year,month,date,weekday,hour,minute,second;
    byte[] data;


    public SCTMTimeData(Calendar calendar) throws IOException {
           year = calendar.get(Calendar.YEAR);
           month = calendar.get(Calendar.MONTH);
           date = calendar.get(Calendar.DAY_OF_MONTH);
           weekday = calendar.get(Calendar.DAY_OF_WEEK);
           hour = calendar.get(Calendar.HOUR_OF_DAY);
           minute = calendar.get(Calendar.MINUTE);
           second = calendar.get(Calendar.SECOND);
    }

    /** Creates a new instance of SCTMTimeData */
    public SCTMTimeData(byte[] data) throws IOException {
        if (data.length == 14) {
           this.data = data;
           year = verifyYear(ProtocolUtils.parseIntFromStr(data, 0, 2));
           month = ProtocolUtils.parseIntFromStr(data, 2, 2)-1;
           date = ProtocolUtils.parseIntFromStr(data, 4, 2);
           weekday = ProtocolUtils.parseIntFromStr(data, 7, 1);
           hour = ProtocolUtils.parseIntFromStr(data, 8, 2);
           minute = ProtocolUtils.parseIntFromStr(data, 10, 2);
           second = ProtocolUtils.parseIntFromStr(data, 12, 2);
        }
        else if (data.length == 10) {
           this.data = data;
           year = verifyYear(ProtocolUtils.parseIntFromStr(data, 0, 2));
           month = ProtocolUtils.parseIntFromStr(data, 2, 2)-1;
           date = ProtocolUtils.parseIntFromStr(data, 4, 2);
           weekday = 0;
           hour = ProtocolUtils.parseIntFromStr(data, 6, 2);
           minute = ProtocolUtils.parseIntFromStr(data, 8, 2);
           second = 0;
        }
        else throw new IOException("SCTMTimeData, wrong datalength ("+data.length+")");
    }

    private int verifyYear(int rawyear) {
        if (rawyear <= 80) return rawyear+2000;
        else return rawyear+1900;
    }

    public byte[] getSETTIMEData() {
       data = new byte[12];
       data[0] = getDecimalCoded(year-2000)[0];
       data[1] = getDecimalCoded(year-2000)[1];
       data[2] = getDecimalCoded(month+1)[0];
       data[3] = getDecimalCoded(month+1)[1];
       data[4] = getDecimalCoded(date)[0];
       data[5] = getDecimalCoded(date)[1];
       int wd = weekday==1 ? 7 : weekday-1;
       data[6] = getDecimalCoded(wd)[0];
       data[7] = getDecimalCoded(wd)[1];
       data[8] = getDecimalCoded(hour)[0];
       data[9] = getDecimalCoded(hour)[1];
       data[10] = getDecimalCoded(minute)[0];
       data[11] = getDecimalCoded(minute)[1];
       return data;
    }
    public byte[] getBUFENQData() {
       data = new byte[10];
       data[0] = getDecimalCoded(year-2000)[0];
       data[1] = getDecimalCoded(year-2000)[1];
       data[2] = getDecimalCoded(month+1)[0];
       data[3] = getDecimalCoded(month+1)[1];
       data[4] = getDecimalCoded(date)[0];
       data[5] = getDecimalCoded(date)[1];
       data[6] = getDecimalCoded(hour)[0];
       data[7] = getDecimalCoded(hour)[1];
       data[8] = getDecimalCoded(minute)[0];
       data[9] = getDecimalCoded(minute)[1];
       return data;
    }

    private byte[] getDecimalCoded(int fieldval) {
        byte[] val = String.valueOf(fieldval).getBytes();
        byte[] codedval = new byte[2];
        if (val.length < 2) {
            codedval[0] = 0x30;
            codedval[1] = val[0];
        }
        else {
            codedval[0] = val[0];
            codedval[1] = val[1];
        }
        return codedval;
    }


    public Calendar getCalendar(TimeZone timeZone) {
       return doGetCalendar(timeZone);
    }

    public Date getDate(TimeZone timeZone) {
       return doGetCalendar(timeZone).getTime();
    }

    private Calendar doGetCalendar(TimeZone timeZone) {
       Calendar calendar = ProtocolUtils.getCleanCalendar(timeZone);
       calendar.set(Calendar.YEAR,year);
       calendar.set(Calendar.MONTH,month);
       calendar.set(Calendar.DAY_OF_MONTH,date);
       calendar.set(Calendar.HOUR_OF_DAY,hour);
       calendar.set(Calendar.MINUTE,minute);
       calendar.set(Calendar.SECOND,second);
       return calendar;
    }

}
