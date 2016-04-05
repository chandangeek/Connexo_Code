package com.elster.jupiter.calendar.rest.impl;

import com.elster.jupiter.rest.util.ExceptionFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Path("/calendars")
public class CalendarResource {
    private final ExceptionFactory exceptionFactory;
    private final CalendarInfoFactory calendarInfoFactory;

    @Inject
    public CalendarResource(ExceptionFactory exceptionFactory, CalendarInfoFactory calendarInfoFactory) {
        this.exceptionFactory = exceptionFactory;
        this.calendarInfoFactory = calendarInfoFactory;
    }

    @GET
    @Path(("/timeofusecalendars"))
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public List<CalendarInfo> getAllTimeOfUseCalendars() {
        List<CalendarInfo> infos = new ArrayList<CalendarInfo>();

        for (int i = 0; i < randomWithRange(5, 15); i++) {
            infos.add(calendarInfoFactory.fromCalendar());
        }

        return infos;
    }

    private int randomWithRange(int min, int max) {
        int range = (max - min) + 1;
        return (int) (Math.random() * range) + min;
    }
}
