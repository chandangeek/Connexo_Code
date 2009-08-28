package com.energyict.protocolimpl.iec1107.abba230.eventlogs;

import java.io.IOException;
import java.util.*;

import com.energyict.protocol.*;

public class OverVoltageEventLog extends AbstractEventLog {

	int mostRecent;
	int count;
	TimeStampPair[] timeStampPair=new TimeStampPair[10];
	
	public OverVoltageEventLog(TimeZone timeZone) throws IOException {
		super(timeZone);
	}

	public void parse(byte[] data) throws IOException {
		int offset=0;
		mostRecent = ProtocolUtils.getIntLE(data, offset++, 1);
		count = ProtocolUtils.getIntLE(data, offset, 2); offset+=2;
        for( int i = 0; i < 10; i ++ ) {
        	timeStampPair[i] = new TimeStampPair(data,offset,getTimeZone());
        	offset+=TimeStampPair.size();
        	if (timeStampPair[i].getStartDate()!=null) {
        		addMeterEvent(new MeterEvent(timeStampPair[i].getStartDate(), MeterEvent.VOLTAGE_SWELL, "start of overvoltage"+" ("+count+")"));
        		addMeterEvent(new MeterEvent(timeStampPair[i].getEndDate(), MeterEvent.VOLTAGE_SWELL, "end of overvoltage"+" ("+count+")"));
        	}
        }

	}
}
