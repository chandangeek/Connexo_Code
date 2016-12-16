package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.calendar.rest.CalendarInfoFactory;
import com.elster.jupiter.usagepoint.calendar.CalendarOnUsagePoint;

import com.google.common.collect.Range;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Instant;

@Component(name = "com.elster.jupiter.mdm.usagepoint.data.rest.CalendarOnUsagePointInfoFactory",
        immediate = true,
        service = CalendarOnUsagePointInfoFactory.class)
public class CalendarOnUsagePointInfoFactory {

    private volatile CalendarInfoFactory calendarInfoFactory;

    public CalendarOnUsagePointInfoFactory() {
    }

    @Inject
    public CalendarOnUsagePointInfoFactory(CalendarInfoFactory calendarInfoFactory) {
        this.calendarInfoFactory = calendarInfoFactory;
    }

    CalendarOnUsagePointInfo from(CalendarOnUsagePoint calendarOnUsagePoint) {
        CalendarOnUsagePointInfo info = new CalendarOnUsagePointInfo();
        info.calendar = calendarInfoFactory.detailedFromCalendar(calendarOnUsagePoint.getCalendar());
        Range<Instant> range = calendarOnUsagePoint.getRange();
        info.fromTime = range.lowerEndpoint().toEpochMilli();
        info.toTime = range.hasUpperBound() ? range.upperEndpoint().toEpochMilli() : null;
        info.usagePointId = calendarOnUsagePoint.getUsagePoint().getId();
        return info;
    }

    @Reference
    public void setCalendarInfoFactory(CalendarInfoFactory calendarInfoFactory) {
        this.calendarInfoFactory = calendarInfoFactory;
    }
}
