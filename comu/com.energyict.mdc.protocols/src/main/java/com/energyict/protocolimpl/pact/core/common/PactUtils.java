/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * PactUtils.java
 *
 * Created on 11 maart 2004, 13:19
 */

package com.energyict.protocolimpl.pact.core.common;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 *
 * @author  Koen
 */
public class PactUtils {

    static public final int FRACTIONAL_DIGITS=6;

    /** Creates a new instance of PactUtils */
    public PactUtils() {
    }

    static public BigDecimal convert2BigDecimal(double val) {
        BigDecimal bd = new BigDecimal(val);
        bd = bd.setScale(FRACTIONAL_DIGITS,BigDecimal.ROUND_HALF_UP);
        return bd;
    }

    // Calendar parse methods

    static public Calendar getCalendar(int datePacs, int timePacs5Sec, TimeZone timeZone) {
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.clear();
        calendar.set(Calendar.YEAR,1988);
        calendar.set(Calendar.MONTH,0);
        calendar.set(Calendar.DATE,1);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.add(Calendar.YEAR, datePacs>>9);
        boolean isLeap = ((GregorianCalendar)calendar).isLeapYear(calendar.get(Calendar.YEAR));
        int days = datePacs&0x01FF;
        if ((!isLeap) && (days>=60)) days--;
        calendar.add(Calendar.DATE,days);
        calendar.add(Calendar.SECOND, timePacs5Sec*5);
        return calendar;
    }

    static public byte[] getPacsTimeDataFrame(Calendar calendar) {
        byte[] data = new  byte[8];
        int yearPart = calendar.get(Calendar.YEAR)-1988;
        int datePart = calendar.get(Calendar.DAY_OF_YEAR)-1;
        boolean isLeap = ((GregorianCalendar)calendar).isLeapYear(calendar.get(Calendar.YEAR));
        if ((datePart > 58) && !isLeap) datePart++;
        int datePacs = (yearPart<<9) | datePart;
        int timePacs5Sec = ((calendar.get(Calendar.HOUR_OF_DAY)*3600)+
                            (calendar.get(Calendar.MINUTE)*60)+
                             calendar.get(Calendar.SECOND))/5;

        data[0] = (byte)timePacs5Sec;
        data[1] = (byte)(timePacs5Sec >> 8);
        data[2] = (byte)datePacs;
        data[3] = (byte)(datePacs >> 8);

        return data;
    }

    static public Calendar getCalendar2(int days, int timeSeconds, TimeZone timeZone) {
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.clear();
        calendar.set(Calendar.YEAR,1988);
        calendar.set(Calendar.MONTH,0);
        calendar.set(Calendar.DATE,1);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.add(Calendar.DATE,days);
        calendar.add(Calendar.SECOND, timeSeconds);
        return calendar;
    }


    static public Calendar getCalendar(long intervalsSince1988, int intervalInMinutes, TimeZone timeZone) {
        return getCalendar(intervalsSince1988*(int)intervalInMinutes,timeZone);
    }

    static public Calendar getCalendar(long minutesSince1988, TimeZone timeZone) {
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.clear();
        calendar.set(Calendar.YEAR,1988);
        calendar.set(Calendar.MONTH,0);
        calendar.set(Calendar.DATE,1);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.add(Calendar.DATE,(int)(minutesSince1988/(24*60)));
        calendar.set(Calendar.HOUR_OF_DAY,(int)(minutesSince1988%(24*60))/60);
        calendar.set(Calendar.MINUTE,(int)(minutesSince1988%(24*60))%60);
        return calendar;
    }
}
