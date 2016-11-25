package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.rest.CalendarInfo;
import com.elster.jupiter.calendar.rest.CalendarInfoFactory;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.usagepoint.calendar.CalendarOnUsagePoint;
import com.elster.jupiter.usagepoint.calendar.UsagePointCalendarService;

import com.google.common.collect.Range;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Currying.use;

public class UsagePointCalendarResource {

    private final CalendarOnUsagePointInfoFactory calendarOnUsagePointInfoFactory;
    private final Clock clock;
    private final ResourceHelper resourceHelper;
    private final UsagePointCalendarService usagePointCalendarService;
    private final CalendarService calendarService;
    private final ExceptionFactory exceptionFactory;
    private final CalendarInfoFactory calendarInfoFactory;
    private final TransactionService transactionService;

    @Inject
    public UsagePointCalendarResource(CalendarOnUsagePointInfoFactory calendarOnUsagePointInfoFactory, Clock clock, ResourceHelper resourceHelper, UsagePointCalendarService usagePointCalendarService, CalendarService calendarService, ExceptionFactory exceptionFactory, CalendarInfoFactory calendarInfoFactory, TransactionService transactionService) {
        this.calendarOnUsagePointInfoFactory = calendarOnUsagePointInfoFactory;
        this.clock = clock;
        this.resourceHelper = resourceHelper;
        this.usagePointCalendarService = usagePointCalendarService;
        this.calendarService = calendarService;
        this.exceptionFactory = exceptionFactory;
        this.calendarInfoFactory = calendarInfoFactory;
        this.transactionService = transactionService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getCalendarsOnUsagePoint(@PathParam("name") String usagePointName, @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(usagePointName);
        return usagePointCalendarService.calendarsFor(usagePoint)
                .getCalendars()
                .entrySet()
                .stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().getDisplayName()))
                .map(Map.Entry::getValue)
                .map(use(this::createFrom).on(usagePoint))
                .filter(Objects::nonNull)
                .collect(PagedInfoList.toPagedInfoList("calendars", queryParameters));
    }

    @GET
    @Path("/{calendarId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public CalendarInfo getCalendar(@PathParam("name") String name, @PathParam("calendarId") long calendarId, @QueryParam("weekOf") long milliseconds) {
        if (milliseconds <= 0) {
            return calendarService.findCalendar(calendarId)
                    .map(calendarInfoFactory::detailedFromCalendar)
                    .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_CALENDAR));
        } else {
            Instant instant = Instant.ofEpochMilli(milliseconds);
            Calendar calendar = calendarService.findCalendar(calendarId).get();
            LocalDate localDate = LocalDateTime.ofInstant(instant, ZoneId.of("UTC"))
                    .toLocalDate();
            return transformToWeekCalendar(calendar, localDate);
        }
    }

    private CalendarInfo transformToWeekCalendar(Calendar calendar, LocalDate localDate) {
        return calendarInfoFactory.detailedWeekFromCalendar(calendar, localDate);
    }


    @POST
    @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({com.elster.jupiter.calendar.security.Privileges.Constants.MANAGE_TOU_CALENDARS})
    public Response addCalendarToUsagePoint(
            @PathParam("name") String usagePointName,
            @Context UriInfo uriInfo,
            CalendarOnUsagePointInfo calendarOnUsagePointInfo) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByNameOrThrowException(usagePointName);
        Calendar calendar = calendarService.findCalendar(calendarOnUsagePointInfo.calendar.id)
                .orElse(null);
        Instant start = Instant.ofEpochMilli(calendarOnUsagePointInfo.fromTime);
        CalendarOnUsagePoint calendarOnUsagePoint = usagePointCalendarService.calendarsFor(usagePoint)
                    .addCalendar(start, calendar);
        return Response.ok(calendarOnUsagePointInfoFactory.from(calendarOnUsagePoint)).build();
    }

    private CalendarOnUsagePointInfo createFrom(UsagePoint usagePoint, List<CalendarOnUsagePoint> calendars) {
        List<CalendarOnUsagePointInfo> infoList = calendars.stream()
                .filter(calendarOnUsagePoint -> !endsBeforeNow(calendarOnUsagePoint.getRange()))
                .map(calendarOnUsagePointInfoFactory::from)
                .collect(Collectors.toList());
        return link(usagePoint, infoList);
    }

    private CalendarOnUsagePointInfo link(UsagePoint usagePoint, List<CalendarOnUsagePointInfo> infoList) {
        List<CalendarOnUsagePointInfo> padded = withLeadingDummyIfNeeded(usagePoint, infoList);
        CalendarOnUsagePointInfo previous = null;
        for (CalendarOnUsagePointInfo current : padded) {
            if (previous != null) {
                previous.next = current;
            }
            previous = current;
        }
        return padded.isEmpty() ? null : padded.get(0);
    }

    private List<CalendarOnUsagePointInfo> withLeadingDummyIfNeeded(UsagePoint usagePoint, List<CalendarOnUsagePointInfo> infoList) {
        List<CalendarOnUsagePointInfo> padded = infoList;
        if (infoList.isEmpty()) {
            padded = Collections.singletonList(createDummy(usagePoint, null, 0));
        } else if (Instant.ofEpochMilli(infoList.get(0).fromTime).isAfter(clock.instant())) {
            padded = new ArrayList<>();
            padded.add(createDummy(usagePoint, infoList.get(0).calendar, infoList.get(0).fromTime));
            padded.addAll(infoList);
        }
        return padded;
    }

    private CalendarOnUsagePointInfo createDummy(UsagePoint usagePoint, CalendarInfo calendar, long fromTime) {
        CalendarOnUsagePointInfo dummy = new CalendarOnUsagePointInfo();
        dummy.usagePointId = usagePoint.getId();
        dummy.calendar = calendar;
        dummy.fromTime = fromTime;
        return dummy;
    }

    private boolean endsBeforeNow(Range<Instant> range) {
        return range.hasUpperBound() && !(range.upperEndpoint().isAfter(clock.instant()));
    }
}
