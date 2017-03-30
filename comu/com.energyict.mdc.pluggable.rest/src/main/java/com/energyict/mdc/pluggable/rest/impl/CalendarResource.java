/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable.rest.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.device.config.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

@Path("/calendars")
public class CalendarResource {

    private final CalendarService calendarService;

    @Inject
    public CalendarResource(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_MASTER_DATA, Privileges.Constants.VIEW_MASTER_DATA,
            com.energyict.mdc.engine.config.security.Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION,
            com.energyict.mdc.engine.config.security.Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION,
            com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_TYPE,
            com.energyict.mdc.device.config.security.Privileges.Constants.ADMINISTRATE_DEVICE_TYPE})
    public CalendarInfos getCodeTablePropertyContext(@Context UriInfo uriInfo){
        CalendarInfos timeZoneInUseInfos = new CalendarInfos();
        this.calendarService
                .findAllCalendars()
                .stream()
                .map(CalendarInfo::new)
                .forEach(timeZoneInUseInfos.codeTableInfos::add);
        return timeZoneInUseInfos;
    }

}