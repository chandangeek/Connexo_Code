/**
 * 
 */
package com.energyict.protocolimpl.iec1107.instromet.dl220.profile;

import com.energyict.protocolimpl.iec1107.instromet.dl220.DL220Record;
import com.energyict.protocolimpl.iec1107.instromet.dl220.DL220Utils;
import com.energyict.protocolimpl.iec1107.instromet.dl220.objects.ClockObject;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

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
	 * @throws IOException 
	 */
	public Date getEndTime() throws IOException{
		return ClockObject.parseCalendar(DL220Utils.getTextBetweenBracketsFromIndex(this.record, derc.getTimeIndex()), timeZone).getTime();
	}

	/**
	 * {@inheritDoc}
	 * @throws IOException 
	 */
	public String getEvent() throws IOException{
		return DL220Utils.getTextBetweenBracketsFromIndex(this.record, derc.getEventIndex());
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
	public String getValue(int index) {
		// TODO Auto-generated method stub
		return null;
	}
}
