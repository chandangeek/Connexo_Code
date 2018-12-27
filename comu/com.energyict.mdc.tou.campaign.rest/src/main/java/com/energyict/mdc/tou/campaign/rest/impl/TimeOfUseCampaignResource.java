/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.security.Privileges;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaign;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignService;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignException;

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
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/touCampaigns")
public class TimeOfUseCampaignResource {

    private final TimeOfUseCampaignService timeOfUseCampaignService;
    private final TimeOfUseCampaignInfoFactory timeOfUseCampaignInfoFactory;
    private final Thesaurus thesaurus;


    @Inject
    public TimeOfUseCampaignResource(TimeOfUseCampaignService timeOfUseCampaignService, TimeOfUseCampaignInfoFactory timeOfUseCampaignInfoFactory,
                                     Thesaurus thesaurus) {
        this.timeOfUseCampaignService = timeOfUseCampaignService;
        this.timeOfUseCampaignInfoFactory = timeOfUseCampaignInfoFactory;
        this.thesaurus = thesaurus;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.VIEW_SERVICE_CALLS})
    public Response getToUCampaigns(@BeanParam JsonQueryParameters queryParameters) {
        List<TimeOfUseCampaignInfo> touCampaigns = new ArrayList<>();
        timeOfUseCampaignService.getAllCampaigns().forEach((campaign, satus) -> {
            TimeOfUseCampaignInfo info = timeOfUseCampaignInfoFactory.from(campaign);
            info.status = satus.getDefaultFormat();
            info.devices = new ArrayList<>();
            info.devices.add(new DevicesStatusAndQuantity(DefaultState.SUCCESSFUL.toString(), 0L));
            info.devices.add(new DevicesStatusAndQuantity(DefaultState.FAILED.toString(), 0L));
            info.devices.add(new DevicesStatusAndQuantity(DefaultState.REJECTED.toString(), 0L));
            info.devices.add(new DevicesStatusAndQuantity(DefaultState.ONGOING.toString(), 0L));
            info.devices.add(new DevicesStatusAndQuantity(DefaultState.PENDING.toString(), 0L));
            info.devices.add(new DevicesStatusAndQuantity(DefaultState.CANCELLED.toString(), 0L));
            timeOfUseCampaignService.getChildrenStatusFromCampaign(campaign.getId()).forEach((deviceStatus, quantity) -> {
                info.devices.stream().filter(devicesStatusAndQuantity -> devicesStatusAndQuantity.status.equals(deviceStatus.name()))
                        .findAny().ifPresent(devicesStatusAndQuantity -> {
                    devicesStatusAndQuantity.status = devicesStatusAndQuantity.getStatus(deviceStatus, thesaurus);
                    devicesStatusAndQuantity.quantity = quantity;
                });
            });
            touCampaigns.add(info);
        });
        return Response.ok(PagedInfoList.fromPagedList("touCampaigns", touCampaigns, queryParameters)).build();
    }

    @GET
    @Transactional
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.VIEW_SERVICE_CALLS})
    public Response getToUCampaign(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters) {
        TimeOfUseCampaign timeOfUseCampaign = timeOfUseCampaignService.getCampaign(name)
                .orElseThrow(() -> new TimeOfUseCampaignException(thesaurus, MessageSeeds.NO_TIME_OF_USE_CAMPAIGN_IS_FOUND));
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
    public Response retry(long id) {
        timeOfUseCampaignService.retry(id);
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
        List<IdWithNameInfo> deviceTypes = new ArrayList<>();
        timeOfUseCampaignService.getDeviceTypesWithCalendars()
                .forEach(deviceType -> deviceTypes.add(new IdWithNameInfo(deviceType.getId(), deviceType.getName())));
        return Response.ok(deviceTypes).build();
    }

}
