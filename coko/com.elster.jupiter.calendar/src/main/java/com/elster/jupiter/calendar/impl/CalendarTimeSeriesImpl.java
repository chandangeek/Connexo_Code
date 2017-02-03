/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.ids.StorerStats;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.TimeSeriesDataStorer;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.time.TimeDuration;

import com.google.common.collect.Range;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAmount;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides an implementation for the {@link CalendarTimeSeries} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-02-02 (16:26)
 */
class CalendarTimeSeriesImpl implements CalendarTimeSeries, PersistenceAware {

    private static final int SECONDS_IN_HOUR = 3600;
    private static final int SECONDS_IN_MINUTE = 60;

    private static final Logger LOGGER = Logger.getLogger(CalendarService.class.getName());

    private ServerCalendar calendar;
    private TimeDuration interval;
    @Size(min = 1, max = 64)
    private String zoneIdString;
    private ZoneId zoneId;
    private TimeSeries timeSeries;

    private ServerCalendarService calendarService;

    @Inject
    CalendarTimeSeriesImpl(ServerCalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @Override
    public void postLoad() {
        this.zoneId = ZoneId.of(this.zoneIdString);
    }

    @Override
    public CalendarTimeSeriesImpl initialize(ServerCalendar calendar, TemporalAmount interval, ZoneId zoneId) {
        this.calendar = calendar;
        this.zoneId = zoneId;
        this.zoneIdString = zoneId.getId();
        this.interval = toTimeDuration(interval);
        return this;
    }


    private static TimeDuration toTimeDuration(TemporalAmount temporalAmount) {
        if (temporalAmount instanceof Duration) {
            return toTimeDuration((Duration) temporalAmount);
        } else {
            return toTimeDuration((Period) temporalAmount);
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

    @Override
    public TimeSeries timeSeries() {
        return this.timeSeries;
    }

    @Override
    public ServerCalendar calendar() {
        return this.calendar;
    }

    @Override
    public boolean matches(TemporalAmount interval, ZoneId zoneId) {
        return this.interval.equals(toTimeDuration(interval)) && this.zoneId.equals(zoneId);
    }

    @Override
    public CalendarTimeSeries generate() {
        this.timeSeries =
                this.calendarService
                    .getVault()
                    .createRegularTimeSeries(
                            this.calendarService.getRecordSpec(),
                            ZoneOffset.UTC,
                            this.interval.asTemporalAmount(),
                            0);
        if (Year.now(this.calendarService.getClock()).getValue() >= this.calendar.getStartYear().getValue()) {
            // Calendar does not start in the future
            ServerCalendar.ZonedView zonedView = this.calendar.forZone(this.zoneId, this.calendar.getStartYear());
            TimeSeriesDataStorer storer = this.calendarService.getIdsService().createNonOverrulingStorer();// Change this to overruling storer to support regenerating after calendar was updated
            this.timeSeries
                    .toList(this.initialGenerationRange())
                    .forEach(instant -> storer.add(this.timeSeries, instant, zonedView.eventFor(instant).getCode()));
            this.log(storer.execute());
        }
        return this;
    }

    private Range<Instant> initialGenerationRange() {
        ZonedDateTime startOfFirstYear = this.calendar.getStartYear().atDay(1).atStartOfDay(this.zoneId);
        ZonedDateTime startOfNextYear = Year.now(this.calendarService.getClock()).atDay(1).plusYears(1).atStartOfDay(this.zoneId);
        return Range.openClosed(startOfFirstYear.toInstant(), startOfNextYear.toInstant());
    }

    private void log(StorerStats stats) {
        LOGGER.log(Level.INFO, () -> "Generated timeseries for calendar(id=" + this.calendar.getId() + ", name=" + this.calendar.getName() + ")");
        LOGGER.log(Level.INFO, () -> "Inserted " + stats.getEntryCount() + " entries in " + stats.getExecuteTime() + " millis");
    }

}