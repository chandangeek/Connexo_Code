package com.energyict.protocolimpl.iec1107.abba1140.eventlogs;

import java.util.TimeZone;
import com.energyict.protocol.MeterEvent;
 
public class PowerFailEventLog extends AbstractEventLog {

	static final String EVENT_NAME 		= "Power fail event";
	static final int EVENT_CODE 		= MeterEvent.POWERDOWN;
	
	/*
	 * Constructors
	 */

	public PowerFailEventLog(TimeZone timeZone) {
		super(timeZone);
	}

	protected int getEventCode() {
		return EVENT_CODE;
	}

	protected String getEventName() {
		return EVENT_NAME;
	}

}
