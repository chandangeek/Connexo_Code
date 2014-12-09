package com.energyict.protocolimpl.dlms.nta.eventhandling;

import com.energyict.dlms.DataContainer;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class EventsLog extends AbstractEvent{

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
        super(dc, timeZone);
    }

    /**
     * {@inheritDoc}
     */
	protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
		
		if( !ExtraEvents.extraEvents.containsKey(new Integer(eventId)) ){
			switch(eventId){
			case EVENT_EVENT_LOG_CLEARED : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.CLEAR_DATA, eventId, "Event log profile cleared."));}break;
			case EVENT_POWER_DOWN : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.POWERDOWN, eventId, "Powerdown"));}break;
			case EVENT_POWER_UP : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.POWERUP, eventId, "Powerup"));}break;
			case EVENT_DAYLING_SAVING_TIME_CHANGE : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.DAYLIGHT_SAVING_TIME_ENABLED_OR_DISABLED, eventId, "Daylight saving time changed (enabled/disabled)"));}break;
			case EVENT_CLOCK_ADJUSTED_OLD : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.SETCLOCK_BEFORE, eventId, "Clock adjusted (old dateTime)"));}break;
			case EVENT_CLOCK_ADJUSTED_NEW : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.SETCLOCK_AFTER, eventId, "Clock adjusted (new dateTime)"));}break;
			case EVENT_CLOCK_INVALID : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.CLOCK_INVALID, eventId, "Clock invalid, power reserve may be exhausted"));}break;
			case EVENT_BATTERY_REPLACE : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.REPLACE_BATTERY, eventId, "Replace battery"));}break;
			case EVENT_BATTERY_VOLTAGE_LOW : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.BATTERY_VOLTAGE_LOW, eventId, "Battery voltage low"));}break;
			case EVENT_TOU_ACTIVATED : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.TOU_ACTIVATED, eventId, "Passive TOU has been activated"));}break;
			case EVENT_ERROR_REGISTER_CLEARED : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.ERROR_REGISTER_CLEARED, eventId, "Error register cleared"));}break;
			case EVENT_ALARM_REGISTER_CLEARED : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.ALARM_REGISTER_CLEARED, eventId, "Alarm register cleared"));}break;
			case EVENT_PROGRAM_MEMORY_ERROR : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.PROGRAM_MEMORY_ERROR, eventId, "Physical or logical error in the Program memory"));}break;
			case EVENT_RAM_ERROR : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.RAM_MEMORY_ERROR, eventId, "Physical or logical error in RAM"));}break;
			case EVENT_NV_MEMORY_ERROR : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.NV_MEMORY_ERROR, eventId, "Physical or logical error in non volatile memory"));}break;
			case EVENT_WATCHDOG_ERROR : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.WATCHDOG_ERROR, eventId, "Watchdog reset or a hardware reset o the microcontroller"));}break;
			case EVENT_MEASUREMENT_SYSTEM_ERROR : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.MEASUREMENT_SYSTEM_ERROR, eventId, "Physical or logical error in the measurement system"));}break;
			case EVENT_FIRMWARE_READY_ACTIVATION : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.FIRMWARE_READY_FOR_ACTIVATION, eventId, "New firmware has been successfully downloaded and verified, i.e. it is ready for activation"));}break;
			case EVENT_FIRMWARE_ACTIVATED : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.FIRMWARE_ACTIVATED, eventId, "New firmware has been activated"));}break;
			default:{
				meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));
			}
			}
		} else {
			meterEvents.add(ExtraEvents.getExtraEvent(eventTimeStamp, eventId));
		}
	}
}
