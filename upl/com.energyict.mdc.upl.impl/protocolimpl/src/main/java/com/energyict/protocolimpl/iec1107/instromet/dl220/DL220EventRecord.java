/**
 * 
 */
package com.energyict.protocolimpl.iec1107.instromet.dl220;

import java.util.Date;
import java.util.TimeZone;

import com.energyict.protocolimpl.iec1107.instromet.dl220.objects.ClockObject;

/**
 * Defines one Event record
 * 
 * @author gna
 * @since 10-mrt-2010
 *
 */
public class DL220EventRecord implements DL220Record {
	
	private final String record;
	private final DL220EventRecordConfig derc;
	private final TimeZone timeZone;
	
	/**
	 * Default constructor
	 * 
	 * @param record
	 * 			- the record received from the device
	 * 
	 * @param derc
	 * 			- the Event record configuration
	 */
	public DL220EventRecord(String record, DL220EventRecordConfig derc, TimeZone timeZone){
		this.record = record;
		this.derc = derc;
		this.timeZone = timeZone;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Date getEndTime(){
		return ClockObject.parseCalendar(DL220Utils.getTextBetweenBracketsStartingFrom(this.record, derc.getTimeIndex()), timeZone).getTime();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getEvent(){
		return DL220Utils.getTextBetweenBracketsStartingFrom(this.record, derc.getEventIndex());
	}

	/**
	 * {@inheritDoc}
	 */
	public String getStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getValue() {
		// TODO Auto-generated method stub
		return null;
	}
}
