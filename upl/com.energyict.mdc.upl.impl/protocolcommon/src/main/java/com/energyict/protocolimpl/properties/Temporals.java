package com.energyict.protocolimpl.properties;

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

    private static final long MILLIS_IN_SECOND = 1000L;
    private static final long AVERAGE_SECONDS_IN_DAY = 86_400L;
    private static final BigDecimal AVERAGE_DAYS_IN_YEAR = new BigDecimal("365.25");
    private static final BigDecimal AVERAGE_DAYS_IN_MONTH = new BigDecimal("30.4375");  // average days in year // 12

    public static long toSeconds(TemporalAmount temporalAmount) {
        if (temporalAmount instanceof Duration) {
            Duration duration = (Duration) temporalAmount;
            return toSeconds(duration);
        } else {
            return toSeconds((Period) temporalAmount);
        }
    }

    private static long toSeconds(Duration duration) {
        return duration.getSeconds();
    }

    private static long toSeconds(Period period) {
        long years = new BigDecimal(period.getYears()).multiply(AVERAGE_DAYS_IN_YEAR).multiply(new BigDecimal(AVERAGE_SECONDS_IN_DAY)).longValue();
        long months = new BigDecimal(period.getMonths()).multiply(AVERAGE_DAYS_IN_MONTH).multiply(new BigDecimal(AVERAGE_SECONDS_IN_DAY)).longValue();
        long days = period.getDays() * AVERAGE_SECONDS_IN_DAY;
        return years + months + days;
    }

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
        return toSeconds(period) * MILLIS_IN_SECOND;
    }

    // Hide utility class constructor
    private Temporals() {}
}