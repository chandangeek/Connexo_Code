/*
 * DateTimeGMT.java
 *
 * Created on 7 juli 2004, 12:20
 */

package com.energyict.protocolimpl.iec1107.indigo;

import java.util.*;
import java.io.*;

import com.energyict.protocol.ProtocolUtils;

/**
 *
 * @author  Koen
 */
public class DateTimeGMT extends DateTime {
    
    /** Creates a new instance of DateTimeGMT */
    public DateTimeGMT(int id,int size, LogicalAddressFactory laf) throws IOException {
        this(id,size,laf,null);
    }
    public DateTimeGMT(int id,int size, LogicalAddressFactory laf,Date date) throws IOException {
        super(id,size,laf);
        setDate(date);
    }
    
    public String toString() {
        return "Datetime gmt="+getDate().toString();
    }
    
    public void parse(byte[] data, java.util.TimeZone timeZone) throws IOException {
       Calendar calendar=ProtocolUtils.getCleanCalendar(TimeZone.getTimeZone("GMT"));
       parseDate(data,calendar);
    }
    
    
    protected byte[] buildData() {
        Calendar calendar=ProtocolUtils.getCleanCalendar(TimeZone.getTimeZone("GMT"));
        calendar.setTime(date);
        return (buildData(calendar));
    }    


}
