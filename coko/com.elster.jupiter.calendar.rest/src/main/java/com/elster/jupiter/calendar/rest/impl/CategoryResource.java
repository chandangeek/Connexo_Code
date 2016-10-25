package com.elster.jupiter.calendar.rest.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.Category;
import com.elster.jupiter.calendar.rest.CalendarInfoFactory;
import com.elster.jupiter.calendar.security.Privileges;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/categories")
public class CategoryResource {

    private final ExceptionFactory exceptionFactory;
    private final CalendarInfoFactory calendarInfoFactory;
    private final CalendarService calendarService;

    @Inject
    public CategoryResource(ExceptionFactory exceptionFactory, CalendarInfoFactory calendarInfoFactory, CalendarService calendarService) {
        this.exceptionFactory = exceptionFactory;
        this.calendarInfoFactory = calendarInfoFactory;
        this.calendarService = calendarService;
    }

    @GET
    @RolesAllowed(Privileges.Constants.MANAGE_TOU_CALENDARS)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getAllCalendars(@BeanParam JsonQueryParameters queryParameters) {
        List<Category> allCategories = calendarService.findAllCategories();
        return PagedInfoList.fromCompleteList("categories", allCategories, queryParameters);
    }

    @GET
    @Path("/{id}")
    @RolesAllowed(Privileges.Constants.MANAGE_TOU_CALENDARS)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public CategoryInfo getCalendar(@PathParam("id") long id) {
        return calendarService.findCategory(id)
                .map(CategoryInfo::from)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_CATEGORY));
    }


}
