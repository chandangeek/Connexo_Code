package com.energyict.protocolimpl.iec1107.instromet.dl220;

import java.util.Date;
import java.util.TimeZone;

import com.energyict.protocolimpl.iec1107.instromet.dl220.objects.ClockObject;

/**
 * Defines one interval record
 * 
 * @author gna
 * @since 8-mrt-2010
 *
 */
public class DL220IntervalRecord implements DL220Record {

	private final String record;
	private final DL220RecordConfig dirc;
	private final TimeZone timeZone;
	
	/**
	 * Default constructor
	 * 
	 * @param record
	 * 			- the record received from the device
	 * 
	 * @param dirc
	 * 			- the record configuration
	 */
	public DL220IntervalRecord(String record, DL220RecordConfig dirc, TimeZone timeZone){
		this.record = record;
		this.dirc = dirc;
		this.timeZone = timeZone;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Date getEndTime(){
		return ClockObject.parseCalendar(DL220Utils.getTextBetweenBracketsStartingFrom(this.record, dirc.getTimeIndex()), timeZone).getTime();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getValue(){
		return DL220Utils.getTextBetweenBracketsStartingFrom(this.record, dirc.getValueIndex());
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getStatus(){
		return DL220Utils.getTextBetweenBracketsStartingFrom(this.record, dirc.getStatusIndex());
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getEvent(){
		return DL220Utils.getTextBetweenBracketsStartingFrom(this.record, dirc.getEventIndex());
	}
}	
