/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign.rest.impl;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.servicecall.security.Privileges;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaign;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/touCampaigns")
public class TimeOfUseCampaignResource {

    private final TimeOfUseCampaignService timeOfUseCampaignService;
    private final TimeOfUseCampaignInfoFactory timeOfUseCampaignInfoFactory;


    @Inject
    public TimeOfUseCampaignResource(TimeOfUseCampaignService timeOfUseCampaignService, TimeOfUseCampaignInfoFactory timeOfUseCampaignInfoFactory) {
        this.timeOfUseCampaignService = timeOfUseCampaignService;
        this.timeOfUseCampaignInfoFactory = timeOfUseCampaignInfoFactory;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.VIEW_SERVICE_CALLS})
    public Response getToUCampaigns(@BeanParam JsonQueryParameters queryParameters) {
        List<TimeOfUseCampaignInfo> touCampaigns = new ArrayList<>();
        timeOfUseCampaignService.getAllCampaigns().forEach(campaign -> touCampaigns.add(timeOfUseCampaignInfoFactory.from(campaign)));
        return Response.ok(PagedInfoList.fromPagedList("touCampaigns", touCampaigns, queryParameters)).build();
    }

    @GET
    @Transactional
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.VIEW_SERVICE_CALLS})
    public Response getToUCampaign(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters) {
        TimeOfUseCampaign timeOfUseCampaign = timeOfUseCampaignService.getCampaign(name)
                .orElseThrow(() -> new IllegalStateException("No time of use campaign is found."));//todo ???
        TimeOfUseCampaignInfo timeOfUseCampaignInfo = timeOfUseCampaignInfoFactory.from(timeOfUseCampaign);
        return Response.ok(timeOfUseCampaignInfo).build();
    }

    @POST
    @Transactional
    @Path("/create")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.VIEW_SERVICE_CALLS})
    public Response createToUCampaign(TimeOfUseCampaignInfo timeOfUseCampaignInfo) {
        timeOfUseCampaignService.createToUCampaign(timeOfUseCampaignInfoFactory.build(timeOfUseCampaignInfo));
        return Response.ok(timeOfUseCampaignInfo).build();
    }

    @PUT
    @Transactional
    @Path("/retry")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.VIEW_SERVICE_CALLS})
    public Response retry(String deviceName) {
        timeOfUseCampaignService.retry(deviceName);
        return Response.ok().build();
    }

    @PUT
    @Transactional
    @Path("/{name}/edit")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.VIEW_SERVICE_CALLS})
    public Response edit(@PathParam("name") String name, TimeOfUseCampaignInfo timeOfUseCampaignInfo) {
        timeOfUseCampaignService.edit(name, timeOfUseCampaignInfoFactory.build(timeOfUseCampaignInfo));
        return Response.ok(timeOfUseCampaignInfo).build();
    }

    @PUT
    @Transactional
    @Path("/{name}/cancel")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.VIEW_SERVICE_CALLS})
    public Response cancel(@PathParam("name") String name) {
        timeOfUseCampaignService.cancelCampaign(name);
        return Response.ok().build();
    }

    @GET
    @Transactional
    @Path("/devicetypes")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.VIEW_SERVICE_CALLS})
    public Response getDeviceTypesForCalendars(@BeanParam JsonQueryParameters queryParameters) {
        List<DeviceType> deviceTypes = timeOfUseCampaignService.getDeviceTypesWithCalendars();
        return Response.ok(deviceTypes).build();
    }

}
