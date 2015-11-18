package com.energyict.mdc.pluggable.rest.impl;

import com.energyict.mdc.common.rest.Transactional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

/**
 * Copyrights EnergyICT
 * Date: 20/11/13
 * Time: 15:20
 */
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