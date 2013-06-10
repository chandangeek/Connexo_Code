package com.elster.jupiter.metering.impl;

import java.util.Arrays;

import com.elster.jupiter.ids.IntervalLengthUnit;

import static com.elster.jupiter.ids.IntervalLengthUnit.*;

class IntervalLength {
	private static final int[] VALIDMINUTEVALUES = { 1 , 2 , 3, 5 , 10 , 15 , 30 , 60 };
	private static final int[] MINUTEVALUESCIMCODES = { 3 , 10 , 14 , 6 , 1, 2 , 5 , 7 };
	private static final int DAYCIMCODE = 11;
	private static final int MONTHCIMCODE = 13;
	
	private final int length;
	private final IntervalLengthUnit unit;
	
	private IntervalLength(int length , IntervalLengthUnit unit) {
		this.length = length;
		this.unit = unit;
	}
	
	static IntervalLength ofMonth() {
		return new IntervalLength(1,MONTH);
	}
	
	static IntervalLength ofDay() {
		return new IntervalLength(1,DAY);
	}
	
	static IntervalLength ofMinutes(int minutes) {
		int result = Arrays.binarySearch(VALIDMINUTEVALUES, minutes);
		if (result < 0) {
			throw new IllegalArgumentException("" + minutes);
		}
		return new IntervalLength(minutes, MINUTE);
	}
	
	static IntervalLength forCimCode(int cimCode) {
		if (cimCode == MONTHCIMCODE)
			return ofMonth();
		if (cimCode == DAYCIMCODE)
			return ofDay();
		for (int i = 0 ; i < MINUTEVALUESCIMCODES.length ; i++) {
			if (MINUTEVALUESCIMCODES[i] == cimCode)
				return ofMinutes(VALIDMINUTEVALUES[i]);
		}
		return null;
	}

	int getLength() {
		return length;
	}

	IntervalLengthUnit getUnitCode() {
		return unit;
	}
	
	int getCimCode() {
		switch(unit) {
			case MONTH:
				return 13;
			case DAY:
				return 11;
			case MINUTE:
				return MINUTEVALUESCIMCODES[Arrays.binarySearch(VALIDMINUTEVALUES, length)];
		}
		throw new IllegalStateException();
	}
	
	long getLengthInSeconds() {
		switch(unit) {
			case MONTH:
				return 30L * 86400L * length;
			case DAY:
				return 86400L * length;
			case MINUTE:
				return 60L * length; 
		}
		throw new IllegalStateException();
	}
	
	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		try {
			IntervalLength o = (IntervalLength) other;
			return this.length == o.length && this.unit == o.unit;
		} catch (ClassCastException ex) {
			return false;
		}
	}
	
	@Override 
	public int hashCode() {
		return (int) getLengthInSeconds();
	}

}
