/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar;

import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.sql.SqlFragment;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Optional;

/**
 * Models a time series of {@link Event}s that are produced by a {@link Calendar}
 * for one particular interval and time zone.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-02-07 (15:00)
 */
@ProviderType
public interface CalendarTimeSeries {

    long getId();

    Calendar getCalendar();

    TemporalAmount getInterval();

    ZoneId getZoneId();

    List<Event> getEvents(Range<Instant> interval);

    Optional<Event> getEvent(Instant when);

    /**
     * Returns a SqlFragment that aligns the {@link Event}s of this CalendarTimeSeries
     * with the raw data from the specified TimeSeries in the specified period in time,
     * omitting the raw data that does not align with the specified Event.
     *
     * @param timeSeries The TimeSeries that contains the raw data
     * @param interval The period in time
     * @param fieldSpecAndAliasNames Forwarded to {@link TimeSeries#getRawValuesSql(Range, Pair[])}
     * @return The SqlFragment
     */
    SqlFragment joinSql(TimeSeries timeSeries, Event event, Range<Instant> interval, Pair<String, String>... fieldSpecAndAliasNames);

}