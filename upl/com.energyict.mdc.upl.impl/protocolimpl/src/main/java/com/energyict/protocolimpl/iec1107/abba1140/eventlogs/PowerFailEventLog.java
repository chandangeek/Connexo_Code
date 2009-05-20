package com.energyict.protocolimpl.iec1107.abba1140.eventlogs;

import java.io.IOException;
import java.util.*;

import com.energyict.protocol.*;

public class PowerFailEventLog extends AbstractEventLog {
	
	private static final int NUMBER_OF_EVENTS = 3;
	
	int mostRecent;
	int count;
	TimeStampPair[] timeStampPair=new TimeStampPair[NUMBER_OF_EVENTS];
	
	public PowerFailEventLog(TimeZone timeZone) throws IOException {
		super(timeZone);
	}

	public void parse(byte[] data) throws IOException {
		int offset=0;
		mostRecent = ProtocolUtils.getIntLE(data, offset++, 1);
		count = ProtocolUtils.getIntLE(data, offset, 2); offset+=2;
        for( int i = 0; i < NUMBER_OF_EVENTS; i ++ ) {
        	timeStampPair[i] = new TimeStampPair(data,offset,getTimeZone());
        	offset+=TimeStampPair.size();
        	if (timeStampPair[i].getStartDate()!=null) {
        		addMeterEvent(new MeterEvent(timeStampPair[i].getStartDate(), MeterEvent.POWERDOWN, "start of powerfail"+" ("+count+")"));
        		addMeterEvent(new MeterEvent(timeStampPair[i].getEndDate(), MeterEvent.POWERUP, "end of powerfail"+" ("+count+")"));
        	}
        }

	}

}
