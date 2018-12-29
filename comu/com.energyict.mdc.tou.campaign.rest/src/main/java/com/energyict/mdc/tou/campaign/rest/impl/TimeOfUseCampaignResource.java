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
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.security.Privileges;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaign;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignException;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignService;
import com.energyict.mdc.tou.campaign.impl.servicecall.TimeOfUseItemDomainExtension;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
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
        timeOfUseCampaignService.getAllCampaigns().forEach((campaign, status) -> {
            touCampaigns.add(getOverviewCampaignInfo(campaign, status));
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
        TimeOfUseCampaignInfo timeOfUseCampaignInfo = getOverviewCampaignInfo(timeOfUseCampaign, timeOfUseCampaignService.findCampaignServiceCall(timeOfUseCampaign.getName()).get().getState());

        return Response.ok(timeOfUseCampaignInfo).build();
    }

    @GET
    @Transactional
    @Path("/{name}/devices")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.VIEW_SERVICE_CALLS})
    public Response getToUCampaignsDevices(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters) {
        TimeOfUseCampaign timeOfUseCampaign = timeOfUseCampaignService.getCampaign(name)
                .orElseThrow(() -> new TimeOfUseCampaignException(thesaurus, MessageSeeds.NO_TIME_OF_USE_CAMPAIGN_IS_FOUND));
        ServiceCall parent = timeOfUseCampaignService.findCampaignServiceCall(timeOfUseCampaign.getName()).get();
        List<DeviceInCampaignInfo> deviceInCampaignInfo = new ArrayList<>();
        parent.findChildren().find().forEach(serviceCall -> deviceInCampaignInfo
                .add(new DeviceInCampaignInfo(serviceCall.getExtension(TimeOfUseItemDomainExtension.class).get().getDevice().getmRID(),
                        getStatus(serviceCall.getState(), thesaurus),
                        serviceCall.getCreationTime(),
                        (serviceCall.getState().equals(DefaultState.CANCELLED)
                                || serviceCall.getState().equals(DefaultState.SUCCESSFUL)) ? serviceCall.getLastModificationTime() : null)));
        return Response.ok(deviceInCampaignInfo).build();
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

    @GET
    @Transactional
    @Path("/getoptions")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.VIEW_SERVICE_CALLS})
    public Response getSendOptionsForType(@QueryParam("type") Long deviceTypeId) {
        DeviceTypeAndOptionsInfo deviceTypeAndOptionsInfo = new DeviceTypeAndOptionsInfo();
        DeviceType deviceType = timeOfUseCampaignService.getDeviceTypesWithCalendars().stream()
                .filter(deviceType1 -> deviceType1.getId() == deviceTypeId).findAny().get();
        deviceTypeAndOptionsInfo.deviceType = new IdWithNameInfo(deviceType.getId(), deviceType.getName());
        deviceTypeAndOptionsInfo.calendars = new ArrayList<>();
        deviceTypeAndOptionsInfo.fullCalendar = false;
        deviceTypeAndOptionsInfo.withActivationDate = false;
        deviceTypeAndOptionsInfo.specialDays = false;
        deviceType.getAllowedCalendars().forEach(allowedCalendar -> deviceTypeAndOptionsInfo.calendars.add(new IdWithNameInfo(allowedCalendar.getCalendar().get().getId(), allowedCalendar.getName())));
        timeOfUseCampaignService.getDeviceConfigurationService().findTimeOfUseOptions(deviceType).get().getOptions()
                .forEach(protocolSupportedCalendarOptions -> {
                    if (protocolSupportedCalendarOptions.getId().equals("send")) {
                        deviceTypeAndOptionsInfo.fullCalendar = true;
                    } else if (protocolSupportedCalendarOptions.getId().equals("sendWithDateTime")) {
                        deviceTypeAndOptionsInfo.fullCalendar = true;
                        deviceTypeAndOptionsInfo.withActivationDate = true;
                    } else if (protocolSupportedCalendarOptions.getId().equals("sendSpecialDays")) {
                        deviceTypeAndOptionsInfo.specialDays = true;
                    }
                });
        return Response.ok(deviceTypeAndOptionsInfo).build();
    }

    private TimeOfUseCampaignInfo getOverviewCampaignInfo(TimeOfUseCampaign campaign, DefaultState status) {
        TimeOfUseCampaignInfo info = timeOfUseCampaignInfoFactory.from(campaign);
        ServiceCall campaignsServiceCall = timeOfUseCampaignService.findCampaignServiceCall(campaign.getName()).get();
        info.startedOn = campaignsServiceCall.getCreationTime();
        info.finishedOn = (campaignsServiceCall.getState().equals(DefaultState.CANCELLED)
                || campaignsServiceCall.getState().equals(DefaultState.SUCCESSFUL)) ? campaignsServiceCall.getLastModificationTime() : null;
        info.status = status.getDefaultFormat();
        info.devices = new ArrayList<>();
        info.devices.add(new DevicesStatusAndQuantity(getStatus(DefaultState.SUCCESSFUL, thesaurus), 0L));
        info.devices.add(new DevicesStatusAndQuantity(getStatus(DefaultState.FAILED, thesaurus), 0L));
        info.devices.add(new DevicesStatusAndQuantity(getStatus(DefaultState.REJECTED, thesaurus), 0L));
        info.devices.add(new DevicesStatusAndQuantity(getStatus(DefaultState.ONGOING, thesaurus), 0L));
        info.devices.add(new DevicesStatusAndQuantity(getStatus(DefaultState.PENDING, thesaurus), 0L));
        info.devices.add(new DevicesStatusAndQuantity(getStatus(DefaultState.CANCELLED, thesaurus), 0L));
        timeOfUseCampaignService.getChildrenStatusFromCampaign(campaign.getId()).forEach((deviceStatus, quantity) ->
                info.devices.stream().filter(devicesStatusAndQuantity -> devicesStatusAndQuantity.status.equals(getStatus(deviceStatus, thesaurus)))
                        .findAny().ifPresent(devicesStatusAndQuantity -> devicesStatusAndQuantity.quantity = quantity));
        return info;
    }


    private String getStatus(DefaultState defaultState, Thesaurus thesaurus) {
        switch (defaultState) {
            case SUCCESSFUL:
                return thesaurus.getString(MessageSeeds.STATUS_SUCCESSFUL.getKey(), MessageSeeds.STATUS_SUCCESSFUL.getDefaultFormat());
            case FAILED:
                return thesaurus.getString(MessageSeeds.STATUS_FAILED.getKey(), MessageSeeds.STATUS_FAILED.getDefaultFormat());
            case REJECTED:
                return thesaurus.getString(MessageSeeds.STATUS_CONFIGURATION_ERROR.getKey(), MessageSeeds.STATUS_CONFIGURATION_ERROR.getDefaultFormat());
            case ONGOING:
                return thesaurus.getString(MessageSeeds.STATUS_ONGOING.getKey(), MessageSeeds.STATUS_ONGOING.getDefaultFormat());
            case PENDING:
                return thesaurus.getString(MessageSeeds.STATUS_PENDING.getKey(), MessageSeeds.STATUS_PENDING.getDefaultFormat());
            case CANCELLED:
                return thesaurus.getString(MessageSeeds.STATUS_CANCELED.getKey(), MessageSeeds.STATUS_CANCELED.getDefaultFormat());
        }
        return defaultState.getDisplayName(thesaurus);
    }
}
