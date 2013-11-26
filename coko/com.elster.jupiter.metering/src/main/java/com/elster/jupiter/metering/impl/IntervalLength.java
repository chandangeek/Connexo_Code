package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.ids.IntervalLengthUnit;
import com.elster.jupiter.metering.ReadingType;
import com.google.common.base.Optional;

import org.joda.time.DateTimeConstants;

import java.util.Arrays;

import static com.elster.jupiter.ids.IntervalLengthUnit.*;

final class IntervalLength {
	private static final int[] VALIDMINUTEVALUES = { 1 , 2 , 3, 5 , 10 , 15 , 20, 30 , 60 };
	private static final int[] MINUTEVALUESCIMCODES = { 3 , 10 , 14 , 6 , 1, 2 , 31, 5 , 7 };
	private static final int DAYCIMCODE = 11;
	private static final int MONTHCIMCODE = 13;
    private static final long NOMINAL_DAYS_PER_MONTH = 30L;

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
	
	static Optional<IntervalLength> from(ReadingType readingType) {
		switch (readingType.getMacroPeriod()) {
			case MONTHLY:
				return Optional.of(ofMonth());
			case DAILY:
				return Optional.of(ofDay());
			default:       
		}
		if (readingType.getMeasuringPeriod() == TimeAttribute.HOUR24) {
			return Optional.of(ofDay());
		}
		int minutes = readingType.getMeasuringPeriod().getMinutes();
		return minutes == 0 ? Optional.<IntervalLength>absent() : Optional.of(ofMinutes(minutes));	
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
				return MONTHCIMCODE;
			case DAY:
				return DAYCIMCODE;
			case MINUTE:
				return MINUTEVALUESCIMCODES[Arrays.binarySearch(VALIDMINUTEVALUES, length)];
		}
		throw new IllegalStateException();
	}
	
	long getLengthInSeconds() {
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
