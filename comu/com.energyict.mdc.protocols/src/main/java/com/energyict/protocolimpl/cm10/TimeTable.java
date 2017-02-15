/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.cm10;

import com.energyict.protocols.util.ProtocolUtils;

import java.util.Calendar;
import java.util.Date;

public class TimeTable {

	private CM10 cm10Protocol;
    private Date time;

	public TimeTable(CM10 cm10Protocol) {
		this.cm10Protocol = cm10Protocol;
	}


	public void parse(byte[] data) {
		Calendar c = ProtocolUtils.getCalendar(cm10Protocol.getTimeZone());
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
        c.set(Calendar.MILLISECOND, 0);
        this.time = c.getTime();
	}

	public Date getTime() {
		return time;
	}

}
