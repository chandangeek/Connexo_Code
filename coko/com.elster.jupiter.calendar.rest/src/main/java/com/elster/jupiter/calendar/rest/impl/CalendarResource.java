package com.elster.jupiter.calendar.rest.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarFilter;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.calendar.Status;
import com.elster.jupiter.calendar.rest.CalendarInfo;
import com.elster.jupiter.calendar.rest.CalendarInfoFactory;
import com.elster.jupiter.calendar.security.Privileges;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.transaction.TransactionService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Path("/calendars")
public class CalendarResource {
    private final ExceptionFactory exceptionFactory;
    private final CalendarInfoFactory calendarInfoFactory;
    private final CalendarService calendarService;
    private final ConcurrentModificationExceptionFactory concurrentModificationExceptionFactory;
    private final TransactionService transactionService;

    @Inject
    public CalendarResource(ExceptionFactory exceptionFactory, CalendarInfoFactory calendarInfoFactory, CalendarService calendarService, ConcurrentModificationExceptionFactory concurrentModificationExceptionFactory, TransactionService transactionService) {
        this.exceptionFactory = exceptionFactory;
        this.calendarInfoFactory = calendarInfoFactory;
        this.calendarService = calendarService;
        this.concurrentModificationExceptionFactory = concurrentModificationExceptionFactory;
        this.transactionService = transactionService;
    }

    @GET
    @RolesAllowed(Privileges.Constants.MANAGE_TOU_CALENDARS)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getAllCalendars(@BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter filter) {
        CalendarFilter calendarFilter = calendarService.newCalendarFilter();
        if (filter.hasProperty("status")) {
            Status status = Status.valueOf(filter.getString("status"));
            calendarFilter.setStatus(status);
        }
        if (filter.hasProperty("category")) {
            Category category = calendarService.findCategoryByName(filter.getString("category"))
                    .orElseThrow(IllegalArgumentException::new);
            calendarFilter.setCategory(category);
        }
        List<CalendarInfo> list = calendarService.getCalendarFinder(calendarFilter.toCondition())
                .from(queryParameters)
                .stream()
                .map(calendar -> calendarInfoFactory.summaryForOverview(calendar, calendarService.isCalendarInUse(calendar)))
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("calendars", list, queryParameters);
    }

    @GET
    @Path("/{id}")
    @RolesAllowed(Privileges.Constants.MANAGE_TOU_CALENDARS)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public CalendarInfo getCalendar(@PathParam("id") long id, @QueryParam("weekOf") long milliseconds) {
        if (milliseconds <= 0) {
            return calendarService.findCalendar(id)
                    .map(calendarInfoFactory::detailedFromCalendar)
                    .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_TIME_OF_USE_CALENDAR));
        } else {
            Instant instant = Instant.ofEpochMilli(milliseconds);
            Calendar calendar = calendarService.findCalendar(id).get();
            LocalDate localDate = LocalDateTime.ofInstant(instant, ZoneId.of("UTC"))
                    .toLocalDate();
            return transformToWeekCalendar(calendar, localDate);
        }
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed(Privileges.Constants.MANAGE_TOU_CALENDARS)
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response updateCalendar(@PathParam("id") long id, CalendarInfo info) {
        if (info.status == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        transactionService.run(() -> doUpdateCalendar(id, info));
        return Response.ok().build();
    }

    private void doUpdateCalendar(@PathParam("id") long id, CalendarInfo info) {
        Calendar calendar = lockCalendar(id, info);
        switch (calendar.getStatus()) {
            case ACTIVE:
                if (Status.INACTIVE.equals(info.status.id)) {
                    if (!calendarService.isCalendarInUse(calendar)) {
                        calendar.deactivate();
                    } else {
                        throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.TIME_OF_USE_CALENDAR_IN_USE);
                    }
                }
                break;
            case INACTIVE:
                if (Status.ACTIVE.equals(info.status.id)) {
                    calendar.activate();
                }
                break;
            default:
        }
    }

    private Calendar lockCalendar(long id, CalendarInfo info) {
        Optional<Calendar> calendar = calendarService.findCalendar(id);
        return calendarService.lockCalendar(id, info.version)
                .orElseThrow(() -> concurrentModificationExceptionFactory
                        .contextDependentConflictOn(calendar.map(Calendar::getName).orElse(""))
                        .withActualVersion(() -> calendar.map(Calendar::getVersion).orElse(-1L))
                        .build());
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.MANAGE_TOU_CALENDARS)
    public Response removeCalendar(@PathParam("id") long id) {
        Calendar calendar = calendarService.findCalendar(id)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_TIME_OF_USE_CALENDAR));
        if (!calendar.mayBeDeleted()) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.ACTIVE_CALENDAR_CANT_BE_REMOVED);
        } else if (calendarService.isCalendarInUse(calendar)) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.TIME_OF_USE_CALENDAR_IN_USE);
        } else {
            calendar.makeObsolete();
            return Response.ok().build();
        }
    }

    private CalendarInfo transformToWeekCalendar(Calendar calendar, LocalDate localDate) {
        return calendarInfoFactory.detailedWeekFromCalendar(calendar, localDate);
    }
}
