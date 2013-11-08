package com.energyict.protocolimpl.cm10;

import java.util.Calendar;
import java.util.Date;

public class TimeBuilder {
	
	private CM10 cm10Protocol;
	private Date time;
	
	public TimeBuilder(CM10 cm10Protocol) {
		this.cm10Protocol = cm10Protocol;
	}
	
	public Date getTime() {
		return time;
	}
	
	//63 11 21 00 00 00 00 00 00 00 1C 1C 0B 0E 07 08 0B
	
	public void parse(byte[] data) {
		int seconds = (int) (data[10] & 0xFF);
		int minutes = (int) (data[11] & 0xFF);
		int hours = (int) (data[12] & 0xFF);
		int day = (int) (data[13] & 0xFF);
		int month = (int) (data[14] & 0xFF);
		int year = (int) (data[15] & 0xFF);
		Calendar cal = Calendar.getInstance(cm10Protocol.getTimeZone());
		cal.set(Calendar.YEAR, 2000 + year);
		cal.set(Calendar.MONTH, month - 1);
		cal.set(Calendar.DATE, day);
		cal.set(Calendar.HOUR_OF_DAY, hours);
		cal.set(Calendar.MINUTE, minutes);
		cal.set(Calendar.SECOND, seconds);
		cal.set(Calendar.MILLISECOND, 0);
		this.time = cal.getTime();
	}

}
