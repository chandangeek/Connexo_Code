/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.abba1140.eventlogs;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.util.TimeZone;

public class EndOfBillingEventLog extends AbstractEventLog {

	static final String EVENT_NAME 		= "End of billing event";
	static final int EVENT_CODE 		= MeterEvent.BILLING_ACTION;
	private static final int INFO_SIZE	= 1;

	private int[] infoField = new int[NUMBER_OF_EVENTS];

	/*
	 * Constructors
	 */

	public EndOfBillingEventLog(TimeZone timeZone) {
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
			case 0x01: return "Change of billing date";
			case 0x02: return "Season change";
			case 0x04: return "Tarif change over";
			case 0x08: return "Serial port";
			case 0x10: return "Optical port";
			case 0x20: return "Billing button";
			case 0x40: return "CT ratio change";
			case 0x80: return "Power up";
			default: return "Invalid info field: " + infoField;
		}
	}

}
