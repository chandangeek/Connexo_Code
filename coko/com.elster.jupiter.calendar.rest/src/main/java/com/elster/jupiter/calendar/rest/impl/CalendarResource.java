package com.elster.jupiter.calendar.rest.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.rest.CalendarInfo;
import com.elster.jupiter.calendar.rest.CalendarInfoFactory;
import com.elster.jupiter.calendar.security.Privileges;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.Transactional;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
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
import java.util.stream.Collectors;

@Path("/calendars")
public class CalendarResource {
    private final ExceptionFactory exceptionFactory;
    private final CalendarInfoFactory calendarInfoFactory;
    private final CalendarService calendarService;

    @Inject
    public CalendarResource(ExceptionFactory exceptionFactory, CalendarInfoFactory calendarInfoFactory, CalendarService calendarService) {
        this.exceptionFactory = exceptionFactory;
        this.calendarInfoFactory = calendarInfoFactory;
        this.calendarService = calendarService;
    }

    @GET
    @RolesAllowed(Privileges.Constants.MANAGE_TOU_CALENDARS)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public List<CalendarInfo> getAllCalendars(@BeanParam JsonQueryParameters queryParameters) {
       return calendarService.getCalendarFinder()
               .from(queryParameters)
               .stream()
               .map(calendar -> calendarInfoFactory.summaryForOverview(calendar, calendarService.isCalendarInUse(calendar)))
               .collect(Collectors.toList());
    }

    @GET
    @Path("/{id}")
    @RolesAllowed(Privileges.Constants.MANAGE_TOU_CALENDARS)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public CalendarInfo getCalendar(@PathParam("id") long id, @QueryParam("weekOf") long milliseconds) {
        if(milliseconds <= 0) {
            return  calendarService.findCalendar(id)
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

    @DELETE
    @Path("/{id}")
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.MANAGE_TOU_CALENDARS)
    public Response removeCalendar(@PathParam("id") long id) {
        Calendar calendar = calendarService.findCalendar(id).orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_TIME_OF_USE_CALENDAR));
        if(!calendarService.isCalendarInUse(calendar)) {
            calendar.delete();
            return Response.ok().build();
        } else {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.TIME_OF_USE_CALENDAR_IN_USE);
        }
    }


    private CalendarInfo transformToWeekCalendar(Calendar calendar, LocalDate localDate) {
        return calendarInfoFactory.detailedWeekFromCalendar(calendar, localDate);
    }

}
