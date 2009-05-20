package com.energyict.protocolimpl.iec1107.abba1140.eventlogs;

import java.io.IOException;
import java.util.*;

import com.energyict.protocol.MeterEvent;

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
