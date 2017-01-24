package com.energyict.protocolimpl.iec1107.abba1140.eventlogs;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.util.TimeZone;

public class TransientEventLog extends AbstractEventLog {

	static final String EVENT_NAME 		= "Transient event";
	static final int EVENT_CODE 		= MeterEvent.OTHER;

	/*
	 * Constructors
	 */

	public TransientEventLog(TimeZone timeZone) {
		super(timeZone);
	}

	protected int getEventCode() {
		return EVENT_CODE;
	}

	protected String getEventName() {
		return EVENT_NAME;
	}

}
