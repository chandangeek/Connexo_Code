/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable.rest.impl;

import com.elster.jupiter.rest.util.Transactional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

@Path("/timezoneinuses")
public class TimeZoneInUseResource {

    public TimeZoneInUseResource() {
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public TimeZoneInUseInfos getTimeZoneInUsePropertyContext(@Context UriInfo uriInfo){
        return new TimeZoneInUseInfos();    // Todo: add support to jupiter for the old TimeZonInUse entity???
    }

}