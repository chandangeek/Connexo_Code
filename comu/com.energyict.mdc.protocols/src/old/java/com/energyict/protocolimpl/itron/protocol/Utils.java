/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * Utils.java
 *
 * Created on 14 september 2006, 14:20
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.protocol;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.base.ParseUtils;

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


    static public Date buildDate(byte[] data, int offset, TimeZone timeZone) {
        Calendar cal = ProtocolUtils.getCleanCalendar(timeZone);
        cal.set(Calendar.MINUTE,data[offset]);
        cal.set(Calendar.HOUR_OF_DAY,data[offset+1]);
        cal.set(Calendar.DAY_OF_MONTH,data[offset+2]);
        cal.set(Calendar.MONTH,data[offset+3]-1);
        int year = data[offset+4]>50?data[offset+4]+1900:data[offset+4]+2000;
        cal.set(Calendar.YEAR,year);
        return cal.getTime();
    }

    static public Date buildDateYearFirst(byte[] data, int offset, TimeZone timeZone) {
        Calendar cal = ProtocolUtils.getCleanCalendar(timeZone);
        cal.set(Calendar.MINUTE,data[offset+4]);
        cal.set(Calendar.HOUR_OF_DAY,data[offset+3]);
        cal.set(Calendar.DAY_OF_MONTH,data[offset+2]);
        cal.set(Calendar.MONTH,data[offset+1]-1);
        int year = data[offset]>50?data[offset]+1900:data[offset]+2000;
        cal.set(Calendar.YEAR,year);
        return cal.getTime();
    }

    static public int buildDateSize() {
        return 5;
    }

    /*
     *  parse the Vectron TOO timestamp presentation MM DD HH mm and adjust the year using the current meter time...
     */
    static public Date buildTOODate(byte[] data, int offset, TimeZone timeZone, Calendar meterTime) throws IOException {
        Calendar timeUnderTest = ProtocolUtils.getCleanCalendar(timeZone);
        timeUnderTest.set(Calendar.MONTH,ProtocolUtils.BCD2hex(data[offset])-1);
        timeUnderTest.set(Calendar.DAY_OF_MONTH,ProtocolUtils.BCD2hex(data[offset+1]));
        timeUnderTest.set(Calendar.HOUR_OF_DAY,ProtocolUtils.BCD2hex(data[offset+2]));
        timeUnderTest.set(Calendar.MINUTE,ProtocolUtils.BCD2hex(data[offset+3]));
        ParseUtils.adjustYear2(meterTime, timeUnderTest);
        return timeUnderTest.getTime();
    }

}
