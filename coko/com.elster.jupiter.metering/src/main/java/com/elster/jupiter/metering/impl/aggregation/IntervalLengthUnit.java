package com.elster.jupiter.metering.impl.aggregation;

import java.time.Duration;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.Calendar;

/**
 * Models the supported unit for the length of an interval.
 * Inspired by the IntervalLengthUnit found in ids.impl
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-05 (08:47)
 */
enum IntervalLengthUnit {

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
    WEEK (ChronoUnit.WEEKS, Calendar.WEEK_OF_YEAR) {
        public TemporalAmount amount(int length) {
            return Period.ofWeeks(length);
        }
    },
    MONTH (ChronoUnit.MONTHS, Calendar.MONTH) {
        public TemporalAmount amount(int length) {
            return Period.ofMonths(length);
        }
    },
    YEAR (ChronoUnit.YEARS, Calendar.YEAR) {
        public TemporalAmount amount(int length) {
            return Period.ofYears(length);
        }
    };

    private final TemporalUnit unit;
    private final int calendarCode;

    IntervalLengthUnit(TemporalUnit unit, int calendarCode) {
        this.unit = unit;
        this.calendarCode = calendarCode;
    }

    abstract TemporalAmount amount(int length);

}