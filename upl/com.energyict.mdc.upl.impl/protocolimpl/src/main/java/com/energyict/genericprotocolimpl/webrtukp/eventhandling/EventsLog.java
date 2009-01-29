package com.energyict.genericprotocolimpl.webrtukp.eventhandling;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;

public class EventsLog {
	
	private TimeZone timeZone;
	private DataContainer dcEvents;
	
	// Event log
	private static final int EVENT_EVENT_LOG_CLEARED = 255;
	private static final int EVENT_POWER_DOWN = 1;
	private static final int EVENT_POWER_UP = 2;
	private static final int EVENT_DAYLING_SAVING_TIME_CHANGE = 3;
	private static final int EVENT_CLOCK_ADJUSTED_OLD = 4;
	private static final int EVENT_CLOCK_ADJUSTED_NEW = 5;
	private static final int EVENT_CLOCK_INVALID = 6;
	private static final int EVENT_BATTERY_REPLACE = 7;
	private static final int EVENT_BATTERY_VOLTAGE_LOW = 8;
	private static final int EVENT_TOU_ACTIVATED = 9;
	private static final int EVENT_ERROR_REGISTER_CLEARED = 10;
	private static final int EVENT_ALARM_REGISTER_CLEARED = 11;
	private static final int EVENT_PROGRAM_MEMORY_ERROR = 12;
	private static final int EVENT_RAM_ERROR = 13;
	private static final int EVENT_NV_MEMORY_ERROR = 14;
	private static final int EVENT_WATCHDOG_ERROR = 15;
	private static final int EVENT_MEASUREMENT_SYSTEM_ERROR = 16;
	private static final int EVENT_FIRMWARE_READY_ACTIVATION = 17;
	private static final int EVENT_FIRMWARE_ACTIVATED = 18;
	
	public EventsLog(TimeZone timeZone, DataContainer dc){
		this.timeZone = timeZone;
		this.dcEvents = dc;
	}
	
	public List<MeterEvent> getMeterEvents(){
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
		case EVENT_EVENT_LOG_CLEARED : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.CLEAR_DATA, eventId, "Event log profile cleared."));};
		case EVENT_POWER_DOWN : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.POWERDOWN, eventId, "Powerdown"));};
		case EVENT_POWER_UP : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.POWERUP, eventId, "Powerup"));};
		case EVENT_DAYLING_SAVING_TIME_CHANGE : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Daylight saving time changed (enabled/disabled)"));};
		case EVENT_CLOCK_ADJUSTED_OLD : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.SETCLOCK_BEFORE, eventId, "Clock adjusted (old dateTime)"));};
		case EVENT_CLOCK_ADJUSTED_NEW : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.SETCLOCK_AFTER, eventId, "Clock adjusted (new dateTime)"));};
		case EVENT_CLOCK_INVALID : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Clock invalid, power reserve may be exhausted"));};
		case EVENT_BATTERY_REPLACE : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Replace battery"));};
		case EVENT_BATTERY_VOLTAGE_LOW : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Battery voltage low"));};
		case EVENT_TOU_ACTIVATED : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Passive TOU has been activated"));};
		case EVENT_ERROR_REGISTER_CLEARED : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.CLEAR_DATA, eventId, "Error register cleared"));};
		case EVENT_ALARM_REGISTER_CLEARED : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.CLEAR_DATA, eventId, "Alarm register cleared"));};
		case EVENT_PROGRAM_MEMORY_ERROR : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.PROGRAM_FLOW_ERROR, eventId, "Physical or logical error in the Program memory"));};
		case EVENT_RAM_ERROR : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.RAM_MEMORY_ERROR, eventId, "Physical or logical error in RAM"));};
		case EVENT_NV_MEMORY_ERROR : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Physical or logical error in non volatile memory"));};
		case EVENT_WATCHDOG_ERROR : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.WATCHDOGRESET, eventId, "Watchdog reset or a hardware reset o the microcontroller"));};
		case EVENT_MEASUREMENT_SYSTEM_ERROR : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Physical or logical error in the measurement system"));};
		case EVENT_FIRMWARE_READY_ACTIVATION : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "New firmware has been successfully downloaded and verified, i.e. it is ready for activation"));};
		case EVENT_FIRMWARE_ACTIVATED : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "New firmware has been activated"));};
		default:{
			meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));
		}
		}
	}

	private boolean isOctetString(Object element) {
		return (element instanceof com.energyict.dlms.OctetString)?true:false;
	}
	

}
