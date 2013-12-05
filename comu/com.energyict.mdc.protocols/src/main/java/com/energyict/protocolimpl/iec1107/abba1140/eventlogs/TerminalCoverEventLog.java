package com.energyict.protocolimpl.iec1107.abba1140.eventlogs;

import com.energyict.mdc.protocol.device.events.MeterEvent;

import java.util.TimeZone;

public class TerminalCoverEventLog extends AbstractEventLog {

	static final String EVENT_NAME 		= "Terminal cover event";
	static final int EVENT_CODE 		= MeterEvent.TERMINAL_OPENED;

	/*
	 * Constructors
	 */

	public TerminalCoverEventLog(TimeZone timeZone) {
		super(timeZone);
	}

	protected int getEventCode() {
		return EVENT_CODE;
	}

	protected String getEventName() {
		return EVENT_NAME;
	}

}
