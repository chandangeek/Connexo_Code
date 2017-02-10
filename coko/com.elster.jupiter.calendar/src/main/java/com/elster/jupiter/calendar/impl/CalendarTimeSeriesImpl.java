/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarTimeSeries;
import com.elster.jupiter.calendar.Event;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;

import com.google.common.collect.Range;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link CalendarTimeSeries} interface
 * that wraps a {@link CalendarTimeSeriesEntity} to delegate some of the work to.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-02-07 (15:06)
 */
class CalendarTimeSeriesImpl implements CalendarTimeSeries {
    static final String FIELD_SPEC_CODE_NAME = "code";

    private final CalendarTimeSeriesEntity entity;

    CalendarTimeSeriesImpl(CalendarTimeSeriesEntity entity) {
        this.entity = entity;
    }

    @Override
    public long getId() {
        return this.entity.timeSeries().getId();
    }

    @Override
    public Calendar getCalendar() {
        return this.entity.calendar();
    }

    @Override
    public TemporalAmount getInterval() {
        return this.entity.timeSeries().interval();
    }

    @Override
    public ZoneId getZoneId() {
        return this.entity.timeSeries().getZoneId();
    }

    @Override
    public List<Event> getEvents(Range<Instant> interval) {
        return this.entity
                .timeSeries()
                .getEntries(interval)
                .stream()
                .map(this::toEvent)
                .collect(Collectors.toList());
    }

    private Event toEvent(TimeSeriesEntry timeSeriesEntry) {
        long timeSeriesEventCode = timeSeriesEntry.getLong(0);
        return this.entity
                    .calendar()
                    .getEvents()
                    .stream()
                    .filter(event -> event.getCode() == timeSeriesEventCode)
                    .findAny()
                    .orElseThrow(() -> new IllegalStateException("Calendar time series generation produced event code (value=" + timeSeriesEventCode + ") that does not exist in the calendar(id=" + this.entity.calendar().getId() + ", name=" + this.entity.calendar().getName() + ")"));
    }

    @Override
    public Optional<Event> getEvent(Instant when) {
        return this.entity.timeSeries().getEntry(when).map(this::toEvent);
    }

    @Override
    @SuppressWarnings("unchecked")
    public SqlFragment joinSql(TimeSeries timeSeries, Event event, Range<Instant> interval, Pair<String, String>... fieldSpecAndAliasNames) {
        SqlBuilder builder = new SqlBuilder("SELECT ts.* FROM (");
        builder.add(this.entity.timeSeries().getRawValuesSql(interval, Pair.of(FIELD_SPEC_CODE_NAME, "Value")));
        builder.append(") cal, (");
        builder.add(timeSeries.getRawValuesSql(interval, fieldSpecAndAliasNames));
        builder.append(") ts WHERE cal.utcstamp = ts.utcstamp and cal.value = ");
        builder.addLong(event.getCode());
        return builder;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.getId());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        CalendarTimeSeriesImpl that = (CalendarTimeSeriesImpl) other;
        return this.getId() == that.getId();
    }

}