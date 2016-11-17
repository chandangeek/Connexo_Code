package com.energyict.protocolimplv2.common;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Period;
import java.time.temporal.TemporalAmount;

/**
 * Provides utility methods for TemporalAmount classes.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-16 (15:24)
 */
public final class Temporals {

    private static final long AVERAGE_MILLI_SECONDS_IN_DAY = 86_400_000L;
    private static final BigDecimal AVERAGE_DAYS_IN_YEAR = new BigDecimal("365.25");
    private static final BigDecimal AVERAGE_DAYS_IN_MONTH = new BigDecimal("30.4375");  // average days in year // 12

    public static long toMilliSeconds(TemporalAmount temporalAmount) {
        if (temporalAmount instanceof Duration) {
            Duration duration = (Duration) temporalAmount;
            return toMilliSeconds(duration);
        } else {
            return toMilliSeconds((Period) temporalAmount);
        }
    }

    private static long toMilliSeconds(Duration duration) {
        return duration.toMillis();
    }

    private static long toMilliSeconds(Period period) {
        long yearMillis = new BigDecimal(period.getYears()).multiply(AVERAGE_DAYS_IN_YEAR).multiply(new BigDecimal(AVERAGE_MILLI_SECONDS_IN_DAY)).longValue();
        long monthMillis = new BigDecimal(period.getMonths()).multiply(AVERAGE_DAYS_IN_MONTH).multiply(new BigDecimal(AVERAGE_MILLI_SECONDS_IN_DAY)).longValue();
        long dayMillis = period.getDays() * AVERAGE_MILLI_SECONDS_IN_DAY;
        return yearMillis + monthMillis + dayMillis;
    }

    // Hide utility class constructor
    private Temporals() {}
}