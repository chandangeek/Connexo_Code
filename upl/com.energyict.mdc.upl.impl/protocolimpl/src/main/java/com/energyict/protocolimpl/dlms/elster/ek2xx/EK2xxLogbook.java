package com.energyict.protocolimpl.dlms.elster.ek2xx;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.energyict.protocol.MeterEvent;

public class EK2xxLogbook {

	private static final int DEBUG	= 0;
	private List meterEvents 		= new ArrayList(0);
	
	/*
	 * Constructors
	 */

	public EK2xxLogbook() {}
	
	/*
	 * Private getters, setters and methods
	 */

	private String getEventDescription(int protocolCode) {
		return EK2xxEvents.getEventDescription(protocolCode);
	}
	
	/*
	 * Public methods
	 */

	public void addMeterEvent(MeterEvent meterEvent) {
		if (DEBUG >= 1)
			System.out.println("Adding meter event: " + meterEvent.toString());
		meterEvents.add(meterEvent);
	}
	
	public void addMeterEvent(Date eventTime, int eiCode, int protocolCode, String message) {
		MeterEvent meterEvent = new MeterEvent(eventTime, eiCode, protocolCode, message);
		addMeterEvent(meterEvent);
	}
	
	public void addMeterEvent(Date eventTime, int eiCode, int protocolCode) {
		MeterEvent meterEvent = new MeterEvent(eventTime, eiCode, protocolCode, getEventDescription(protocolCode));
		addMeterEvent(meterEvent);
	}

	public void clearLogbook() {
		meterEvents.clear();
	}
	
	/*
	 * Public getters and setters
	 */

	public List getMeterEvents() {
		return meterEvents;
	}

}
