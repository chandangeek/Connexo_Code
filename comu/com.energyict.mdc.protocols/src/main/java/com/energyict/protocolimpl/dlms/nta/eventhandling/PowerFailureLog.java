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

/**
 * 
 * @author gna
 * Changes:
 * GNA|20072009| Changed the duration to a long, otherwise you could get negative durations ...
 */

public class PowerFailureLog extends AbstractEvent{
	
	// Power failure log
	public PowerFailureLog(TimeZone timeZone, DataContainer dc){
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
			long duration = this.dcEvents.getRoot().getStructure(i).getValue(1);
			if(duration < 0){
				duration += 0xFFFF;
				duration++;
			}
			if(isOctetString(this.dcEvents.getRoot().getStructure(i).getElement(0))){
				eventTimeStamp = new AXDRDateTime(OctetString.fromByteArray(dcEvents.getRoot().getStructure(i).getOctetString(0).getArray())).getValue().getTime();
			}
			if(eventTimeStamp != null){
				buildMeterEvent(meterEvents, eventTimeStamp, duration);
			}
		}
		return meterEvents;
	}

	protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, long duration) {
		meterEvents.add(new MeterEvent(eventTimeStamp, MeterEvent.PHASE_FAILURE, "Duration of power failure: " + duration));
	}
}
