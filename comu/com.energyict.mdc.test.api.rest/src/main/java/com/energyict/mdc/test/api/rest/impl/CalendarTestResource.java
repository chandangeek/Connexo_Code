/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.test.api.rest.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.rest.util.Transactional;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/calendars")
public class CalendarTestResource {
    private final CalendarService calendarService;

    @Inject
    public CalendarTestResource(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response deleteCalendar(@PathParam("id") long id) {
        Calendar calendar = calendarService.findCalendar(id).orElseThrow(() -> new IllegalStateException("Calendar not found"));
        if (!calendar.mayBeDeleted()) {
            throw new IllegalArgumentException("ACTIVE_CALENDAR_CANT_BE_REMOVED");
        } else if (calendarService.isCalendarInUse(calendar)) {
            throw new IllegalArgumentException("TIME_OF_USE_CALENDAR_IN_USE");
        } else {
            calendar.delete();
            return Response.noContent().build();
        }
    }
}
