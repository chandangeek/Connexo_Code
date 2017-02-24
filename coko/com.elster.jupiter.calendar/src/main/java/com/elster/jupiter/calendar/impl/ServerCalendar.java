/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.Event;

import java.time.Instant;
import java.time.Year;
import java.time.ZoneId;
import java.util.List;

/**
 * Adds behavior to {@link Calendar} that is restricted to server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-02-02 (16:24)
 */
public interface ServerCalendar extends Calendar {

    List<CalendarTimeSeriesEntity> getCachedTimeSeries();

    /**
     * Returns a view on this Calendar where all time zone neutral
     * information is mapped to the specified ZoneId within the specified year.
     * All {@link com.elster.jupiter.calendar.RecurrentExceptionalOccurrence}s
     * are converted to {@link com.elster.jupiter.calendar.FixedExceptionalOccurrence}s.
     * All {@link com.elster.jupiter.calendar.RecurrentPeriodTransitionSpec}s
     * are converted to {@link com.elster.jupiter.calendar.FixedPeriodTransitionSpec}s.
     * The occurrence of all {@link com.elster.jupiter.calendar.PeriodTransition}s
     * are mapped to the ZoneId.
     *
     * @param zoneId The ZoneId
     * @param year The year
     * @return The ZonedView
     */
    ZonedView forZone(ZoneId zoneId, Year year);

    /**
     * Extends the cached TimeSeries with one additional year.
     *
     * @param timeSeriesId The id of the existing cached TimeSeries
     */
    void extend(long timeSeriesId);

    /**
     * Bumps the end year (as part of extending the cached timeseries).
     */
    void bumpEndYear();

    void regenerateCachedTimeSeries();

    interface ZonedView {
        Event eventFor(Instant instant);
    }

}