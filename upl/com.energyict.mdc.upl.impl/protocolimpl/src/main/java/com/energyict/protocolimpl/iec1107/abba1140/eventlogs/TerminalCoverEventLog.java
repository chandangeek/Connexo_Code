package com.energyict.protocolimpl.iec1107.abba1140.eventlogs;

import java.io.IOException;
import java.util.TimeZone;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProtocolUtils;
 
public class TerminalCoverEventLog extends AbstractEventLog {

	private static final int DEBUG 				= 0;
	private static final int NUMBER_OF_EVENTS 	= 3;
	private static final int COUNT_SIZE 		= 2;
	private static final int TIMESTAMP_SIZE 	= 4;

	private static final String EVENT_NAME 		= "Terminal cover tamper";
	private static final int EVENT_CODE 		= MeterEvent.TERMINAL_OPENED;
	
	private int count = 0;
	private TimeStamp[] timeStamp=new TimeStamp[NUMBER_OF_EVENTS];

	/*
	 * Constructors
	 */

	public TerminalCoverEventLog(TimeZone timeZone) throws IOException {
		super(timeZone);
	}

	/*
	 * Private getters, setters and methods
	 */

	private void debug() {
		System.out.println("count = " + count);
		for( int i = 0; i < NUMBER_OF_EVENTS; i ++ )
        	System.out.println(EVENT_NAME + " timeStamp[" + i + "] = " + timeStamp[i].getTimeStamp());
	}

	/*
	 * Public methods
	 */

	public void parse(byte[] data) throws IOException {
		count = ProtocolUtils.getIntLE(data, 0, COUNT_SIZE);
		for( int i = 0; i < NUMBER_OF_EVENTS; i++ ) {
        	timeStamp[i] = new TimeStamp(data, COUNT_SIZE + (i*TIMESTAMP_SIZE), getTimeZone());
        	if (timeStamp[i].getTimeStamp()!=null)
        		addMeterEvent(new MeterEvent(timeStamp[i].getTimeStamp(), EVENT_CODE, EVENT_NAME + " ("+count+")"));
        }
		if (DEBUG>=1) debug();
	}

	/*
	 * Public getters and setters
	 */

	public int getCount() {
		return count;
	}
	
	public TimeStamp[] getTimeStamp() {
		return timeStamp;
	}

	public TimeStamp getTimeStamp(int index) {
		return timeStamp[index];
	}

}
