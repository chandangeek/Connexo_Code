/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.ids.StorerStats;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.TimeSeriesDataStorer;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
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
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAmount;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides an implementation for the {@link CalendarTimeSeriesEntity} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-02-02 (16:26)
 */
class CalendarTimeSeriesEntityImpl implements CalendarTimeSeriesEntity, PersistenceAware {

    private static final int SECONDS_IN_HOUR = 3600;
    private static final int SECONDS_IN_MINUTE = 60;

    private static final Logger LOGGER = Logger.getLogger(CalendarService.class.getName());

    private Reference<ServerCalendar> calendar = ValueReference.absent();
    private TimeDuration interval;
    @Size(min = 1, max = 64)
    private String zoneIdString;
    private ZoneId zoneId;
    private Reference<TimeSeries> timeSeries = ValueReference.absent();

    private ServerCalendarService calendarService;

    @Inject
    CalendarTimeSeriesEntityImpl(ServerCalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @Override
    public void postLoad() {
        this.zoneId = ZoneId.of(this.zoneIdString);
    }

    CalendarTimeSeriesEntityImpl initialize(ServerCalendar calendar, TemporalAmount interval, ZoneId zoneId) {
        this.calendar.set(calendar);
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
        return this.timeSeries.get();
    }

    @Override
    public ServerCalendar calendar() {
        return this.calendar.get();
    }

    @Override
    public boolean matches(TemporalAmount interval, ZoneId zoneId) {
        return this.interval.equals(toTimeDuration(interval)) && this.zoneId.equals(zoneId);
    }

    CalendarTimeSeriesEntity generate(Year endYear) {
        ServerCalendar calendar = this.calendar();
        TimeSeries newlyCreated =
                this.calendarService
                    .getVault()
                    .createRegularTimeSeries(
                            this.calendarService.getRecordSpec(),
                            this.zoneId,
                            this.interval.asTemporalAmount(),
                            0);
        if (Year.now(this.calendarService.getClock()).getValue() >= calendar.getStartYear().getValue()) {
            // Calendar does not start in the future
            ServerCalendar.ZonedView zonedView = calendar.forZone(this.zoneId, calendar.getStartYear());
            TimeSeriesDataStorer storer = this.calendarService.getIdsService().createNonOverrulingStorer();
            newlyCreated
                    .toList(this.initialGenerationRange(endYear))
                    .forEach(instant -> storer.add(newlyCreated, instant, zonedView.eventFor(instant).getCode()));
            LOGGER.log(Level.INFO, () -> "Generated timeseries for calendar(id=" + this.calendar().getId() + ", name=" + this.calendar().getName() + ")");
            this.log(storer.execute());
        }
        this.timeSeries.set(newlyCreated);
        return this;
    }

    private Range<Instant> initialGenerationRange(Year endYear) {
        ZonedDateTime startOfFirstYear = this.calendar().getStartYear().atDay(1).atStartOfDay(this.zoneId);
        ZonedDateTime startOfNextYear = endYear.plusYears(1).atDay(1).atStartOfDay(this.zoneId);
        return Range.closedOpen(startOfFirstYear.toInstant(), startOfNextYear.toInstant());
    }

    @Override
    public void extend() {
        Year nextYear = Year.now(this.calendarService.getClock()).plusYears(1);
        ServerCalendar calendar = this.calendar();
        TimeSeries timeSeries = timeSeries();
        ServerCalendar.ZonedView zonedView = calendar.forZone(this.zoneId, nextYear);
        TimeSeriesDataStorer storer = this.calendarService.getIdsService().createNonOverrulingStorer();
        timeSeries
            .toList(this.extensionGenerationRange(nextYear))
            .forEach(instant -> storer.add(timeSeries, instant, zonedView.eventFor(instant).getCode()));
        LOGGER.log(Level.INFO, () -> "Extended timeseries for calendar(id=" + this.calendar().getId() + ", name=" + this.calendar().getName() + ")");
        this.log(storer.execute());
    }

    private Range<Instant> extensionGenerationRange(Year nextYear) {
        ZonedDateTime startOfNextYear = nextYear.atDay(1).atStartOfDay(this.zoneId);
        ZonedDateTime startOfFollowingYear = nextYear.atDay(1).plusYears(1).atStartOfDay(this.zoneId);
        return Range.closedOpen(startOfNextYear.toInstant(), startOfFollowingYear.toInstant());
    }

    @Override
    public void regenerate() {
        ServerCalendar calendar = this.calendar();
        TimeSeries timeSeries = timeSeries();
        TimeSeriesDataStorer storer = this.calendarService.getIdsService().createOverrulingStorer();
        for (int y = calendar.getStartYear().getValue(); y < calendar.getEndYear().getValue(); y++) {
            Year year = Year.of(y);
            ServerCalendar.ZonedView zonedView = calendar.forZone(this.zoneId, year);
            timeSeries
                .toList(this.oneYearRange(year))
                .forEach(instant -> storer.add(timeSeries, instant, zonedView.eventFor(instant).getCode()));
        }
        LOGGER.log(Level.INFO, () -> "Regenerated timeseries for calendar(id=" + this.calendar().getId() + ", name=" + this.calendar().getName() + ")");
        this.log(storer.execute());
    }

    private Range<Instant> oneYearRange(Year oneYear) {
        ZonedDateTime startOfYear = oneYear.atDay(1).atStartOfDay(this.zoneId);
        ZonedDateTime startOfNextYear = oneYear.atDay(1).plusYears(1).atStartOfDay(this.zoneId);
        return Range.closedOpen(startOfYear.toInstant(), startOfNextYear.toInstant());
    }

    private void log(StorerStats stats) {
        LOGGER.log(Level.INFO, () -> "Inserted " + stats.getEntryCount() + " entries in " + stats.getExecuteTime() + " millis");
    }

}