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

public class FraudDetectionLog {
	
	private TimeZone timeZone;
	private DataContainer dcEvents;
	
	// Fraud detection log
	private static final int EVENT_EVENT_LOG_CLEARED = 255;
	private static final int EVENT_TERMINAL_COVER_REMOVED = 40;
	private static final int EVENT_TERMINAL_COVER_CLOSED = 41;
	private static final int EVENT_STRONG_DC_FIELD = 42;
	private static final int EVENT_STRONG_DC_FIELD_GONE = 43;
	private static final int EVENT_METER_COVER_REMOVED = 44;
	private static final int EVENT_METER_COVER_CLOSED = 45;
	private static final int EVENT_TIMES_WRONG_PASSWORD = 46;
	
	public FraudDetectionLog(TimeZone timeZone, DataContainer dc){
		this.timeZone = timeZone;
		this.dcEvents = dc;
	}
	
	public List<MeterEvent> getMeterEvents() throws IOException{
		List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
		int size = this.dcEvents.getRoot().getNrOfElements();
		Date eventTimeStamp = null;
		for(int i = 0; i <= (size-1); i++){
			int eventId = (int)this.dcEvents.getRoot().getStructure(i).getValue(1);
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
		case EVENT_EVENT_LOG_CLEARED : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.CLEAR_DATA, eventId, "Fraud detection event log profile cleared."));}break;
		case EVENT_TERMINAL_COVER_REMOVED : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.TERMINAL_OPENED, eventId, "The terminal cover has been removed"));}break;
		case EVENT_TERMINAL_COVER_CLOSED : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.TAMPER, eventId, "The terminal cover has been closed"));}break;
		case EVENT_STRONG_DC_FIELD : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "A strong magnetic DC field has been detected"));}break;
		case EVENT_STRONG_DC_FIELD_GONE : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "The strong magnetic DC field disappeared"));}break;
		case EVENT_METER_COVER_REMOVED : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.COVER_OPENED, eventId, "The meter cover has been removed"));}break;
		case EVENT_METER_COVER_CLOSED : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.TAMPER, eventId, "The meter cover has been closed"));}break;
		case EVENT_TIMES_WRONG_PASSWORD : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Intrusion Detection, User tried to gain access with a wrong password"));}break;
		default : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));}break;
		}
	}

	private boolean isOctetString(Object element) {
		return (element instanceof com.energyict.dlms.OctetString)?true:false;
	}
}
