/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.security.Privileges;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.usagepoint.calendar.CalendarOnUsagePoint;
import com.elster.jupiter.usagepoint.calendar.UsagePointCalendarService;

import javax.annotation.security.RolesAllowed;
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

public class UsagePointCalendarHistoryResource {

    private final CalendarOnUsagePointInfoFactory calendarOnUsagePointInfoFactory;
    private final Clock clock;
    private final ResourceHelper resourceHelper;
    private final UsagePointCalendarService usagePointCalendarService;
    private final CalendarService calendarService;
    private final ExceptionFactory exceptionFactory;
    private final TransactionService transactionService;

    @Inject
    public UsagePointCalendarHistoryResource(CalendarOnUsagePointInfoFactory calendarOnUsagePointInfoFactory, Clock clock, ResourceHelper resourceHelper, UsagePointCalendarService usagePointCalendarService, CalendarService calendarService, ExceptionFactory exceptionFactory, TransactionService transactionService) {
        this.calendarOnUsagePointInfoFactory = calendarOnUsagePointInfoFactory;
        this.clock = clock;
        this.resourceHelper = resourceHelper;
        this.usagePointCalendarService = usagePointCalendarService;
        this.calendarService = calendarService;
        this.exceptionFactory = exceptionFactory;
        this.transactionService = transactionService;
    }

    @GET
    @RolesAllowed(Privileges.Constants.MANAGE_TOU_CALENDARS)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getAllCalendars(@PathParam("name") String usagePointName, @BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter filter) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(usagePointName);
        return usagePointCalendarService.calendarsFor(usagePoint)
                .getCalendars()
                .entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .flatMap(List::stream)
                .sorted(
                        Comparator.<CalendarOnUsagePoint, String>comparing(
                                calendarOnUsagePoint -> calendarOnUsagePoint
                                        .getCalendar()
                                        .getCategory()
                                        .getDisplayName())
                                .thenComparing(
                                        Comparator.<CalendarOnUsagePoint, Instant>comparing(
                                                calendarOnUsagePoint -> calendarOnUsagePoint
                                                        .getRange()
                                                        .lowerEndpoint()
                                        ).reversed()
                                )
                )
                .map(calendarOnUsagePointInfoFactory::from)
                .filter(Objects::nonNull)
                .collect(PagedInfoList.toPagedInfoList("calendars", queryParameters));
    }

}
