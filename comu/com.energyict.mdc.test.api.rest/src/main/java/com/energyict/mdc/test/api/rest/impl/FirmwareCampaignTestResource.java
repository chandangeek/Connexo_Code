/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.test.api.rest.impl;

import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareService;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/campaigns")
public class FirmwareCampaignTestResource {
    private final FirmwareService firmwareService;

    @Inject
    public FirmwareCampaignTestResource(FirmwareService firmwareService) {
        this.firmwareService = firmwareService;
    }

    @DELETE
    @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteFirmwareCampaign(@PathParam("id") long firmwareCampaignId) {
        firmwareService.getFirmwareCampaignById(firmwareCampaignId).ifPresent(FirmwareCampaign::delete);
        return Response.noContent().build();
    }
}
