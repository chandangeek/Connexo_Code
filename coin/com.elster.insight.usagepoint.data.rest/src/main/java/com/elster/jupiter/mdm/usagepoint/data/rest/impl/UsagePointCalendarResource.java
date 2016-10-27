package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.usagepoint.calendar.CalendarOnUsagePoint;
import com.elster.jupiter.usagepoint.calendar.UsagePointCalendarService;

import com.google.common.collect.Range;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class UsagePointCalendarResource {

    private final CalendarOnUsagePointInfoFactory calendarOnUsagePointInfoFactory;
    private final Clock clock;
    private final ResourceHelper resourceHelper;
    private final UsagePointCalendarService usagePointCalendarService;

    @Inject
    public UsagePointCalendarResource(CalendarOnUsagePointInfoFactory calendarOnUsagePointInfoFactory, Clock clock, ResourceHelper resourceHelper, UsagePointCalendarService usagePointCalendarService) {
        this.calendarOnUsagePointInfoFactory = calendarOnUsagePointInfoFactory;
        this.clock = clock;
        this.resourceHelper = resourceHelper;
        this.usagePointCalendarService = usagePointCalendarService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getCalendarsOnUsagePoint(@PathParam("mrid") String usagePointMrid, @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByMrIdOrThrowException(usagePointMrid);
        return usagePointCalendarService.calendarsFor(usagePoint)
                .getCalendars()
                .entrySet()
                .stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().getDisplayName()))
                .map(Map.Entry::getValue)
                .map(this::createFrom)
                .filter(Objects::nonNull)
                .collect(PagedInfoList.toPagedInfoList("calendars", queryParameters));
    }

    private CalendarOnUsagePointInfo createFrom(List<CalendarOnUsagePoint> calendars) {
        List<CalendarOnUsagePointInfo> infoList = calendars.stream()
                .filter(calendarOnUsagePoint -> !endsBeforeNow(calendarOnUsagePoint.getRange()))
                .map(calendarOnUsagePointInfoFactory::from)
                .collect(Collectors.toList());
        link(infoList);
        return infoList.isEmpty() ? null : infoList.get(0);
    }

    private void link(List<CalendarOnUsagePointInfo> infoList) {
        CalendarOnUsagePointInfo previous = null;
        for (CalendarOnUsagePointInfo current : infoList) {
            if (previous != null) {
                previous.next = current;
            }
            previous = current;
        }
    }

    private boolean endsBeforeNow(Range<Instant> range) {
        return range.hasUpperBound() && !(range.upperEndpoint().isAfter(clock.instant()));
    }
}
