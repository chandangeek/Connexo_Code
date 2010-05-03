package com.energyict.genericprotocolimpl.webrtuz3.eventhandling;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.protocol.MeterEvent;

import java.io.IOException;
import java.util.*;

public class TicLog {
	
	private DataContainer dcEvents;

	private static final int EVENT_PERCC = 240;
	private static final int EVENT_PERCP = 241;
	
	public TicLog(DataContainer ticContainter) {
		this.dcEvents = ticContainter;
	}

	public List<MeterEvent> getMeterEvents() throws IOException {
		List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
		int size = this.dcEvents.getRoot().getNrOfElements();
		Date eventTimeStamp = null;
		for(int i = 0; i <= (size-1); i++){
			int eventId = (int)this.dcEvents.getRoot().getStructure(i).getValue(1)&0xFF; // To prevent negative values
			if(isOctetString(this.dcEvents.getRoot().getStructure(i).getElement(0))){
				eventTimeStamp = new AXDRDateTime(new OctetString(dcEvents.getRoot().getStructure(i).getOctetString(0).getArray())).getValue().getTime();
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
