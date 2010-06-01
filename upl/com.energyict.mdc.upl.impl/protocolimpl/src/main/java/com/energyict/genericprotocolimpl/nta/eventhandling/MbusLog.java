package com.energyict.genericprotocolimpl.nta.eventhandling;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.protocol.MeterEvent;

public class MbusLog {

	private TimeZone timeZone;
	private DataContainer dcEvents;
	
	// Mbus log
	private static final int EVENT_EVENT_LOG_CLEARED = 255;
	private static final int EVENT_COMM_ERROR_MBUS_CHANNEL1 = 100;
	private static final int EVENT_COMM_OK_MBUS_CHANNEL1 = 101;
	private static final int EVENT_REPLACE_BATTERY_MBUS1 = 102;
	private static final int EVENT_FRAUD_ATTEMPT_MBUS1 = 103;
	private static final int EVENT_CLOCK_ADJUSTED_MBUS1 = 104;
	
	private static final int EVENT_COMM_ERROR_MBUS_CHANNEL2 = 110;
	private static final int EVENT_COMM_OK_MBUS_CHANNEL2 = 111;
	private static final int EVENT_REPLACE_BATTERY_MBUS2 = 112;
	private static final int EVENT_FRAUD_ATTEMPT_MBUS2 = 113;
	private static final int EVENT_CLOCK_ADJUSTED_MBUS2 = 114;
	
	private static final int EVENT_COMM_ERROR_MBUS_CHANNEL3 = 120;
	private static final int EVENT_COMM_OK_MBUS_CHANNEL3 = 121;
	private static final int EVENT_REPLACE_BATTERY_MBUS3 = 122;
	private static final int EVENT_FRAUD_ATTEMPT_MBUS3 = 123;
	private static final int EVENT_CLOCK_ADJUSTED_MBUS3 = 124;
	
	private static final int EVENT_COMM_ERROR_MBUS_CHANNEL4 = 130;
	private static final int EVENT_COMM_OK_MBUS_CHANNEL4 = 131;
	private static final int EVENT_REPLACE_BATTERY_MBUS4 = 132;
	private static final int EVENT_FRAUD_ATTEMPT_MBUS4 = 133;
	private static final int EVENT_CLOCK_ADJUSTED_MBUS4 = 134;
	
	public MbusLog(TimeZone timeZone, DataContainer dc){
		this.timeZone = timeZone;
		this.dcEvents = dc;
	}
	
	public List<MeterEvent> getMeterEvents() throws IOException{
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
		
		if( !ExtraEvents.extraEvents.containsKey(new Integer(eventId)) ){
			switch(eventId){
			case EVENT_EVENT_LOG_CLEARED : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.CLEAR_DATA, eventId, "Event log profile cleared."));}break;
			case EVENT_COMM_ERROR_MBUS_CHANNEL1 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Communication problem with Mbus 1"));}break;
			case EVENT_COMM_OK_MBUS_CHANNEL1 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Communication problem with Mbus 1 resolved"));}break;
			case EVENT_REPLACE_BATTERY_MBUS1 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Battery must be replaced for Mbus 1"));}break;
			case EVENT_FRAUD_ATTEMPT_MBUS1 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.TAMPER, eventId, "Fraud attempt for Mbus 1"));}break;
			case EVENT_CLOCK_ADJUSTED_MBUS1 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.SETCLOCK, eventId, "Clock adjusted for Mbus 1"));}break;
	
			case EVENT_COMM_ERROR_MBUS_CHANNEL2 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Communication problem with Mbus 2"));}break;
			case EVENT_COMM_OK_MBUS_CHANNEL2 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Communication problem with Mbus 2 resolved"));}break;
			case EVENT_REPLACE_BATTERY_MBUS2 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Battery must be replaced for Mbus 2"));}break;
			case EVENT_FRAUD_ATTEMPT_MBUS2 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.TAMPER, eventId, "Fraud attempt for Mbus 2"));}break;
			case EVENT_CLOCK_ADJUSTED_MBUS2 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.SETCLOCK, eventId, "Clock adjusted for Mbus 2"));}break;
			
			case EVENT_COMM_ERROR_MBUS_CHANNEL3 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Communication problem with Mbus 3"));}break;
			case EVENT_COMM_OK_MBUS_CHANNEL3 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Communication problem with Mbus 3 resolved"));}break;
			case EVENT_REPLACE_BATTERY_MBUS3 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Battery must be replaced for Mbus 3"));}break;
			case EVENT_FRAUD_ATTEMPT_MBUS3 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.TAMPER, eventId, "Fraud attempt for Mbus 3"));}break;
			case EVENT_CLOCK_ADJUSTED_MBUS3 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.SETCLOCK, eventId, "Clock adjusted for Mbus 3"));}break;
			
			case EVENT_COMM_ERROR_MBUS_CHANNEL4 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Communication problem with Mbus 4"));}break;
			case EVENT_COMM_OK_MBUS_CHANNEL4 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Communication problem with Mbus 4 resolved"));}break;
			case EVENT_REPLACE_BATTERY_MBUS4 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Battery must be replaced for Mbus 4"));}break;
			case EVENT_FRAUD_ATTEMPT_MBUS4 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.TAMPER, eventId, "Fraud attempt for Mbus 4"));}break;
			case EVENT_CLOCK_ADJUSTED_MBUS4 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.SETCLOCK, eventId, "Clock adjusted for Mbus 4"));}break;
			
			default:{
				meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));
			}break;
			}
		} else {
			meterEvents.add(ExtraEvents.getExtraEvent(eventTimeStamp, eventId));
		}
	}

	private boolean isOctetString(Object element) {
		return (element instanceof com.energyict.dlms.OctetString)?true:false;
	}
	

}
