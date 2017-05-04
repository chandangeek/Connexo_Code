/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.CM32;

import com.energyict.protocols.util.ProtocolUtils;

import java.util.Calendar;
import java.util.Date;

public class TimeTable {

	private CM32 cm32Protocol;
    private Date time;

	public TimeTable(CM32 cm32Protocol) {
		this.cm32Protocol = cm32Protocol;
	}


	public void parse(byte[] data) {
		Calendar c = ProtocolUtils.getCalendar(cm32Protocol.getTimeZone());
		int sec = data[0];
		int min = data[1];
		int hour = data[2];
		int day = data[3];
		int month = data[4];
		int year = data[5];

        c.set( Calendar.YEAR, 2000 + year );
        c.set( Calendar.MONTH, month-1 );
        c.set( Calendar.DAY_OF_MONTH, day );
        c.set( Calendar.HOUR_OF_DAY, hour);
        c.set( Calendar.MINUTE, min);
        c.set( Calendar.SECOND, sec);
        this.time = c.getTime();
	}

	public Date getTime() {
		return time;
	}

}
