package com.energyict.protocolimpl.iec1107.abba1140.eventlogs;

import java.io.IOException;
import java.util.*;

import com.energyict.protocol.*;
 
public class TerminalCoverEventLog extends AbstractEventLog {

	private static final int NUMBER_OF_EVENTS = 3;
	
	int mostRecent;
	int count;
	TimeStampPair[] timeStampPair=new TimeStampPair[NUMBER_OF_EVENTS];
	
	public TerminalCoverEventLog(TimeZone timeZone) throws IOException {
		super(timeZone);
	}
	
	public void parse(byte[] data) throws IOException {
		System.out.println("DEBUG: Entering TerminalCoverEventLog, parse(data)");
		System.out.println("Data: ");
		ProtocolUtils.printResponseData(data);
		System.out.println();
		
		
		int offset=0;
		mostRecent = ProtocolUtils.getIntLE(data, offset++, 1);
		count = ProtocolUtils.getIntLE(data, offset, 2); offset+=2;
        
		System.out.println("mostRecent = " + mostRecent);
		System.out.println("count = " + count);
		
		for( int i = 0; i < NUMBER_OF_EVENTS; i ++ ) {
        	timeStampPair[i] = new TimeStampPair(data,offset,getTimeZone());
        	System.out.println("timeStampPair["+i+"] = startDate: " + timeStampPair[i].getStartDate() + " endDate: " + timeStampPair[i].getEndDate());
        	offset+=TimeStampPair.size();
        	if (timeStampPair[i].getStartDate()!=null) {
        		addMeterEvent(new MeterEvent(timeStampPair[i].getStartDate(), MeterEvent.TERMINAL_OPENED, "start of terminal cover tamper"+" ("+count+")"));
        		addMeterEvent(new MeterEvent(timeStampPair[i].getEndDate(), MeterEvent.TERMINAL_OPENED, "end of terminal cover tamper"+" ("+count+")"));
        	} 
        }

	}


}
