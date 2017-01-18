package com.energyict.mdc.common.interval;

import com.elster.jupiter.time.TimeDuration;

import java.time.Duration;
import java.time.Period;
import java.time.temporal.TemporalAmount;

/**
 * Provides utility methods for TemporalAmount classes.
 * <p>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 17/01/2017 - 10:18
 */
public class Temporals {

    private static final int SECONDS_IN_HOUR = 3600;
    private static final int SECONDS_IN_MINUTE = 60;

    // Hide utility class constructor
    private Temporals() {
    }

    public static TimeDuration toTimeDuration(TemporalAmount temporalAmount) {
        if (temporalAmount instanceof Duration) {
            Duration duration = (Duration) temporalAmount;
            return toTimeDuration(duration);
        } else {
            return toTimeDuration((Period) temporalAmount);
        }
    }

    public static TemporalAmount toTemporalAmount(TimeDuration timeDuration) {
        switch (timeDuration.getTimeUnit()) {
            case YEARS: {
                return Period.ofYears(timeDuration.getCount());
            }
            case MONTHS: {
                return Period.ofMonths(timeDuration.getCount());
            }
            case DAYS: {
                return Period.ofDays(timeDuration.getCount());
            }
            case HOURS: {
                return Duration.ofHours(timeDuration.getCount());
            }
            case MINUTES: {
                return Duration.ofMinutes(timeDuration.getCount());
            }
            case SECONDS: {
                return Duration.ofSeconds(timeDuration.getCount());
            }
            case MILLISECONDS: {
                return Duration.ofMillis(timeDuration.getCount());
            }
            default: {
                throw new IllegalArgumentException("Unsupported TimeDuration unit " + timeDuration.getTimeUnitCode());
            }
        }
    }

    private static TimeDuration toTimeDuration(Duration duration) {
        int seconds = (int) duration.getSeconds();
        if (duration.getSeconds() % SECONDS_IN_HOUR == 0) {
            return TimeDuration.hours(seconds / SECONDS_IN_HOUR);
        } else if (duration.getSeconds() % SECONDS_IN_MINUTE == 0) {
            return TimeDuration.minutes(seconds / SECONDS_IN_MINUTE);
        } else {
            return TimeDuration.seconds(seconds);
        }
    }

    private static TimeDuration toTimeDuration(Period period) {
        if (period.getYears() != 0) {
            // Ignore rest of fields
            return TimeDuration.years(period.getYears());
        } else if (period.getMonths() != 0) {
            // Ignore rest of fields
            return TimeDuration.months(period.getMonths());
        } else {
            // Ignore rest of fields
            return TimeDuration.days(period.getDays());
        }
    }
}