package com.elster.jupiter.webservices.inbound.rest.impl;

import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.Optional;

@Path("/devices")
public class DeviceResource {

    private final MeteringService meteringService;

    @Inject
    public DeviceResource(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @GET
    @Path("/{mRID}/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Object getDevice(@PathParam("mRID") String mRID, @Context SecurityContext securityContext) {
        Optional<Meter> foundMeter = meteringService.findMeter(mRID);
        if (foundMeter.isPresent()) {
            return new MeterInfo(foundMeter.get());
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }


}