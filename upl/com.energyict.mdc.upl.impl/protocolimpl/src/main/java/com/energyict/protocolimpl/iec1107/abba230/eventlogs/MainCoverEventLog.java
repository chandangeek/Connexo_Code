package com.energyict.protocolimpl.iec1107.abba230.eventlogs;

import java.io.IOException;
import java.util.TimeZone;

import com.energyict.protocol.*;

public class MainCoverEventLog extends AbstractEventLog {

	int mostRecent;
	int count;
	TimeStampPair[] timeStampPair=new TimeStampPair[10];
	
	public MainCoverEventLog(TimeZone timeZone) throws IOException {
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
        		addMeterEvent(new MeterEvent(timeStampPair[i].getStartDate(), MeterEvent.OTHER, "main cover opened"+" ("+count+")"));
        		addMeterEvent(new MeterEvent(timeStampPair[i].getEndDate(), MeterEvent.OTHER, "main cover closed"+" ("+count+")"));
        		//addMeterEvent(new MeterEvent(timeStampPair[i].getStartDate(), MeterEvent.COVER_OPENED, "main cover opened"+" ("+count+")"));
        		//addMeterEvent(new MeterEvent(timeStampPair[i].getEndDate(), MeterEvent.COVER_OPENED, "main cover closed"+" ("+count+")"));
        	}
        }

	}

}
