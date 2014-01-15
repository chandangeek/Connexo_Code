package com.energyict.protocolimpl.iec1107.abba1140.eventlogs;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

abstract public class AbstractEventLog {

	static final int DEBUG 				= 0;
	static final int COUNT_SIZE 		= 2;
	static final int TIMESTAMP_SIZE 	= 4;
	static final int NUMBER_OF_EVENTS 	= 3;

	int count = 0;
	TimeStamp[] timeStamp=new TimeStamp[NUMBER_OF_EVENTS];
	TimeZone timeZone;
	List meterEvents=new ArrayList();

	abstract protected String getEventName();
	abstract protected int getEventCode();

	/*
	 * Constructors
	 */

	public AbstractEventLog(TimeZone timeZone) {
        this.timeZone=timeZone;
    }

	/*
	 * Private getters, setters and methods
	 */

	protected void debug() {
		if (DEBUG<=0) return;
		System.out.println("count = " + count);
		for( int i = 0; i < NUMBER_OF_EVENTS; i ++ )
        	System.out.println(getEventName() + " timeStamp[" + i + "] = " + timeStamp[i].getTimeStamp());
		System.out.println();
	}

	protected void addMeterEvent(MeterEvent meterEvent) {
		if (meterEvent.getTime() != null)
			meterEvents.add(meterEvent);
	}

	/*
	 * Public methods
	 */

	public void parse(byte[] data) throws IOException {
		count = ProtocolUtils.getIntLE(data, 0, COUNT_SIZE);
		for( int i = 0; i < NUMBER_OF_EVENTS; i++ ) {
        	timeStamp[i] = new TimeStamp(data, COUNT_SIZE + (i*TIMESTAMP_SIZE), getTimeZone());
        	if (timeStamp[i].getTimeStamp()!=null)
        		addMeterEvent(new MeterEvent(timeStamp[i].getTimeStamp(), getEventCode(), getEventName() + " ("+count+")"));
        }
		debug();
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

	public List getMeterEvents() {
		return meterEvents;
	}

	public TimeZone getTimeZone() {
		return timeZone;
	}

}
