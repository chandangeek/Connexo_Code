package com.elster.jupiter.autotests.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.rest.util.Transactional;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/usagepoints")
public class UsagePointTestResource {

    private final MeteringService meteringService;

    @Inject
    public UsagePointTestResource(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @DELETE
    @Path("/{mRID}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    public Response delete(@PathParam("mRID") String mRID, UsagePointInfo usagePointInfo) {
        if (this.meteringService.findUsagePoint(mRID).isPresent()) {
            UsagePoint usagePoint = this.meteringService.findUsagePoint(mRID).get();
            usagePoint.delete();
            return Response.status(Response.Status.OK).build();
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
