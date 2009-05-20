package com.energyict.protocolimpl.iec1107.abba1140.eventlogs;

import java.util.TimeZone;
import com.energyict.protocol.MeterEvent;
 
public class InternalBatteryEventLog extends AbstractEventLog {

	static final String EVENT_NAME 		= "Internal battery event";
	static final int EVENT_CODE 		= MeterEvent.OTHER;
	
	/*
	 * Constructors
	 */

	public InternalBatteryEventLog(TimeZone timeZone) {
		super(timeZone);
	}

	protected int getEventCode() {
		return EVENT_CODE;
	}

	protected String getEventName() {
		return EVENT_NAME;
	}

}
