/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.calendar.security.Privileges;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UsagePointCalendarHistoryResource {

    private final CalendarOnUsagePointInfoFactory calendarOnUsagePointInfoFactory;
    private final ResourceHelper resourceHelper;

    @Inject
    public UsagePointCalendarHistoryResource(CalendarOnUsagePointInfoFactory calendarOnUsagePointInfoFactory, ResourceHelper resourceHelper) {
        this.calendarOnUsagePointInfoFactory = calendarOnUsagePointInfoFactory;
        this.resourceHelper = resourceHelper;
    }

    @GET
    @RolesAllowed(Privileges.Constants.MANAGE_TOU_CALENDARS)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getAllCalendars(@PathParam("name") String usagePointName, @BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter filter) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(usagePointName);
        return usagePoint.getUsedCalendars()
                .getCalendars()
                .entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .flatMap(List::stream)
                .sorted(
                        Comparator.<UsagePoint.CalendarUsage, String>comparing(
                                calendarOnUsagePoint -> calendarOnUsagePoint
                                        .getCalendar()
                                        .getCategory()
                                        .getDisplayName())
                                .thenComparing(
                                        Comparator.<UsagePoint.CalendarUsage, Instant>comparing(
                                                calendarOnUsagePoint -> calendarOnUsagePoint
                                                        .getRange()
                                                        .lowerEndpoint()
                                        ).reversed()
                                )
                )
                .map(usage -> calendarOnUsagePointInfoFactory.from(usage, usagePoint))
                .filter(Objects::nonNull)
                .collect(PagedInfoList.toPagedInfoList("calendars", queryParameters));
    }

}
