package com.elster.jupiter.ids;

import java.util.Calendar;

public enum IntervalLengthUnit {
	MINUTE (Calendar.MINUTE),
	DAY (Calendar.DAY_OF_YEAR),
	MONTH (Calendar.MONTH);
	
	private final int calendarCode;
	
	private IntervalLengthUnit(int calendarCode) {
		this.calendarCode = calendarCode;
	}
	
	public int getCalendarCode() {
		return calendarCode;
	}
}
