package com.energyict.genericprotocolimpl.webrtukp.eventhandling;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.protocol.MeterEvent;

public class PowerFailureLog {
	
	private TimeZone timeZone;
	private DataContainer dcEvents;
	
	// Power failure log
	public PowerFailureLog(TimeZone timeZone, DataContainer dc){
		this.timeZone = timeZone;
		this.dcEvents = dc;
	}
	
	public List<MeterEvent> getMeterEvents() throws IOException{
		List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
		int size = this.dcEvents.getRoot().getNrOfElements();
		Date eventTimeStamp = null;
		for(int i = 0; i <= (size-1); i++){
			int duration = (int)this.dcEvents.getRoot().getStructure(i).getValue(1);
			if(isOctetString(this.dcEvents.getRoot().getStructure(i).getElement(0))){
				eventTimeStamp = new AXDRDateTime(new OctetString(dcEvents.getRoot().getStructure(i).getOctetString(0).getArray())).getValue().getTime();
			}
			if(eventTimeStamp != null){
				buildMeterEvent(meterEvents, eventTimeStamp, duration);
			}
		}
		return meterEvents;
	}

	private void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int duration) {
		meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.PHASE_FAILURE, "Duration of power failure: " + duration));
	}

	private boolean isOctetString(Object element) {
		return (element instanceof com.energyict.dlms.OctetString)?true:false;
	}
}
