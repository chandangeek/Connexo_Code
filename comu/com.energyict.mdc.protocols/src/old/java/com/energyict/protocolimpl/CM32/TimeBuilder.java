package com.energyict.protocolimpl.CM32;

import java.util.Calendar;
import java.util.Date;

public class TimeBuilder {
	
	private CM32 cm32Protocol;
	private Date time;
	
	public TimeBuilder(CM32 cm32Protocol) {
		this.cm32Protocol = cm32Protocol;
	}
	
	public Date getTime() {
		return time;
	}
	
	//63 11 21 00 00 00 00 00 00 00 1C 1C 0B 0E 07 08 0B
	
	public void parse(byte[] data) {
		int seconds = (int) data[10];
		int minutes = (int) data[11];
		int hours = (int) data[12];
		int day = (int) data[13];
		int month = (int) data[14];
		int year = (int) data[15];
		Calendar cal = Calendar.getInstance(cm32Protocol.getTimeZone());
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
