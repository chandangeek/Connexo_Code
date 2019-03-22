/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign.rest.impl;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Where;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaign;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignException;
import com.energyict.mdc.tou.campaign.TimeOfUseCampaignService;
import com.energyict.mdc.tou.campaign.TimeOfUseItem;
import com.energyict.mdc.tou.campaign.security.Privileges;

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
import java.util.stream.Collectors;

@Path("/toucampaigns")
public class TimeOfUseCampaignResource {

    private final TimeOfUseCampaignService timeOfUseCampaignService;
    private final TimeOfUseCampaignInfoFactory timeOfUseCampaignInfoFactory;
    private final DeviceInCampaignInfoFactory deviceInCampaignInfoFactory;
    private final DeviceTypeAndOptionsInfoFactory deviceTypeAndOptionsInfoFactory;
    private final Thesaurus thesaurus;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final DeviceService deviceService;
    private final ServiceCallService serviceCallService;
    private final ExceptionFactory exceptionFactory;
    private final DeviceConfigurationService deviceConfigurationService;

    @Inject
    public TimeOfUseCampaignResource(TimeOfUseCampaignService timeOfUseCampaignService, TimeOfUseCampaignInfoFactory timeOfUseCampaignInfoFactory,
                                     Thesaurus thesaurus, ConcurrentModificationExceptionFactory conflictFactory,
                                     DeviceTypeAndOptionsInfoFactory deviceTypeAndOptionsInfoFactory, DeviceInCampaignInfoFactory deviceInCampaignInfoFactory,
                                     DeviceService deviceService, ServiceCallService serviceCallService, ExceptionFactory exceptionFactory,
                                     DeviceConfigurationService deviceConfigurationService) {
        this.timeOfUseCampaignService = timeOfUseCampaignService;
        this.timeOfUseCampaignInfoFactory = timeOfUseCampaignInfoFactory;
        this.thesaurus = thesaurus;
        this.conflictFactory = conflictFactory;
        this.deviceTypeAndOptionsInfoFactory = deviceTypeAndOptionsInfoFactory;
        this.deviceInCampaignInfoFactory = deviceInCampaignInfoFactory;
        this.deviceService = deviceService;
        this.serviceCallService = serviceCallService;
        this.exceptionFactory = exceptionFactory;
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_TOU_CAMPAIGNS, Privileges.Constants.ADMINISTER_TOU_CAMPAIGNS})
    public Response getToUCampaigns(@BeanParam JsonQueryParameters queryParameters) {
        QueryStream<? extends TimeOfUseCampaign> campaigns = timeOfUseCampaignService.streamAllCampaigns().join(ServiceCall.class)
                .sorted(Order.descending("serviceCall.createTime"));
        queryParameters.getStart().ifPresent(campaigns::skip);
        queryParameters.getLimit().ifPresent(limit -> campaigns.limit(limit + 1));
        List<TimeOfUseCampaignInfo> touCampaigns = campaigns
                .map(timeOfUseCampaignInfoFactory::getOverviewCampaignInfo).collect(Collectors.toList());
        return Response.ok(PagedInfoList.fromPagedList("touCampaigns", touCampaigns, queryParameters)).build();
    }

    @GET
    @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_TOU_CAMPAIGNS, Privileges.Constants.ADMINISTER_TOU_CAMPAIGNS})
    public Response getToUCampaign(@PathParam("id") long id, @BeanParam JsonQueryParameters queryParameters) {
        TimeOfUseCampaign timeOfUseCampaign = timeOfUseCampaignService.getCampaign(id)
                .orElseThrow(() -> new TimeOfUseCampaignException(thesaurus, MessageSeeds.NO_TIME_OF_USE_CAMPAIGN_IS_FOUND));
        TimeOfUseCampaignInfo timeOfUseCampaignInfo = timeOfUseCampaignInfoFactory.getOverviewCampaignInfo(timeOfUseCampaign);
        return Response.ok(timeOfUseCampaignInfo).build();
    }

    @GET
    @Transactional
    @Path("/{id}/devices")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_TOU_CAMPAIGNS, Privileges.Constants.ADMINISTER_TOU_CAMPAIGNS})
    public Response getToUCampaignDevices(@PathParam("id") long id, @BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter filter) {
        List<String> states = filter.getStringList("status").stream().map(DefaultState::valueOf).map(DefaultState::getKey).collect(Collectors.toList());
        QueryStream<? extends TimeOfUseItem> devices = timeOfUseCampaignService.streamDevicesInCampaigns().join(ServiceCall.class).join(ServiceCall.class).join(State.class)
                .sorted(Order.ascending("device")).filter(Where.where("serviceCall.parent.id").isEqualTo(id));
        if (states.size() > 0) {
            devices.filter(Where.where("serviceCall.state.name").in(states));
        }
        queryParameters.getStart().ifPresent(devices::skip);
        queryParameters.getLimit().ifPresent(limit -> devices.limit(limit + 1));
        List<DeviceInCampaignInfo> deviceInCampaignInfo = devices.map(o -> deviceInCampaignInfoFactory.create(o.getDevice(), o.getServiceCall())).collect(Collectors.toList());
        return Response.ok(PagedInfoList.fromPagedList("devicesInCampaign", deviceInCampaignInfo, queryParameters)).build();
    }

    @POST
    @Transactional
    @Path("/create")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_TOU_CAMPAIGNS})
    public Response createToUCampaign(TimeOfUseCampaignInfo timeOfUseCampaignInfo) {
        TimeOfUseCampaign timeOfUseCampaign = timeOfUseCampaignInfoFactory.build(timeOfUseCampaignInfo);
        return Response.ok(timeOfUseCampaignInfoFactory.from(timeOfUseCampaign)).build();
    }

    @PUT
    @Transactional
    @Path("/retryDevice")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_TOU_CAMPAIGNS})
    public Response retryDevice(IdWithNameInfo id) {
        Device device = deviceService.findDeviceById(((Number) id.id).longValue())
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.DEVICE_WITH_ID_ISNT_FOUND, id));
        TimeOfUseItem timeOfUseItem = timeOfUseCampaignService.findActiveTimeOfUseItemByDevice(device)
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.TOU_ITEM_WITH_DEVICE_ISNT_FOUND, device));
        ServiceCall serviceCall = timeOfUseItem.getServiceCall();
        serviceCallService.lockServiceCall(serviceCall.getId());
        DeviceInCampaignInfo deviceInCampaignInfo = deviceInCampaignInfoFactory.create(device, timeOfUseItem.retry());
        return Response.ok(deviceInCampaignInfo).build();
    }

    @PUT
    @Transactional
    @Path("/cancelDevice")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_TOU_CAMPAIGNS})
    public Response cancelDevice(IdWithNameInfo id) {
        Device device = deviceService.findDeviceById(((Number) id.id).longValue())
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.DEVICETYPE_WITH_ID_ISNT_FOUND, id));
        TimeOfUseItem timeOfUseItem = timeOfUseCampaignService.findActiveTimeOfUseItemByDevice(device)
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.TOU_ITEM_WITH_DEVICE_ISNT_FOUND, device));
        ServiceCall serviceCall = timeOfUseItem.getServiceCall();
        serviceCallService.lockServiceCall(serviceCall.getId());
        DeviceInCampaignInfo deviceInCampaignInfo = deviceInCampaignInfoFactory.create(device, timeOfUseItem.cancel());
        return Response.ok(deviceInCampaignInfo).build();
    }

    @PUT
    @Transactional
    @Path("/{id}/edit")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_TOU_CAMPAIGNS})
    public Response edit(@PathParam("id") long id, TimeOfUseCampaignInfo timeOfUseCampaignInfo) {
        TimeOfUseCampaign timeOfUseCampaign = timeOfUseCampaignService.findAndLockToUCampaignByIdAndVersion(timeOfUseCampaignInfo.id, timeOfUseCampaignInfo.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(timeOfUseCampaignInfo.name)
                        .withActualVersion(() -> getCurrentCampaignVersion(timeOfUseCampaignInfo.id))
                        .supplier());
        timeOfUseCampaign.setName(timeOfUseCampaignInfo.name);
        timeOfUseCampaign.setUploadPeriodStart(timeOfUseCampaignInfo.activationStart);
        timeOfUseCampaign.setUploadPeriodEnd(timeOfUseCampaignInfo.activationEnd);
        timeOfUseCampaign.update();
        return Response.ok(timeOfUseCampaignInfoFactory.from(timeOfUseCampaign)).build();
    }

    @PUT
    @Transactional
    @Path("/{id}/cancel")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_TOU_CAMPAIGNS})
    public Response cancel(@PathParam("id") long id, TimeOfUseCampaignInfo timeOfUseCampaignInfo) {
        TimeOfUseCampaign timeOfUseCampaign = timeOfUseCampaignService.findAndLockToUCampaignByIdAndVersion(timeOfUseCampaignInfo.id, timeOfUseCampaignInfo.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(timeOfUseCampaignInfo.name)
                        .withActualVersion(() -> getCurrentCampaignVersion(timeOfUseCampaignInfo.id))
                        .supplier());
        timeOfUseCampaign.cancel();
        return Response.ok().build();
    }

    @GET
    @Transactional
    @Path("/devicetypes")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_TOU_CAMPAIGNS, Privileges.Constants.ADMINISTER_TOU_CAMPAIGNS})
    public Response getDeviceTypesForCalendars(@BeanParam JsonQueryParameters queryParameters) {
        List<IdWithNameInfo> deviceTypes = new ArrayList<>();
        timeOfUseCampaignService.getDeviceTypesWithCalendars()
                .forEach(deviceType -> deviceTypes.add(new IdWithNameInfo(deviceType.getId(), deviceType.getName())));
        return Response.ok(deviceTypes).build();
    }

    @GET
    @Transactional
    @Path("/getoptions")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_TOU_CAMPAIGNS, Privileges.Constants.ADMINISTER_TOU_CAMPAIGNS})
    public Response getSendOptionsForType(@QueryParam("type") long deviceTypeId) {
        DeviceType deviceType = deviceConfigurationService.findDeviceType(deviceTypeId)
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.DEVICETYPE_WITH_ID_ISNT_FOUND, deviceTypeId));
        DeviceTypeAndOptionsInfo deviceTypeAndOptionsInfo = deviceTypeAndOptionsInfoFactory.create(deviceType);
        return Response.ok(deviceTypeAndOptionsInfo).build();
    }

    public Long getCurrentCampaignVersion(long id) {
        return timeOfUseCampaignService.getCampaign(id).map(TimeOfUseCampaign::getVersion).orElse(null);
    }
}
