/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservices.inbound.rest.impl;

import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

@Path("/devices")
public class DeviceResource {

    private final MeteringService meteringService;

    @Inject
    public DeviceResource(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getDevices() {
        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity("{\"error\" : \"Not possible to retrieve all devices in the system\"}")
                .build();
    }

    @GET
    @Path("/{name}/")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getDevice(@PathParam("name") String name) {
        Optional<Meter> foundMeter = meteringService.findMeterByName(name);
        if (foundMeter.isPresent()) {
            return Response.ok(new MeterInfo(foundMeter.get()), MediaType.APPLICATION_JSON_TYPE).build();
        }
        return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\" : \"No device exists with name " + name + "\"}")
                .build();
    }
}