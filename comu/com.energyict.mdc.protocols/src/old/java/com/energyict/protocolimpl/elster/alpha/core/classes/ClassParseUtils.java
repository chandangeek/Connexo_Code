/*
 * ClassParseUtils.java
 *
 * Created on 20 juli 2005, 11:59
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.core.classes;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author koen
 */
public class ClassParseUtils {

    /** Creates a new instance of ClassParseUtils */
    public ClassParseUtils() {
    }



    static public Date getDate3(byte[] data, int offset, TimeZone timeZone) throws IOException {
        return getCalendar3(data,offset,timeZone).getTime();
    }
    static public Date getDate5(byte[] data, int offset, TimeZone timeZone) throws IOException {
        return getCalendar5(data,offset,timeZone).getTime();
    }
    static public Date getDate6(byte[] data, int offset, TimeZone timeZone) throws IOException {
        Calendar cal = getCalendar5(data,offset,timeZone);
        cal.set(Calendar.SECOND,ProtocolUtils.getBCD2Int(data,offset+5,1));
        return cal.getTime();
    }

    static public Calendar getCalendar3(byte[] data, int offset, TimeZone timeZone) throws IOException {
        Calendar cal = ProtocolUtils.getCleanCalendar(timeZone);
        int year = ProtocolUtils.getBCD2Int(data,offset,1);
        cal.set(Calendar.YEAR,year>50?1900+year:2000+year);
        cal.set(Calendar.MONTH,ProtocolUtils.getBCD2Int(data,offset+1,1)-1);
        cal.set(Calendar.DATE,ProtocolUtils.getBCD2Int(data,offset+2,1));
        return cal;
    }


    static public Calendar getCalendar5(byte[] data, int offset, TimeZone timeZone) throws IOException {
        Calendar cal = getCalendar3(data, offset, timeZone);
        cal.set(Calendar.HOUR_OF_DAY,ProtocolUtils.getBCD2Int(data,offset+3,1));
        cal.set(Calendar.MINUTE,ProtocolUtils.getBCD2Int(data,offset+4,1));
        return cal;
    }
}
