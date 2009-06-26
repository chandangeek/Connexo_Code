package com.energyict.genericprotocolimpl.webrtukp.eventhandling;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;

public class TicLog {
	
	private TimeZone timeZone;
	private DataContainer dcEvents;

	private static final int EVENT_PERCC = 240;
	private static final int EVENT_PERCP = 241;
	
	public TicLog(TimeZone timeZone, DataContainer ticContainter) {
		this.timeZone = timeZone;
		this.dcEvents = ticContainter;
	}

	public List<MeterEvent> getMeterEvents() {
		List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
		int size = this.dcEvents.getRoot().getNrOfElements();
		Date eventTimeStamp = null;
		for(int i = 0; i <= (size-1); i++){
			int eventId = (int)this.dcEvents.getRoot().getStructure(i).getValue(1);
			if(isOctetString(this.dcEvents.getRoot().getStructure(i).getElement(0))){
				eventTimeStamp = dcEvents.getRoot().getStructure(i).getOctetString(0).toDate(this.timeZone);
			}
			if(eventTimeStamp != null){
				buildMeterEvent(meterEvents, eventTimeStamp, eventId);
			}
		}
		return meterEvents;
	}
	
	private void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
		switch(eventId){
		case EVENT_PERCC: {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "(PERCC) transition between period P and P-1"));}break;
		case EVENT_PERCP: {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "(PERCP) transition between period P-1 and P-2"));}break;
		default:{
			meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Eventcode: " + eventId));
		}
		}
	}

	private boolean isOctetString(Object element) {
		return (element instanceof com.energyict.dlms.OctetString)?true:false;
	}

}
