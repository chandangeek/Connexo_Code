package com.energyict.mdc.multisense.api.redknee;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by bvn on 9/17/15.
 */
@Path("usagepoints/{mrid}/contactor")
public class RknProxyResource {

    private final ConsumptionExportGenerator generator;

    @Inject
    public RknProxyResource(ConsumptionExportGenerator generator) {

        this.generator = generator;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getName() {
        return Response.ok().entity("{\"status\":\"ok\"}").build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateContactor(@PathParam("mrid") String mRID, ContactorInfo contactorInfo) {
        UsagePoint usagePoint = generator.getUsagePoint(mRID).orElseThrow(()->new WebApplicationException("No such usagepoint", Response.Status.NOT_FOUND));
        switch (contactorInfo.status) {
            case connected:
            case armed:
                usagePoint.connect();
                break;
            case disconnected:
                usagePoint.disconnect();
                break;
        }
        return Response.ok().entity("{\"status\":\"ok\"}").build();

    }

}
