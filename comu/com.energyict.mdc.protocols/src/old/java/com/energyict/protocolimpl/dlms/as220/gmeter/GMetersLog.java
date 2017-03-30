/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.as220.gmeter;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GMetersLog {

	private DataContainer dcEvents;

	// Mbus log
	private static final int MBUS1_REPLACE_BATTERY = 102;
	private static final int MBUS2_REPLACE_BATTERY = 112;
	private static final int MBUS3_REPLACE_BATTERY = 122;
	private static final int MBUS4_REPLACE_BATTERY = 132;

	private static final int MBUS1_FRAUD_ATTEMPT = 103;
	private static final int MBUS2_FRAUD_ATTEMPT = 113;
	private static final int MBUS3_FRAUD_ATTEMPT = 123;
	private static final int MBUS4_FRAUD_ATTEMPT = 133;

	private static final int MBUS1_CLOCK_ADJUST_FAILED = 104;
	private static final int MBUS2_CLOCK_ADJUST_FAILED = 114;
	private static final int MBUS3_CLOCK_ADJUST_FAILED = 124;
	private static final int MBUS4_CLOCK_ADJUST_FAILED = 134;

	private static final int MBUS1_PERMANENT_ERROR = 105;
	private static final int MBUS2_PERMANENT_ERROR = 115;
	private static final int MBUS3_PERMANENT_ERROR = 125;
	private static final int MBUS4_PERMANENT_ERROR = 135;

	private static final int MBUS1_VALVE_ALARM = 164;
	private static final int MBUS2_VALVE_ALARM = 174;
	private static final int MBUS3_VALVE_ALARM = 184;
	private static final int MBUS4_VALVE_ALARM = 194;


	/**
	 * Default constructor
	 * @param dc
	 */
	public GMetersLog(DataContainer dc){
		this.dcEvents = dc;
	}

	/**
	 * Build up a list of MeterEvents
	 *
	 * @return the list of MeterEvents
	 *
	 * @throws IOException if constructing the dateTime failed
	 */
	public List<MeterEvent> getMeterEvents() throws IOException{
		List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
		int size = this.dcEvents.getRoot().getNrOfElements();
		Date eventTimeStamp = null;
		for(int i = 0; i <= (size-1); i++){
			int eventId = (int)this.dcEvents.getRoot().getStructure(i).getValue(1)&0xFF; // To prevent negative values
			if(isOctetString(this.dcEvents.getRoot().getStructure(i).getElement(0))){
				eventTimeStamp = new AXDRDateTime(OctetString.fromByteArray(dcEvents.getRoot().getStructure(i).getOctetString(0).getArray())).getValue().getTime();
			}
			if(eventTimeStamp != null){
				buildMeterEvent(meterEvents, eventTimeStamp, eventId);
			}
		}
		return meterEvents;
	}

	/**
	 * Add an event to the global eventList
	 *
	 * @param meterEvents
	 * 				- the global eventList
	 *
	 * @param eventTimeStamp
	 * 				- the timestamp from the event
	 *
	 * @param eventId
	 * 				- the eventId from the device
	 */
	private void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {

		switch(eventId){
		case MBUS1_REPLACE_BATTERY : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "MBus 1 Battery LOW"));}break;
		case MBUS1_FRAUD_ATTEMPT : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.TAMPER, eventId, "Mbus 1 Fraud attempt"));}break;
		case MBUS1_CLOCK_ADJUST_FAILED : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "MBus 1 clock adjustment failed"));}break;
		case MBUS1_PERMANENT_ERROR : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.FATAL_ERROR, eventId, "MBus 1 Permanent Error"));}break;
		case MBUS1_VALVE_ALARM : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.HARDWARE_ERROR, eventId, "MBus 1 Valve alarm"));}break;

		case MBUS2_REPLACE_BATTERY : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "MBus 2 Battery LOW"));}break;
		case MBUS2_FRAUD_ATTEMPT : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.TAMPER, eventId, "MBus 2 Fraud attempt"));}break;
		case MBUS2_CLOCK_ADJUST_FAILED : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "MBus 2 clock adjustment failed"));}break;
		case MBUS2_PERMANENT_ERROR : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.FATAL_ERROR, eventId, "MBus 2 Permanent Error"));}break;
		case MBUS2_VALVE_ALARM : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.HARDWARE_ERROR, eventId, "MBus 2 Valve alarm"));}break;

		case MBUS3_REPLACE_BATTERY : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "MBus 3 Battery LOW"));}break;
		case MBUS3_FRAUD_ATTEMPT : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.TAMPER, eventId, "MBus 3 Fraud attempt"));}break;
		case MBUS3_CLOCK_ADJUST_FAILED : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "MBus 3 clock adjustment failed"));}break;
		case MBUS3_PERMANENT_ERROR : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.FATAL_ERROR, eventId, "MBus 3 Permanent Error"));}break;
		case MBUS3_VALVE_ALARM : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.HARDWARE_ERROR, eventId, "MBus 3 Valve alarm"));}break;

		case MBUS4_REPLACE_BATTERY : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "MBus 4 Battery LOW"));}break;
		case MBUS4_FRAUD_ATTEMPT : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.TAMPER, eventId, "MBus 4 Fraud attempt"));}break;
		case MBUS4_CLOCK_ADJUST_FAILED : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "MBus 4 clock adjustment failed"));}break;
		case MBUS4_PERMANENT_ERROR : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.FATAL_ERROR, eventId, "MBus 4 Permanent Error"));}break;
		case MBUS4_VALVE_ALARM : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.HARDWARE_ERROR, eventId, "MBus 4 Valve alarm"));}break;


		default:{
			meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId));
		}break;
		}
	}

	private boolean isOctetString(Object element) {
		return (element instanceof com.energyict.dlms.OctetString)?true:false;
	}


}
