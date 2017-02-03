/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.ids.TimeSeries;

import java.time.ZoneId;
import java.time.temporal.TemporalAmount;

/**
 * Models the entity that links a {@link CalendarImpl} to the
 * persistent {@link TimeSeries} for a specific interval.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-02-02 (16:12)
 */
public interface CalendarTimeSeries {
    TimeSeries timeSeries();
    ServerCalendar calendar();

    boolean matches(TemporalAmount interval, ZoneId zoneId);
    CalendarTimeSeriesImpl initialize(ServerCalendar calendar, TemporalAmount interval, ZoneId zoneId);
    CalendarTimeSeries generate();
}