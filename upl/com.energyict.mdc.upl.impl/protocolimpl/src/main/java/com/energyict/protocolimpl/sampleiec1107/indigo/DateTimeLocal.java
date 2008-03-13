/*
 * DateTimeLocal.java
 *
 * Created on 7 juli 2004, 16:35
 */

package com.energyict.protocolimpl.sampleiec1107.indigo;

import java.util.*;
import java.io.*;

import com.energyict.protocol.ProtocolUtils;

/**
 *
 * @author  Koen
 */
public class DateTimeLocal extends DateTime {

    /** Creates a new instance of DateTimeLocal */
    public DateTimeLocal(int id,int size, LogicalAddressFactory laf) throws IOException {
        super(id,size,laf);
    }
    
    public String toString() {
        return "Datetime local="+getDate().toString();
    }
    
    public void parse(byte[] data, java.util.TimeZone timeZone) throws IOException {
       Calendar calendar=ProtocolUtils.getCleanCalendar(timeZone);
       parseDate(data,calendar);
    }
    
    protected byte[] buildData() {
        Calendar calendar=ProtocolUtils.getCleanCalendar(getLogicalAddressFactory().getIndigoPlus().getTimeZone());
        calendar.setTime(date);
        return (buildData(calendar));
    }    
    
}
