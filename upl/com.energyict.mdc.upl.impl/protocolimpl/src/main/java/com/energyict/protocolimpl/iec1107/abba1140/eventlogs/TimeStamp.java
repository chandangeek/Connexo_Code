package com.energyict.protocolimpl.iec1107.abba1140.eventlogs;

import com.energyict.protocol.ProtocolException;
import com.energyict.protocol.ProtocolUtils;

import java.util.Date;
import java.util.TimeZone;

public class TimeStamp {

    private Date timeStamp;
	
    public TimeStamp(byte[] data, int offset, TimeZone timeZone) throws ProtocolException {
    	long shift = (long)ProtocolUtils.getIntLE(data,offset,4)&0x0FFFFFFFFL; offset+=4;
		if (shift > 0) timeStamp = ProtocolUtils.getCalendar(timeZone,shift).getTime();
    }
    
    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("TimeStampPair: timeStamp="+getTimeStamp()+"\n");
        return strBuff.toString();
    }
    
	public Date getTimeStamp() {
		return timeStamp;
	}

}
