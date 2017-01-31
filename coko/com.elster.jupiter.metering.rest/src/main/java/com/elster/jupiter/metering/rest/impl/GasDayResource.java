/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.GasDayOptions;
import com.elster.jupiter.metering.MeteringService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/gasday")
public class GasDayResource {

    private final MeteringService meteringService;

    @Inject
    public GasDayResource(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @GET
    @Path("/yearstart")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public GasDayYearStartInfo getYearStart() {
        return this.meteringService
                .getGasDayOptions()
                .map(GasDayOptions::getYearStart)
                .map(GasDayYearStartInfo::new)
                .orElseGet(GasDayYearStartInfo::new);
    }

}