/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.calendar.impl.ServerCalendar;
import com.elster.jupiter.metering.aggregation.MetrologyContractCalculationIntrospector;

import java.time.Instant;
import java.time.Year;
import java.time.ZoneId;

/**
 * Zoned view of a {@link MetrologyContractCalculationIntrospector.CalendarUsage}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-03-24 (11:05)
 */
class ZonedCalendarUsage {
    private final ZoneId zoneId;
    private final MetrologyContractCalculationIntrospector.CalendarUsage calendarUsage;
    private final ServerCalendar.ZonedView zonedView;

    ZonedCalendarUsage(ZoneId zoneId, Year startYear, Year endYear, MetrologyContractCalculationIntrospector.CalendarUsage calendarUsage) {
        this.zoneId = zoneId;
        this.calendarUsage = calendarUsage;
        this.zonedView = calendarUsage.getCalendar().atZone(zoneId, startYear, endYear);
    }

    boolean contains(Instant timestamp) {
        return this.calendarUsage.getRange().contains(timestamp);
    }

}