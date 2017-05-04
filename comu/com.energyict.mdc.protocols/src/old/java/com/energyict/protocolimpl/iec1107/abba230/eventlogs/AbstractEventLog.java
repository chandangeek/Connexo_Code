/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.abba230.eventlogs;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

abstract public class AbstractEventLog {

	TimeZone timeZone;
	List meterEvents=new ArrayList();

	public AbstractEventLog(TimeZone timeZone) throws IOException {
        this.timeZone=timeZone;
    }

	public TimeZone getTimeZone() {
		return timeZone;
	}

	protected void addMeterEvent(MeterEvent meterEvent) {
		if (meterEvent.getTime() != null)
			meterEvents.add(meterEvent);
	}

	public List getMeterEvents() {
		return meterEvents;
	}
}
