package com.energyict.protocolimpl.iec1107.instromet.dl220.profile;

import com.energyict.protocolimpl.iec1107.instromet.dl220.DL220Record;
import com.energyict.protocolimpl.iec1107.instromet.dl220.DL220RecordConfig;
import com.energyict.protocolimpl.iec1107.instromet.dl220.DL220Utils;
import com.energyict.protocolimpl.iec1107.instromet.dl220.objects.ClockObject;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

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
	 * @throws IOException if parsing the raw object configuration failed
	 */
	public Date getEndTime() throws IOException{
		return ClockObject.parseCalendar(DL220Utils.getTextBetweenBracketsFromIndex(this.record, dirc.getTimeIndex()), timeZone).getTime();
	}
	
	/**
	 * {@inheritDoc}
	 * @param index 
	 * @throws IOException if parsing the raw object configuration failed
	 */
	public String getValue(int index) throws IOException{
		return DL220Utils.getTextBetweenBracketsFromIndex(this.record, dirc.getValueIndex(index));
	}
	
	/**
	 * {@inheritDoc}
	 * @throws IOException if parsing the raw object configuration failed
	 */
	public String getStatus() throws IOException{
		return DL220Utils.getTextBetweenBracketsFromIndex(this.record, dirc.getStatusIndex());
	}
	
	/**
	 * {@inheritDoc}
	 * @throws IOException if parsing the raw object configuration failed
	 */
	public String getEvent() throws IOException{
		return DL220Utils.getTextBetweenBracketsFromIndex(this.record, dirc.getEventIndex());
	}
}	
