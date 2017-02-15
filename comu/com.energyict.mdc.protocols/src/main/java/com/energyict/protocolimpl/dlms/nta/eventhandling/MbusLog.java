/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.nta.eventhandling;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.dlms.DataContainer;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class MbusLog extends AbstractEvent{
	
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
        super(dc, timeZone);
	}

	protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
		
		if( !ExtraEvents.extraEvents.containsKey(new Integer(eventId)) ){
			switch(eventId){
			case EVENT_EVENT_LOG_CLEARED : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.CLEAR_DATA, eventId, "Event log profile cleared."));}break;
			case EVENT_COMM_ERROR_MBUS_CHANNEL1 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.COMMUNICATION_ERROR_MBUS, eventId, "Communication problem with Mbus 1"));}break;
			case EVENT_COMM_OK_MBUS_CHANNEL1 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.COMMUNICATION_OK_MBUS, eventId, "Communication problem with Mbus 1 resolved"));}break;
			case EVENT_REPLACE_BATTERY_MBUS1 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.REPLACE_BATTERY_MBUS, eventId, "Battery must be replaced for Mbus 1"));}break;
			case EVENT_FRAUD_ATTEMPT_MBUS1 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.FRAUD_ATTEMPT_MBUS, eventId, "Fraud attempt for Mbus 1"));}break;
			case EVENT_CLOCK_ADJUSTED_MBUS1 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.CLOCK_ADJUSTED_MBUS, eventId, "Clock adjusted for Mbus 1"));}break;
	
			case EVENT_COMM_ERROR_MBUS_CHANNEL2 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.COMMUNICATION_ERROR_MBUS, eventId, "Communication problem with Mbus 2"));}break;
			case EVENT_COMM_OK_MBUS_CHANNEL2 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.COMMUNICATION_OK_MBUS, eventId, "Communication problem with Mbus 2 resolved"));}break;
			case EVENT_REPLACE_BATTERY_MBUS2 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.REPLACE_BATTERY_MBUS, eventId, "Battery must be replaced for Mbus 2"));}break;
			case EVENT_FRAUD_ATTEMPT_MBUS2 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.FRAUD_ATTEMPT_MBUS, eventId, "Fraud attempt for Mbus 2"));}break;
			case EVENT_CLOCK_ADJUSTED_MBUS2 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.CLOCK_ADJUSTED_MBUS, eventId, "Clock adjusted for Mbus 2"));}break;
			
			case EVENT_COMM_ERROR_MBUS_CHANNEL3 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.COMMUNICATION_ERROR_MBUS, eventId, "Communication problem with Mbus 3"));}break;
			case EVENT_COMM_OK_MBUS_CHANNEL3 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.COMMUNICATION_OK_MBUS, eventId, "Communication problem with Mbus 3 resolved"));}break;
			case EVENT_REPLACE_BATTERY_MBUS3 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.REPLACE_BATTERY_MBUS, eventId, "Battery must be replaced for Mbus 3"));}break;
			case EVENT_FRAUD_ATTEMPT_MBUS3 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.FRAUD_ATTEMPT_MBUS, eventId, "Fraud attempt for Mbus 3"));}break;
			case EVENT_CLOCK_ADJUSTED_MBUS3 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.CLOCK_ADJUSTED_MBUS, eventId, "Clock adjusted for Mbus 3"));}break;
			
			case EVENT_COMM_ERROR_MBUS_CHANNEL4 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.COMMUNICATION_ERROR_MBUS, eventId, "Communication problem with Mbus 4"));}break;
			case EVENT_COMM_OK_MBUS_CHANNEL4 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.COMMUNICATION_OK_MBUS, eventId, "Communication problem with Mbus 4 resolved"));}break;
			case EVENT_REPLACE_BATTERY_MBUS4 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.REPLACE_BATTERY_MBUS, eventId, "Battery must be replaced for Mbus 4"));}break;
			case EVENT_FRAUD_ATTEMPT_MBUS4 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.FRAUD_ATTEMPT_MBUS, eventId, "Fraud attempt for Mbus 4"));}break;
			case EVENT_CLOCK_ADJUSTED_MBUS4 : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.CLOCK_ADJUSTED_MBUS, eventId, "Clock adjusted for Mbus 4"));}break;
			
			default:{
				meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));
			}break;
			}
		} else {
			meterEvents.add(ExtraEvents.getExtraEvent(eventTimeStamp, eventId));
		}
	}
}
