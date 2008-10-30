package com.energyict.protocolimpl.iec1107.abba230.eventlogs;

import java.io.IOException;
import java.util.*;

import com.energyict.protocol.MeterEvent;

abstract public class AbstractEventLog {
    
	TimeZone timeZone;
	List<MeterEvent> meterEvents=new ArrayList();
	
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

	public List<MeterEvent> getMeterEvents() {
		return meterEvents;
	}
}
