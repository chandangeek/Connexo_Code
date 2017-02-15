/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * DateType.java
 *
 * Created on 21 februari 2007, 9:22
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarandlms.common;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


/**
 *
 * @author Koen
 */
public class DateType {

    private Calendar calendar;
    private byte[] data;

    /** Creates a new instance of DateType */

    public DateType(Date date, TimeZone timeZone) {
        setCalendar(ProtocolUtils.getCleanCalendar(timeZone));
        getCalendar().setTime(date);
        setData(new byte[5]);
        int year = getCalendar().get(Calendar.YEAR)<2000?getCalendar().get(Calendar.YEAR)-1900:getCalendar().get(Calendar.YEAR)-2000;
        long temp = ((((long)year)<<33) & 0xFE00000000L);
        temp |= ((((long)getCalendar().get(Calendar.MONTH)+1)<<29) &       0x01E0000000L);
        temp |= ((((long)getCalendar().get(Calendar.DAY_OF_MONTH)<<24)) &  0x001F000000L);
        temp |= ((((long)getCalendar().get(Calendar.HOUR)<<19)) &          0x0000F80000L);
        temp |= ((((long)getCalendar().get(Calendar.MINUTE)<<13)) &        0x000007E000L);
        temp |= ((((long)getCalendar().get(Calendar.SECOND)<<7)) &         0x0000001F80L);
        temp |= ((((long)getCalendar().get(Calendar.MILLISECOND)/10)) &         0x000000007FL); // 100-ths of seconds
        getData()[0] = (byte)(temp>>32);
        getData()[1] = (byte)(temp>>24);
        getData()[2] = (byte)(temp>>16);
        getData()[3] = (byte)(temp>>8);
        getData()[4] = (byte)(temp);

        // skip the 100-ths of seconds
    }

    public String toString() {
        return "DateType= "+calendar.getTime()+"\n";
    }

    static public byte[] getDataFromLong(long lData) {
        byte[] data = new byte[5];
        data[0] = (byte)(lData>>32);
        data[1] = (byte)(lData>>24);
        data[2] = (byte)(lData>>16);
        data[3] = (byte)(lData>>8);
        data[4] = (byte)(lData);
        return data;
    }

    public DateType(long lData, TimeZone timeZone) throws IOException {
        this(getDataFromLong(lData),0, timeZone);
    }
    public DateType(byte[] data, int offset, TimeZone timeZone) throws IOException {

//System.out.println(ProtocolUtils.outputHexString(data));

       setCalendar(ProtocolUtils.getCleanCalendar(timeZone));
       int temp = ProtocolUtils.getInt(data,offset, 2);
       temp >>=9;
       temp &= 0x007F;
       getCalendar().set(Calendar.YEAR,temp>50?temp+1900:temp+2000);
       temp = ProtocolUtils.getInt(data,offset, 2);
       temp >>=5;
       temp &= 0x000f;
       getCalendar().set(Calendar.MONTH,temp-1);
       temp = ProtocolUtils.getInt(data,offset, 2);
       temp &= 0x001F;
       getCalendar().set(Calendar.DAY_OF_MONTH,temp);

       offset+=2;

       temp = ProtocolUtils.getInt(data,offset, 3);
       temp >>=19;
       temp&=0x00001F;
       getCalendar().set(Calendar.HOUR_OF_DAY,temp);
       temp = ProtocolUtils.getInt(data,offset, 3);
       temp >>=13;
       temp&=0x00003F;
       getCalendar().set(Calendar.MINUTE,temp);
       temp = ProtocolUtils.getInt(data,offset, 3);
       temp >>=7;
       temp&=0x00003F;
       getCalendar().set(Calendar.SECOND,temp);

       // skip the 100-ths of seconds
    }


    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

}
