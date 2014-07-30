package com.elster.jupiter.ids;

import org.joda.time.DateTimeConstants;

import java.util.Arrays;

import static com.elster.jupiter.ids.IntervalLengthUnit.*;

public final class IntervalLength {
	private static final int[] VALIDMINUTEVALUES = { 1 , 2 , 3, 5 , 10 , 15 , 20, 30 , 60 };
	private static final long NOMINAL_DAYS_PER_MONTH = 30L;

    private final int length;
	private final IntervalLengthUnit unit;
	
	IntervalLength(int length , IntervalLengthUnit unit) {
		this.length = length;
		this.unit = unit;
	}
	
	public static IntervalLength ofMonth() {
		return new IntervalLength(1, MONTH);
	}
	
	public static IntervalLength ofDay() {
		return new IntervalLength(1, DAY);
	}
	
	public static IntervalLength ofMinutes(int minutes) {
		int result = Arrays.binarySearch(VALIDMINUTEVALUES, minutes);
		if (result < 0) {
			throw new IllegalArgumentException("" + minutes);
		}
		return new IntervalLength(minutes, MINUTE);
	}
	
	public int getLength() {
		return length;
	}

	public IntervalLengthUnit getUnitCode() {
		return unit;
	}
	
	private long getLengthInSeconds() {
		switch(unit) {
			case MONTH:
				return NOMINAL_DAYS_PER_MONTH * DateTimeConstants.SECONDS_PER_DAY * length;
			case DAY:
				return DateTimeConstants.SECONDS_PER_DAY * length;
			case MINUTE:
				return DateTimeConstants.SECONDS_PER_MINUTE * length;
		}
		throw new IllegalStateException();
	}
	
	@Override
	public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof IntervalLength)) {
            return false;
        }
        IntervalLength o = (IntervalLength) other;
        return this.length == o.length && this.unit == o.unit;
    }
	
	@Override 
	public int hashCode() {
		return (int) getLengthInSeconds();
	}

}
