/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.siemenss4s.objects;

import com.energyict.protocols.util.ProtocolUtils;

import java.util.Calendar;

/**
 * The dateTime values from the S4s device
 * <b>NOTE: We assume the meter stores his time in GMT ...</b>
 *
 * @author gna
 *
 */
public class S4sDateTime {

	private byte[] date;
	private byte[] time;

	private final static int SECONDS 	= 2;
	private final static int MINUTES 	= 1;
	private final static int HOURES 	= 0;
	private final static int MONTHS		= 2;
	private final static int YEARS		= 1;
	private final static int DAYS		= 0;

	/**
	 * Creates a new instance of the dateTime object
	 * @param date
	 * @param time
	 */
	public S4sDateTime(byte[] date, byte[] time){
		this.date = S4sObjectUtils.getAsciiConvertedDecimalByteArray(S4sObjectUtils.revertByteArray(date));
		this.time = S4sObjectUtils.getAsciiConvertedDecimalByteArray(S4sObjectUtils.revertByteArray(time));
	}

	/**
	 * Construct a GMT calendar with the meterTime
	 * NOTE: In the documentation is never mentioned about a timeZone, so we assume we receive everything in GMT
	 * @return a Calendar with the current MeterTime
	 */
	public Calendar getMeterTime(){
		Calendar cal = ProtocolUtils.getCleanGMTCalendar();
		int year = (date[YEARS] >= 90)?(1900 + date[YEARS]):(2000 + date[YEARS]);
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, date[MONTHS]-1); // -1 because javaMonth starts from zero ...
		cal.set(Calendar.DAY_OF_MONTH, date[DAYS]);
		cal.set(Calendar.HOUR_OF_DAY, time[HOURES]);
		cal.set(Calendar.MINUTE, time[MINUTES]);
		cal.set(Calendar.SECOND, time[SECONDS]);
		return cal;
	}

}
