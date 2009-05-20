package com.energyict.protocolimpl.iec1107.abba1140.eventlogs;

import java.util.TimeZone;
import com.energyict.protocol.MeterEvent;
 
public class PhaseFailureEventLog extends AbstractEventLog {

	static final String EVENT_NAME 		= "Phase failure event";
	static final int EVENT_CODE 		= MeterEvent.PHASE_FAILURE;
	
	/*
	 * Constructors
	 */

	public PhaseFailureEventLog(TimeZone timeZone) {
		super(timeZone);
	}

	protected void doParse(byte[] data) {
		
	}

	protected int getEventCode() {
		return EVENT_CODE;
	}

	protected String getEventName() {
		return EVENT_NAME;
	}

}
