package com.energyict.mdc.multisense.api.impl;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

/**
 * Created by bvn on 10/6/15.
 */
@Path("/comschedules")
public class ComScheduleResource {
    // TODO

    @Path("{comScheduleId}")
    public Response getComSchedule(@PathParam("comSchedule") long comScheduleId) {
        return Response.ok().build();
    }
}
