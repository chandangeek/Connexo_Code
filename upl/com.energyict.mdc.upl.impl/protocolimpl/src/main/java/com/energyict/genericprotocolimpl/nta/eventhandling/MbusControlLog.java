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

public class MbusControlLog {
	
	private TimeZone timeZone;
	private DataContainer dcEvents;
	
	// Mbus control log
	private static final int EVENT_EVENT_LOG_CLEARED = 255;
	
	private static final int EVENT_MANUAL_DISCONNECTION_MBUS1 = 160;
	private static final int EVENT_MANUAL_CONNECTION_MBUS1 = 161;
	private static final int EVENT_REMOTE_DISCONNECTION_MBUS1 = 162;
	private static final int EVENT_REMOTE_CONNECTION_MBUS1 = 163;
	private static final int EVENT_VALVE_ALARM_MBUS1 = 164;
	
	private static final int EVENT_MANUAL_DISCONNECTION_MBUS2 = 170;
	private static final int EVENT_MANUAL_CONNECTION_MBUS2 = 171;
	private static final int EVENT_REMOTE_DISCONNECTION_MBUS2 = 172;
	private static final int EVENT_REMOTE_CONNECTION_MBUS2 = 173;
	private static final int EVENT_VALVE_ALARM_MBUS2 = 174;
	
	private static final int EVENT_MANUAL_DISCONNECTION_MBUS3 = 180;
	private static final int EVENT_MANUAL_CONNECTION_MBUS3 = 181;
	private static final int EVENT_REMOTE_DISCONNECTION_MBUS3 = 182;
	private static final int EVENT_REMOTE_CONNECTION_MBUS3 = 183;
	private static final int EVENT_VALVE_ALARM_MBUS3 = 184;
	
	private static final int EVENT_MANUAL_DISCONNECTION_MBUS4 = 190;
	private static final int EVENT_MANUAL_CONNECTION_MBUS4 = 191;
	private static final int EVENT_REMOTE_DISCONNECTION_MBUS4 = 192;
	private static final int EVENT_REMOTE_CONNECTION_MBUS4 = 193;
	private static final int EVENT_VALVE_ALARM_MBUS4 = 194;
	
	public MbusControlLog(TimeZone timeZone, DataContainer dc){
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
			case EVENT_EVENT_LOG_CLEARED : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.CLEAR_DATA, eventId, "Mbus control event log profile cleared."));}break;
			
			case EVENT_MANUAL_DISCONNECTION_MBUS1 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "The disconnector has been manually disconnected (MBus1)"));}break;
			case EVENT_MANUAL_CONNECTION_MBUS1 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "The disconnector has been manually connected (MBus1)" ));}break;
			case EVENT_REMOTE_DISCONNECTION_MBUS1 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "The disconnector has been remotely disconnected (MBus1)" ));}break;
			case EVENT_REMOTE_CONNECTION_MBUS1 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "The disconnector has been remotely connected (MBus1)" ));}break;
			case EVENT_VALVE_ALARM_MBUS1 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.METER_ALARM, eventId, "Valve alarm has been registerd (MBus1)" ));}break;
			
			case EVENT_MANUAL_DISCONNECTION_MBUS2 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "The disconnector has been manually disconnected (MBus2)"));}break;
			case EVENT_MANUAL_CONNECTION_MBUS2 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "The disconnector has been manually connected (MBus2)" ));}break;
			case EVENT_REMOTE_DISCONNECTION_MBUS2 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "The disconnector has been remotely disconnected (MBus2)" ));}break;
			case EVENT_REMOTE_CONNECTION_MBUS2 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "The disconnector has been remotely connected (MBus2)" ));}break;
			case EVENT_VALVE_ALARM_MBUS2 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.METER_ALARM, eventId, "Valve alarm has been registerd (MBus2)" ));}break;
			
			case EVENT_MANUAL_DISCONNECTION_MBUS3 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "The disconnector has been manually disconnected (Mbus3)"));}break;
			case EVENT_MANUAL_CONNECTION_MBUS3 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "The disconnector has been manually connected (Mbus3)" ));}break;
			case EVENT_REMOTE_DISCONNECTION_MBUS3 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "The disconnector has been remotely disconnected (Mbus3)" ));}break;
			case EVENT_REMOTE_CONNECTION_MBUS3 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "The disconnector has been remotely connected (Mbus3)" ));}break;
			case EVENT_VALVE_ALARM_MBUS3 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.METER_ALARM, eventId, "Valve alarm has been registerd (Mbus3)" ));}break;
			
			case EVENT_MANUAL_DISCONNECTION_MBUS4 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "The disconnector has been manually disconnected (Mbus4)"));}break;
			case EVENT_MANUAL_CONNECTION_MBUS4 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "The disconnector has been manually connected (Mbus4)" ));}break;
			case EVENT_REMOTE_DISCONNECTION_MBUS4 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "The disconnector has been remotely disconnected (Mbus4)" ));}break;
			case EVENT_REMOTE_CONNECTION_MBUS4 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "The disconnector has been remotely connected (Mbus4)" ));}break;
			case EVENT_VALVE_ALARM_MBUS4 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.METER_ALARM, eventId, "Valve alarm has been registerd (Mbus4)" ));}break;
			
			default : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));}break;
			}
		} else {
			meterEvents.add(ExtraEvents.getExtraEvent(eventTimeStamp, eventId));
		}
	}

	private boolean isOctetString(Object element) {
		return (element instanceof com.energyict.dlms.OctetString)?true:false;
	}
}
