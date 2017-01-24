package com.energyict.protocolimpl.iec1107.abba1140.eventlogs;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.util.TimeZone;

public class PhaseFailureEventLog extends AbstractEventLog {

	static final String EVENT_NAME 		= "Phase failure event";
	static final int EVENT_CODE 		= MeterEvent.PHASE_FAILURE;
	private static final int INFO_SIZE	= 1;

	private int[] infoField = new int[NUMBER_OF_EVENTS];

	/*
	 * Constructors
	 */

	public PhaseFailureEventLog(TimeZone timeZone) {
		super(timeZone);
	}

	public void parse(byte[] data) throws IOException {
		count = ProtocolUtils.getIntLE(data, 0, COUNT_SIZE);
		for( int i = 0; i < NUMBER_OF_EVENTS; i++ ) {
        	timeStamp[i] = new TimeStamp(data, COUNT_SIZE + (i*TIMESTAMP_SIZE), getTimeZone());
        	infoField[i] = ((int)data[COUNT_SIZE + (TIMESTAMP_SIZE*NUMBER_OF_EVENTS) + (i*INFO_SIZE)]) & 0x000000FF;
        	if (timeStamp[i].getTimeStamp()!=null)
        		addMeterEvent(new MeterEvent(timeStamp[i].getTimeStamp(), getEventCode(), getEventName() + " " + getInfoFIeldDescription(infoField[i]) + " ("+count+")"));
        }
		debug();
	}

	protected void debug() {
		if (DEBUG<=0) return;
		System.out.println("count = " + count);
		for( int i = 0; i < NUMBER_OF_EVENTS; i ++ )
        	System.out.println(getEventName() + " timeStamp[" + i + "] = " + timeStamp[i].getTimeStamp() + " Info=" + getInfoFIeldDescription(infoField[i]) + " " + infoField[i]);
		System.out.println();
	}

	protected int getEventCode() {
		return EVENT_CODE;
	}

	protected String getEventName() {
		return EVENT_NAME;
	}

	private String getInfoFIeldDescription(int infoField) {
		switch (infoField) {
			case 0x01: return "Phase A";
			case 0x02: return "Phase B";
			case 0x03: return "Phase A and B";
			case 0x04: return "Phase C";
			case 0x05: return "Phase A and C";
			case 0x06: return "Phase B and C";
			case 0x07: return "Phase A, B and C";
			default: return "Invalid info field: " + infoField;
		}
	}

}
