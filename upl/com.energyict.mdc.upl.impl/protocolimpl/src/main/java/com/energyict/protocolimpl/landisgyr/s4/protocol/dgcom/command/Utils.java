/*
 * Utils.java
 *
 * Created on 24 mei 2006, 17:02
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom.command;

import com.energyict.protocol.*;
import java.io.*;
import java.util.*;

/**
 *
 * @author Koen
 */
public class Utils {
    
    /** Creates a new instance of Utils */
    public Utils() {
    }
    
    
    static public Date getTimestampwwhhddYYDDMM(byte[] data, int offset, TimeZone timeZone) throws IOException {
        Calendar cal = ProtocolUtils.getCleanCalendar(timeZone);
        cal.set(Calendar.MINUTE,ProtocolUtils.BCD2hex(data[offset++]));
        cal.set(Calendar.HOUR_OF_DAY,ProtocolUtils.BCD2hex(data[offset++]));
        cal.set(Calendar.DAY_OF_WEEK,ProtocolUtils.BCD2hex(data[offset++]));
        int year = ProtocolUtils.BCD2hex(data[offset++]);
        cal.set(Calendar.YEAR,year>50?1900+year:2000+year);
        cal.set(Calendar.DAY_OF_MONTH,ProtocolUtils.BCD2hex(data[offset++]));
        cal.set(Calendar.MONTH,ProtocolUtils.BCD2hex(data[offset++])-1);
        return cal.getTime();
    }   
}
