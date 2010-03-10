/**
 * 
 */
package com.energyict.protocolimpl.iec1107.instromet.dl220;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.energyict.protocol.MeterEvent;

/**
 * Functionality to create and maintain {@link MeterEvent}s
 * 
 * @author gna
 * @since 10-mrt-2010
 *
 */
public class DL220MeterEventList {
	
	private static final int VALUE_ARCHIVE1_CHANGED = 33538;	// 0x8302
	
	List<MeterEvent> eventList = new ArrayList<MeterEvent>();

	/**
	 * Create an event entry with from the given intervalrecord
	 * 
	 * @param dir 
	 * 			- the {@link DL220IntervalRecord}
	 */
	public void addRawEvent(DL220IntervalRecord dir) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Add/Create a {@link MeterEvent} from the given input
	 * 
	 * @param eventTimeStamp
	 * 			- the event timeStamp
	 * 
	 * @param eventId
	 * 			- the eventId
	 */
	protected void addMeterEventToList(Date eventTimeStamp, String eventId){
		switch(convertStringEventIdToInteger(eventId)){
		case VALUE_ARCHIVE1_CHANGED:{
			eventList.add(new MeterEvent(eventTimeStamp, MeterEvent.OTHER, convertStringEventIdToInteger(eventId), ""));
		}break;
		}
	}
	
	/**
	 * Convert the Hexadecimal eventId to an {@link Integer}
	 * 
	 * @param eventId
	 * 			- the eventId as a String
	 * 
	 * @return the same eventId as an Integer
	 */
	protected static int convertStringEventIdToInteger(String eventId){
		return -1;
	}

}
