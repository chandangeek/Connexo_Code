package com.energyict.protocolimpl.iec1107.instromet.dl220;

import java.util.Date;

import com.energyict.protocolimpl.iec1107.instromet.dl220.objects.ClockObject;

/**
 * Defines 1 interval record
 * 
 * @author gna
 * @since 8-mrt-2010
 *
 */
public class DL220IntervalRecord {

	private final String record;
	private final DL220IntervalRecordConfig dirc;
	/**
	 * Default constructor
	 * 
	 * @param record
	 * 			- the record received from the device
	 * 
	 * @param dirc
	 * 			- the record configuration
	 */
	public DL220IntervalRecord(String record, DL220IntervalRecordConfig dirc){
		this.record = record;
		this.dirc = dirc;
	}
	
	/**
	 * @return the Interval time
	 */
	public Date getEndTime(){
		return ClockObject.parseCalendar(DL220Utils.getTextBetweenBracketsStartingFrom(this.record, dirc.getTimeIndex())).getTime();
	}
	
	/**
	 * @return the Interval value
	 */
	public String getValue(){
		return DL220Utils.getTextBetweenBracketsStartingFrom(this.record, dirc.getValueIndex());
	}
}
