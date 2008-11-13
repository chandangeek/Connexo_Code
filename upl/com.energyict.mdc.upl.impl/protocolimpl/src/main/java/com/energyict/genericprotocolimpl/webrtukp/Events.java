package com.energyict.genericprotocolimpl.webrtukp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.energyict.dlms.DataContainer;
import com.energyict.protocol.MeterEvent;

public class Events {
	
	private TimeZone timeZone;
	private Calendar calendar;
	private DataContainer dcEvents;
	
	public Events(TimeZone timeZone, Calendar fromCalendar, DataContainer dc){
		this.timeZone = timeZone;
		this.calendar = fromCalendar;
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
		// TODO Auto-generated method stub
		
	}

	private boolean isOctetString(Object element) {
		return (element instanceof com.energyict.dlms.OctetString)?true:false;
	}
	

}
