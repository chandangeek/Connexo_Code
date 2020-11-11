package com.energyict.protocolimpl.iec1107.abba1140.eventlogs;

import java.util.TimeZone;
import com.energyict.protocol.MeterEvent;
 
public class MainCoverEventLog extends AbstractEventLog {

	static final String EVENT_NAME 		= "Main cover event";
	static final int EVENT_CODE 		= MeterEvent.METER_COVER_OPENED;
	
	/*
	 * Constructors
	 */

	public MainCoverEventLog(TimeZone timeZone) {
		super(timeZone);
	}

	protected int getEventCode() {
		return EVENT_CODE;
	}

	protected String getEventName() {
		return EVENT_NAME;
	}

}
