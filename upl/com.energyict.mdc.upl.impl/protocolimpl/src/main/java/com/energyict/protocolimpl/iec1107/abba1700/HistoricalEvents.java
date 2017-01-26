/*
 * HistoricalEvents.java
 *
 * Created on 15 juni 2004, 8:34
 */

package com.energyict.protocolimpl.iec1107.abba1700;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.util.TimeZone;
/**
 *
 * @author  Koen
 */
public class HistoricalEvents {

    HistoricalEventSet historicalEventSet;
    TimeZone timeZone;

    /** Creates a new instance of HistoricalEventSet */
    public HistoricalEvents(byte[] data, TimeZone timeZone) throws ProtocolException {
        this.timeZone=timeZone;
        parse(data);
    }

    public String toString() {
       StringBuffer strBuff = new StringBuffer();
       strBuff.append(historicalEventSet.toString());
       return strBuff.toString();
    }

    private void parse(byte[] data) throws ProtocolException {
       historicalEventSet = new HistoricalEventSet(ProtocolUtils.getSubArray2(data,0,66),timeZone);
    }
}
