/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.impl;

import com.elster.jupiter.time.TimeDuration;

import java.time.Duration;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.stream.Stream;

interface TimeDurationFactory<T extends TemporalAmount> {

    TimeDuration from(T temporalAmount);

    class TimeDurationFromDurationFactory implements TimeDurationFactory<Duration> {

        private Stream<TimeDurationFactory<Duration>> factories;

        TimeDurationFromDurationFactory() {
            super();
            this.factories = Stream.of(
                    new TimeDurationFromDurationInHoursFactory(),
                    new TimeDurationFromDurationInMinutesFactory(),
                    new TimeDurationFromDurationInSecondsFactory());
        }

        @Override
        public TimeDuration from(Duration temporalAmount) {
            return this.factories
                    .map(f -> f.from(temporalAmount))
                    .filter(t -> t != null)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unable to convert Duration '" + temporalAmount + "' to TemporalExpression"));
        }

    }

    class TimeDurationFromDurationInHoursFactory implements TimeDurationFactory<Duration> {
        @Override
        public TimeDuration from(Duration duration) {
            if (duration.toHours() != 0) {
                return TimeDuration.hours(Math.toIntExact(duration.toHours()));
            } else {
                return null;
            }
        }
    }

    class TimeDurationFromDurationInMinutesFactory implements TimeDurationFactory<Duration> {
        @Override
        public TimeDuration from(Duration duration) {
            if (duration.toMinutes() != 0) {
                return TimeDuration.minutes(Math.toIntExact(duration.toMinutes()));
            } else {
                return null;
            }
        }
    }

    class TimeDurationFromDurationInSecondsFactory implements TimeDurationFactory<Duration> {

        @Override
        public TimeDuration from(Duration duration) {
            if (duration.getSeconds() != 0) {
                return TimeDuration.seconds(Math.toIntExact(duration.getSeconds()));
            } else {
                return null;
            }
        }
    }

    class TimeDurationFromPeriodFactory implements TimeDurationFactory<Period> {

        private Stream<TimeDurationFactory<Period>> factories;

        TimeDurationFromPeriodFactory() {
            super();
            this.factories = Stream.of(
                    new TimeDurationFromPeriodInMonthsFactory(),
                    new TimeDurationFromPeriodInDaysFactory());
        }

        @Override
        public TimeDuration from(Period period) {
            if (period.getYears() != 0 && period.getDays() != 0) {
                throw new IllegalArgumentException("Years and days are not supported");
            }
            if (period.getMonths() != 0 && period.getDays() != 0) {
                throw new IllegalArgumentException("Months and days are not supported");
            }
            return this.factories
                    .map(f -> f.from(period))
                    .filter(t -> t != null)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unable to convert Period '" + period + "' to TemporalExpression"));
        }
    }

    class TimeDurationFromPeriodInMonthsFactory implements TimeDurationFactory<Period> {
        @Override
        public TimeDuration from(Period period) {
            if (period.toTotalMonths() != 0) {
                return TimeDuration.months(Math.toIntExact(period.toTotalMonths()));
            } else {
                return null;
            }
        }
    }

    class TimeDurationFromPeriodInDaysFactory implements TimeDurationFactory<Period> {
        @Override
        public TimeDuration from(Period period) {
            if (period.getDays() != 0) {
                return TimeDuration.days(Math.toIntExact(period.getDays()));
            } else {
                return null;
            }
        }
    }
}