/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * Utils.java
 *
 * Created on 8 december 2006, 16:48
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author Koen
 */
public class Utils {

    /** Creates a new instance of Utils */
    public Utils() {
    }

    static int getDateTimeExtendedSize() {
        return 8;
    }


    static Date getDateFromTOUDate(byte[] data,int offset, TimeZone timeZone) throws IOException {
        return getCalendarFromTOUDate(data, offset, timeZone).getTime();
    }

    static Calendar getCalendarFromTOUDate(byte[] data,int offset, TimeZone timeZone) throws IOException {
        Calendar cal = ProtocolUtils.getCleanCalendar(timeZone);

        int touDate = ProtocolUtils.getInt(data,offset,2); // temp = (year-1990)x31x12 + (monthx31) + (date-1)
        int years = touDate/(31*12);
        int months = (touDate%(31*12)) / 31;
        int date = ((touDate%(31*12)) % 31) + 1;

        cal.set(Calendar.YEAR,1990+years);
        cal.set(Calendar.MONTH,months);
        cal.set(Calendar.DATE,date);

        return cal;
    }

    static int getTOUDateSize() {
        return 2;
    }

    static Date getDateFromDateTimeExtended(byte[] data,int offset, TimeZone timeZone) throws IOException {
        return getCalendarFromDateTimeExtended(data, offset, timeZone).getTime();
    }

    static Calendar getCalendarFromDateTimeExtended(byte[] data,int offset, TimeZone timeZone) throws IOException {
        Calendar cal = ProtocolUtils.getCleanCalendar(timeZone);
        cal.set(Calendar.MONTH,ProtocolUtils.getInt(data,offset++, 1)-1);
        cal.set(Calendar.DAY_OF_MONTH,ProtocolUtils.getInt(data,offset++, 1));
        cal.set(Calendar.YEAR,ProtocolUtils.getInt(data,offset++, 1)+2000);
        cal.set(Calendar.HOUR_OF_DAY,ProtocolUtils.getInt(data,offset++, 1));
        cal.set(Calendar.MINUTE,ProtocolUtils.getInt(data,offset++, 1));
        cal.set(Calendar.SECOND,ProtocolUtils.getInt(data,offset++, 1));
        cal.set(Calendar.MILLISECOND,ProtocolUtils.getInt(data,offset, 2));
        return cal;
    }


    static public byte[] getDateTimeExtendedFromDate(Date date, TimeZone timeZone) {
        Calendar cal = ProtocolUtils.getCleanCalendar(timeZone);
        cal.setTime(date);
        byte[] data = new byte[8];
        data[0] = (byte)(cal.get(Calendar.MONTH)+1);
        data[1] = (byte)cal.get(Calendar.DAY_OF_MONTH);
        data[2] = (byte)(cal.get(Calendar.YEAR)-2000);
        data[3] = (byte)cal.get(Calendar.HOUR_OF_DAY);
        data[4] = (byte)cal.get(Calendar.MINUTE);
        data[5] = (byte)cal.get(Calendar.SECOND);
        data[6] = (byte)(cal.get(Calendar.MILLISECOND)>>8);
        data[7] = (byte)cal.get(Calendar.MILLISECOND);
        return data;
    }

    static int getDateTimeSize() {
        return 6;
    }

    static Date getDateFromDateTime(byte[] data,int offset, TimeZone timeZone) throws IOException {
        return getCalendarFromDateTime(data, offset, timeZone).getTime();
    }

    static Calendar getCalendarFromDateTime(byte[] data,int offset, TimeZone timeZone) throws IOException {
        Calendar cal = ProtocolUtils.getCleanCalendar(timeZone);
        cal.set(Calendar.MONTH,ProtocolUtils.getInt(data,offset++, 1)-1);
        cal.set(Calendar.DAY_OF_MONTH,ProtocolUtils.getInt(data,offset++, 1));
        cal.set(Calendar.YEAR,ProtocolUtils.getInt(data,offset++, 1)+2000);
        cal.set(Calendar.HOUR_OF_DAY,ProtocolUtils.getInt(data,offset++, 1));
        cal.set(Calendar.MINUTE,ProtocolUtils.getInt(data,offset++, 1));
        cal.set(Calendar.SECOND,ProtocolUtils.getInt(data,offset++, 1));
        return cal;
    }

    static Date getDateFromDateTimeEventLogEntry(byte[] data,int offset, TimeZone timeZone) throws IOException {
        return getCalendarFromDateTimeEventLogEntry(data, offset, timeZone).getTime();
    }

    static Calendar getCalendarFromDateTimeEventLogEntry(byte[] data,int offset, TimeZone timeZone) throws IOException {
        Calendar cal = ProtocolUtils.getCleanCalendar(timeZone);
        cal.set(Calendar.YEAR,ProtocolUtils.getInt(data,offset++, 1)+2000);
        int month = ProtocolUtils.getInt(data,offset++, 1) & 0x0F;
        cal.set(Calendar.MONTH,month-1);
        cal.set(Calendar.DAY_OF_MONTH,ProtocolUtils.getInt(data,offset++, 1));
        cal.set(Calendar.HOUR_OF_DAY,ProtocolUtils.getInt(data,offset++, 1));
        cal.set(Calendar.MINUTE,ProtocolUtils.getInt(data,offset++, 1));
        cal.set(Calendar.SECOND,ProtocolUtils.getInt(data,offset++, 1));
        return cal;
    }

    static int getTimeSize() {
        return 3;
    }

    static Date getDateFromTime(byte[] data,int offset, TimeZone timeZone) throws IOException {
        return getCalendarFromTime(data, offset, timeZone).getTime();
    }

    static Calendar getCalendarFromTime(byte[] data,int offset, TimeZone timeZone) throws IOException {
        Calendar cal = ProtocolUtils.getCleanCalendar(timeZone);
        cal.set(Calendar.HOUR_OF_DAY,ProtocolUtils.getInt(data,offset++, 1));
        cal.set(Calendar.MINUTE,ProtocolUtils.getInt(data,offset++, 1));
        cal.set(Calendar.SECOND,ProtocolUtils.getInt(data,offset++, 1));
        return cal;
    }

}
