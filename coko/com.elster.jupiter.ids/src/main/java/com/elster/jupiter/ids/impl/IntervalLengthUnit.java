/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.ids.impl;

import java.time.Duration;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.Calendar;

public enum IntervalLengthUnit {
	MINUTE (ChronoUnit.MINUTES, Calendar.MINUTE) {
		public TemporalAmount amount(int length) {
			return Duration.ofMinutes(length);
		}
	},
	DAY (ChronoUnit.DAYS, Calendar.DAY_OF_YEAR) {
		public TemporalAmount amount(int length) {
			return Period.ofDays(length);
		}
	},
	MONTH (ChronoUnit.MONTHS, Calendar.MONTH) {
		public TemporalAmount amount(int length) {
			return Period.ofMonths(length);
		}
	};
	
	private final TemporalUnit unit;
	private final int calendarCode;
	
	private IntervalLengthUnit(TemporalUnit unit, int calendarCode) {
		this.unit = unit;
		this.calendarCode = calendarCode;
	}
	
	public TemporalUnit getUnit() {
		return unit;
	}

    public int getCalendarCode() {
    	return calendarCode;
    }
    
    abstract public TemporalAmount amount(int length);
}
