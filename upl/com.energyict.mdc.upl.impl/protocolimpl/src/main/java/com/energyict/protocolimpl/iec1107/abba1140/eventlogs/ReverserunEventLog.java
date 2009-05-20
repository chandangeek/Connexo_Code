package com.energyict.protocolimpl.iec1107.abba1140.eventlogs;

import java.io.IOException;
import java.util.*;

import com.energyict.protocol.*;

public class ReverserunEventLog extends AbstractEventLog {

	private static final int NUMBER_OF_EVENTS = 3;
	
	int mostRecent;
	int count;
	Date[] date = new Date[NUMBER_OF_EVENTS];
	
	public ReverserunEventLog(TimeZone timeZone) throws IOException {
		super(timeZone);
	}
	
	public void parse(byte[] data) throws IOException {
		int offset=0;
		mostRecent = ProtocolUtils.getIntLE(data, offset++, 1);
		count = ProtocolUtils.getIntLE(data, offset, 2); offset+=2;
        for( int i = 0; i < NUMBER_OF_EVENTS; i ++ ) {
    		long shift = (long)ProtocolUtils.getInt(data,offset,4)&0xFFFFFFFFL; offset+=4;
    		if (shift>0) {
	    		date[i] = ProtocolUtils.getCalendar(timeZone,shift).getTime();
	            addMeterEvent(new MeterEvent(date[i], MeterEvent.REVERSE_RUN, "reverse run"+" ("+count+")"));
    		} 
        }

	}

}
