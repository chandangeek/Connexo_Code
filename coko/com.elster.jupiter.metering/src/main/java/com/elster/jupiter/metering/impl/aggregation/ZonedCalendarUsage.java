/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.calendar.impl.ServerCalendar;
import com.elster.jupiter.metering.UsagePoint;
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
    private final UsagePoint usagePoint;
    private final MetrologyContractCalculationIntrospector.CalendarUsage calendarUsage;
    private final ServerCalendar.ZonedView zonedView;

    ZonedCalendarUsage(UsagePoint usagePoint, ZoneId zoneId, Year startYear, Year endYear, MetrologyContractCalculationIntrospector.CalendarUsage calendarUsage) {
        this.usagePoint = usagePoint;
        this.calendarUsage = calendarUsage;
        this.zonedView = calendarUsage.getCalendar().forZone(zoneId, startYear, endYear);
    }

    UsagePoint getUsagePoint() {
        return this.usagePoint;
    }

    boolean contains(Instant timestamp) {
        return this.calendarUsage.getRange().contains(timestamp);
    }

    boolean sameTimeOfUse(Instant timestamp, int timeOfUse) {
        return this.zonedView.eventFor(timestamp).getCode() == timeOfUse;
    }

}