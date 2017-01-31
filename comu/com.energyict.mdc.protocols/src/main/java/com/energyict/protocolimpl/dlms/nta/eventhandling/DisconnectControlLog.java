/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.nta.eventhandling;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class DisconnectControlLog extends AbstractEvent{
	
	// Disconnect control log
	private static final int EVENT_EVENT_LOG_CLEARED = 255;
	private static final int EVENT_MANUAL_DISCONNECTION = 60;
	private static final int EVENT_MANUAL_CONNECTION = 61;
	private static final int EVENT_REMOTE_DISCONNECTION = 62;
	private static final int EVENT_REMOTE_CONNECTION = 63;
	private static final int EVENT_LOCAL_DISCONNECTION = 64;
	private static final int EVENT_LIMITER_THRESHOLD_EXCEEDED = 65;
	private static final int EVENT_LIMITER_THRESHOLD_OK = 66;
	private static final int EVENT_LIMITER_THRESHOLD_CHANGED = 67;
	
	public DisconnectControlLog(TimeZone timeZone, DataContainer dc){
        super(dc, timeZone);
	}

    /**
     * <b><u>Note:</u></b> This will do nothing
     * Build a list of MeterEvents
     */
    @Override
    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId) {
        // This does not do anything. We created a custom buildMeterEvent method because we have an extra argument
    }

    @Override
    public List<MeterEvent> getMeterEvents() throws IOException{
		List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
		int size = this.dcEvents.getRoot().getNrOfElements();
		Date eventTimeStamp = null;
		for(int i = 0; i <= (size-1); i++){
			int eventId = (int)this.dcEvents.getRoot().getStructure(i).getValue(1)&0xFF; // To prevent negative values
			String threshold = "Unknown";
			//fixed it for the Iskra 2009 meter
			if(this.dcEvents.getRoot().getStructure(i).getElements().length == 3){
				threshold = Integer.toString(this.dcEvents.getRoot().getStructure(i).getInteger(2));
			}
			if(isOctetString(this.dcEvents.getRoot().getStructure(i).getElement(0))){
				eventTimeStamp = new AXDRDateTime(OctetString.fromByteArray(dcEvents.getRoot().getStructure(i).getOctetString(0).getArray())).getValue().getTime();
			}
			if(eventTimeStamp != null){
				buildMeterEvent(meterEvents, eventTimeStamp, eventId, threshold);
			}
		}
		return meterEvents;
	}


	protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId, String threshold) {
		if( !ExtraEvents.extraEvents.containsKey(new Integer(eventId)) ){
			switch(eventId){
			case EVENT_EVENT_LOG_CLEARED : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.CLEAR_DATA, eventId, "Disconnect control event log profile cleared."));}break;
			case EVENT_MANUAL_DISCONNECTION : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.MANUAL_DISCONNECTION, eventId, "The disconnector has been manually disconnected - Active threshold value: " + threshold));}break;
			case EVENT_MANUAL_CONNECTION : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.MANUAL_CONNECTION, eventId, "The disconnector has been manually connected - Active threshold value: " + threshold));}break;
			case EVENT_REMOTE_DISCONNECTION : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.REMOTE_DISCONNECTION, eventId, "The disconnector has been remotely disconnected - Active threshold value: " + threshold));}break;
			case EVENT_REMOTE_CONNECTION : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.REMOTE_CONNECTION, eventId, "The disconnector has been remotely connected - Active threshold value: " + threshold));}break;
			case EVENT_LOCAL_DISCONNECTION : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.LOCAL_DISCONNECTION, eventId, "The disconnector has been locally disconnected (i.e. via the limiter) - Active threshold value: " + threshold));}break;
			case EVENT_LIMITER_THRESHOLD_EXCEEDED : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.LIMITER_THRESHOLD_EXCEEDED, eventId, "The limiter threshold has been exceeded - Active threshold value: " + threshold));}break;
			case EVENT_LIMITER_THRESHOLD_OK : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.LIMITER_THRESHOLD_OK, eventId, "The monitored value of the limiter dropped below the threshold - Active threshold value: " + threshold));}break;
			case EVENT_LIMITER_THRESHOLD_CHANGED : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.LIMITER_THRESHOLD_CHANGED, eventId, "The limiter threshold has been changed - Active threshold value: " + threshold));}break;
			default : {meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, eventId, "Unknown eventcode: " + eventId + " - Active threshold value: " + threshold));}break;
			}
		} else {
			meterEvents.add(ExtraEvents.getExtraEvent(eventTimeStamp, eventId));
		}
	}
}
