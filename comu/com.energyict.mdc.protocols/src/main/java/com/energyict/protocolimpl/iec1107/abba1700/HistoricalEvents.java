/*
 * HistoricalEvents.java
 *
 * Created on 15 juni 2004, 8:34
 */

package com.energyict.protocolimpl.iec1107.abba1700;

import java.util.*;
import java.io.*;
import com.energyict.cbo.*;
import com.energyict.protocol.*;
/**
 *
 * @author  Koen
 */
public class HistoricalEvents {
    
    HistoricalEventSet historicalEventSet;
    TimeZone timeZone;
    
    /** Creates a new instance of HistoricalEventSet */
    public HistoricalEvents(byte[] data, TimeZone timeZone) throws IOException {
        this.timeZone=timeZone;
        parse(data);
    }
    
    public String toString() {
       StringBuffer strBuff = new StringBuffer();
       strBuff.append(historicalEventSet.toString());
       return strBuff.toString();
    }
    
    private void parse(byte[] data) throws IOException {
       historicalEventSet = new HistoricalEventSet(ProtocolUtils.getSubArray2(data,0,66),timeZone);
    }
}
