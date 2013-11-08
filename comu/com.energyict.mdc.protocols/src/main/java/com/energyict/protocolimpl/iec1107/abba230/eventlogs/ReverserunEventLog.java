package com.energyict.protocolimpl.iec1107.abba230.eventlogs;

import java.io.IOException;
import java.util.*;

import com.energyict.protocol.*;

public class ReverserunEventLog extends AbstractEventLog {

	int mostRecent;
	int count;
	Date[] date = new Date[10];
	
	public ReverserunEventLog(TimeZone timeZone) throws IOException {
		super(timeZone);
	}
	
	public void parse(byte[] data) throws IOException {
		int offset=0;
		mostRecent = ProtocolUtils.getIntLE(data, offset++, 1);
		count = ProtocolUtils.getIntLE(data, offset, 2); offset+=2;
        for( int i = 0; i < 10; i ++ ) {
    		long shift = (long)ProtocolUtils.getInt(data,offset,4)&0xFFFFFFFFL; offset+=4;
    		if (shift>0) {
	    		date[i] = ProtocolUtils.getCalendar(timeZone,shift).getTime();
	            addMeterEvent(new MeterEvent(date[i], MeterEvent.REVERSE_RUN, "reverse run"+" ("+count+")"));
    		} 
        }

	}

}
