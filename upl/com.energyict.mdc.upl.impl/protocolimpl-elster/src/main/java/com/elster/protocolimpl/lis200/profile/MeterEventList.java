/**
 * 
 */
package com.elster.protocolimpl.lis200.profile;

import com.elster.utils.lis200.events.EventInterpreter;
import com.energyict.protocol.MeterEvent;

import java.util.*;

/**
 * Functionality to create and maintain {@link MeterEvent}s for LIS200 devices
 * 
 * @author gh
 * @since 21-Apr-2010
 * 
 */
public class MeterEventList {

	private List<MeterEvent> eventList = new ArrayList<MeterEvent>();
	
	private EventInterpreter eventInterpreter;
	
	/**
	 * Constructor with EventInterpreter to use
	 * 
	 * @param eventInterpreter
	 */
	public MeterEventList(EventInterpreter eventInterpreter) {
		this.eventInterpreter = eventInterpreter; 
	}	

	/** 
	 * Add a event to list
	 * 
	 * @param eventTST - time stamp of event
	 * @param event    - event code
	 */
	public void addSimpleEvent(Date eventTST, int event) {
		MeterEvent me = eventInterpreter.interpretEvent(eventTST, event);
		if (me != null) {
			eventList.add(me);
		}
	}

	/**
	 * Getter for the eventList
	 * 
	 * @return the eventList
	 */
	public List<MeterEvent> getEventList() {
		return eventList;
	}

	/**
	 * Setter for the eventList
	 * 
	 * @param eventList 
	 * 				- the eventList to set
	 */
	protected void setEventList(List<MeterEvent> eventList) {
		this.eventList = eventList;
	}


	/** 
	 * Adds a MeterEvent to the list
	 * 
	 * @param meterEvent
	 */
	public void add(MeterEvent meterEvent) {
		eventList.add(meterEvent);
	}
	
}
