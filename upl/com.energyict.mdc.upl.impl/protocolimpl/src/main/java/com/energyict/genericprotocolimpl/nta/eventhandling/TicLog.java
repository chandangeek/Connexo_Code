package com.energyict.genericprotocolimpl.nta.eventhandling;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class TicLog extends AbstractEvent{

	private static final int EVENT_PERCC = 240;
	private static final int EVENT_PERCP = 241;
	
	public TicLog(TimeZone timeZone, DataContainer ticContainter) {
        super(ticContainter, timeZone);
	}

	protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
		switch(eventId){
		case EVENT_PERCC: {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "(PERCC) transition between period P and P-1"));}break;
		case EVENT_PERCP: {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "(PERCP) transition between period P-1 and P-2"));}break;
		default:{
			meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Eventcode: " + eventId));
		}
		}
	}
}
