/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.test.api.rest.impl;

import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignService;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/toucampaigns")
public class TimeOfUseCampaignTestResource {
    private final TimeOfUseCampaignService timeOfUseCampaignService;

    @Inject
    public TimeOfUseCampaignTestResource(TimeOfUseCampaignService timeOfUseCampaignService) {
        this.timeOfUseCampaignService = timeOfUseCampaignService;
    }

    @DELETE
    @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteCampaign(@PathParam("id") long id) {
        timeOfUseCampaignService.getCampaign(id).get().delete();
        return Response.noContent().build();
    }
}
