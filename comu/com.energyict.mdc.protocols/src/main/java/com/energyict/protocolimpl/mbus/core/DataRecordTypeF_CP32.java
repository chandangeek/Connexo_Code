/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

/**
 *
 * @author kvds
 */
public class DataRecordTypeF_CP32 extends AbstractDataRecordType {

    private Calendar calendar;
    TimeZone timeZone;
    private boolean inValid;
    private boolean summerTime;

    /**
     * Creates a new instance of DataRecordTypeF_CP32
     */
    public DataRecordTypeF_CP32(TimeZone timeZone) {
        this.timeZone=timeZone;
    }

    public String toString() {
        return "DataRecordTypeF_CP32:\n" +
                "   calendar=" + getCalendar().getTime() + "\n" +
                "   inValid=" + isInValid() + "\n" +
                "   summerTime=" + isSummerTime() + "\n";
    }

    protected void doParse(byte[] data) throws IOException {
        setCalendar(Calendar.getInstance(timeZone));
        getCalendar().set(Calendar.MINUTE,ProtocolUtils.getInt(data,0,1)&0x3F);
        getCalendar().set(Calendar.HOUR_OF_DAY,ProtocolUtils.getInt(data,1,1)&0x1F);
        getCalendar().set(Calendar.DATE,ProtocolUtils.getInt(data,2,1)&0x1F);
        getCalendar().set(Calendar.MONTH,(ProtocolUtils.getInt(data,3,1)&0x0F)-1);
        getCalendar().set(Calendar.YEAR,2000 + (((ProtocolUtils.getInt(data,3,1)&0xf0)>>1)|((ProtocolUtils.getInt(data,2,1)&0xe0)>>5)));
        //getCalendar().set(Calendar.YEAR,1900 + (((ProtocolUtils.getInt(data,2,1)&0xe0)>>1)|((ProtocolUtils.getInt(data,3,1)&0xf0)>>4)));

        setInValid((ProtocolUtils.getInt(data,0,1)&0x80)==0x80 ? true : false);
        setSummerTime((ProtocolUtils.getInt(data,1,1)&0x80)==0x80 ? true : false);
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

    public boolean isInValid() {
        return inValid;
    }

    public void setInValid(boolean inValid) {
        this.inValid = inValid;
    }

    public boolean isSummerTime() {
        return summerTime;
    }

    public void setSummerTime(boolean summerTime) {
        this.summerTime = summerTime;
    }
}
