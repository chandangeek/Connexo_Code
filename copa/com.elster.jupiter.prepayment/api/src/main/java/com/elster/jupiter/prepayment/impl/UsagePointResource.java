package com.elster.jupiter.prepayment.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Created by bvn on 9/16/15.
 */
@Path("usagepoints/{mrid}/contactor")
public class UsagePointResource {

    private final MeteringService meteringService;

    @Inject
    public UsagePointResource(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateContactor(@PathParam("mrid") String mRID, ContactorInfo contactorInfo) {
//        UsagePoint usagePoint = meteringService.findUsagePoint(mRID).orElseThrow(() -> new WebApplicationException("No such usagepoint", Response.Status.NOT_FOUND));
        return Response.status(Response.Status.ACCEPTED).build();
    }

}
