/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.test.api.rest.impl;

import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareVersion;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.NoSuchElementException;

@Path("/fwc")
public class FirmwareTestResource {
    private final FirmwareService firmwareService;
    private final OrmService ormService;

    @Inject
    public FirmwareTestResource(FirmwareService firmwareService, OrmService ormService) {
        this.firmwareService = firmwareService;
        this.ormService = ormService;
    }

    @DELETE
    @Transactional
    @Path("/campaigns/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteFirmwareCampaign(@PathParam("id") long firmwareCampaignId) {
        firmwareService.getFirmwareCampaignById(firmwareCampaignId).ifPresent(FirmwareCampaign::delete);
        return Response.noContent().build();
    }

    @DELETE
    @Transactional
    @Path("/versions/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteFirmwareVersion(@PathParam("id") long firmwareVersionId) {
        FirmwareVersion firmwareVersion = firmwareService.getFirmwareVersionById(firmwareVersionId)
                .orElseThrow(() -> new NoSuchElementException("Firmware version not found."));
        ormService.getDataModel(FirmwareService.COMPONENTNAME).get().remove(firmwareVersion);
        return Response.noContent().build();
    }
}
