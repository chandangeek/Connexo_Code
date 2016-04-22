package com.elster.jupiter.calendar.rest.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.rest.util.ExceptionFactory;

import javax.inject.Inject;
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
import java.time.Year;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
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
    @Path("/timeofusecalendars")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public List<CalendarInfo> getAllTimeOfUseCalendars() {
       return calendarService.findAllCalendars()
               .stream()
               .map(calendarInfoFactory::fromCalendar)
               .collect(Collectors.toList());
    }

    @GET
    @Path("/timeofusecalendars/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public CalendarInfo getTimeOfUseCalendar(@PathParam("id") long id, @QueryParam("weekOf") long milliseconds) {
        if(milliseconds <= 0) {
            return  calendarService.findCalendar(id)
                    .map(calendarInfoFactory::fromCalendar)
                    .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_TIME_OF_USE_CALENDAR));
        } else {
            Instant instant = Instant.ofEpochMilli(milliseconds);
            Calendar calendar = calendarService.findCalendar(id).get();
            LocalDate localDate = LocalDateTime.ofInstant(instant, ZoneId.of("UTC"))
                    .toLocalDate();
            return transformToWeekCalendar(calendar, localDate);
        }
    }

    private CalendarInfo transformToWeekCalendar(Calendar calendar, LocalDate localDate) {
        calendarService.newCalendar(calendar.getName(), calendar.getTimeZone(), Year.of(localDate.getYear()));
        return calendarInfoFactory.fromCalendar(calendar, localDate);
    }

}
