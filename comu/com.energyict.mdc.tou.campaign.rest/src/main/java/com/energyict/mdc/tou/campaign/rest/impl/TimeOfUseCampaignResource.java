/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign.rest.impl;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Where;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.tou.campaign.Pair;
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

@Path("/touCampaigns")
public class TimeOfUseCampaignResource {

    private final TimeOfUseCampaignService timeOfUseCampaignService;
    private final TimeOfUseCampaignInfoFactory timeOfUseCampaignInfoFactory;
    private final Thesaurus thesaurus;
    private final ConcurrentModificationExceptionFactory conflictFactory;


    @Inject
    public TimeOfUseCampaignResource(TimeOfUseCampaignService timeOfUseCampaignService, TimeOfUseCampaignInfoFactory timeOfUseCampaignInfoFactory,
                                     Thesaurus thesaurus, ConcurrentModificationExceptionFactory conflictFactory) {
        this.timeOfUseCampaignService = timeOfUseCampaignService;
        this.timeOfUseCampaignInfoFactory = timeOfUseCampaignInfoFactory;
        this.thesaurus = thesaurus;
        this.conflictFactory = conflictFactory;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.VIEW_TOU_CAMPAIGNS, Privileges.Constants.ADMINISTER_TOU_CAMPAIGNS})
    public Response getToUCampaigns(@BeanParam JsonQueryParameters queryParameters) {
        QueryStream<? extends TimeOfUseCampaign> campaigns = timeOfUseCampaignService.streamAllCampaigns().join(ServiceCall.class)
                .sorted(Order.descending("serviceCall.createTime"));
        queryParameters.getStart().ifPresent(campaigns::skip);
        queryParameters.getLimit().ifPresent(limit -> campaigns.limit(limit + 1));
        List<TimeOfUseCampaignInfo> touCampaigns = campaigns.map(o -> getOverviewCampaignInfo(o, o.getServiceCall().getState())).collect(Collectors.toList());
        return Response.ok(PagedInfoList.fromPagedList("touCampaigns", touCampaigns, queryParameters)).build();
    }

    @GET
    @Transactional
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.VIEW_TOU_CAMPAIGNS, Privileges.Constants.ADMINISTER_TOU_CAMPAIGNS})
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
    @RolesAllowed({Privileges.Constants.VIEW_TOU_CAMPAIGNS, Privileges.Constants.ADMINISTER_TOU_CAMPAIGNS})
    public Response getToUCampaignDevices(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter filter) {
        List<String> states = filter.getStringList("status").stream().map(DefaultState::valueOf).map(DefaultState::getKey).collect(Collectors.toList());
        QueryStream<? extends TimeOfUseItem> devices = timeOfUseCampaignService.streamDevicesInCampaigns().join(ServiceCall.class).join(State.class)
                .sorted(Order.ascending("device")).filter(Where.where("serviceCall.parent").isEqualTo(timeOfUseCampaignService.findCampaignServiceCall(name).get()));
        if(states.size()>0){
            devices.filter(Where.where("serviceCall.state.name").in(states));
        }
        queryParameters.getStart().ifPresent(devices::skip);
        queryParameters.getLimit().ifPresent(limit -> devices.limit(limit + 1));
        List<DeviceInCampaignInfo> deviceInCampaignInfo = devices.map(o -> getDeviceInCampaignInfo(o)).collect(Collectors.toList());
        return Response.ok(PagedInfoList.fromPagedList("devicesInCampaign", deviceInCampaignInfo, queryParameters)).build();
    }

    @POST
    @Transactional
    @Path("/create")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.ADMINISTER_TOU_CAMPAIGNS})
    public Response createToUCampaign(TimeOfUseCampaignInfo timeOfUseCampaignInfo) {
        timeOfUseCampaignService.createToUCampaign(timeOfUseCampaignInfoFactory.build(timeOfUseCampaignInfo));
        return Response.ok(timeOfUseCampaignInfo).build();
    }

    @PUT
    @Transactional
    @Path("/retryDevice")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.ADMINISTER_TOU_CAMPAIGNS})
    public Response retryDevice(IdWithNameInfo id) {
        Pair<Device, ServiceCall> pair = timeOfUseCampaignService.retryDevice(((Integer) id.id).longValue());
        DeviceInCampaignInfo deviceInCampaignInfo = new DeviceInCampaignInfo(new IdWithNameInfo(pair.o1.getId(), pair.o1.getName()),
                getStatus(pair.o2.getState(), thesaurus),
                pair.o2.getCreationTime(),
                (pair.o2.getState().equals(DefaultState.CANCELLED)
                        || pair.o2.getState().equals(DefaultState.SUCCESSFUL)) ? pair.o2.getLastModificationTime() : null);
        return Response.ok(deviceInCampaignInfo).build();
    }

    @PUT
    @Transactional
    @Path("/cancelDevice")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.ADMINISTER_TOU_CAMPAIGNS})
    public Response cancelDevice(IdWithNameInfo id) {
        Pair<Device, ServiceCall> pair = timeOfUseCampaignService.cancelDevice(((Integer) id.id).longValue());
        DeviceInCampaignInfo deviceInCampaignInfo = new DeviceInCampaignInfo(new IdWithNameInfo(pair.o1.getId(), pair.o1.getName()),
                getStatus(pair.o2.getState(), thesaurus),
                pair.o2.getCreationTime(),
                (pair.o2.getState().equals(DefaultState.CANCELLED)
                        || pair.o2.getState().equals(DefaultState.SUCCESSFUL)) ? pair.o2.getLastModificationTime() : null);
        return Response.ok(deviceInCampaignInfo).build();
    }

    @PUT
    @Transactional
    @Path("/{name}/edit")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.ADMINISTER_TOU_CAMPAIGNS})
    public Response edit(@PathParam("name") String name, TimeOfUseCampaignInfo timeOfUseCampaignInfo) {
        timeOfUseCampaignService.findAndLockToUCampaignByIdAndVersion(timeOfUseCampaignInfo.id, timeOfUseCampaignInfo.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(timeOfUseCampaignInfo.name)
                        .withActualVersion(() -> getCurrentCampaignVersion(timeOfUseCampaignInfo.id))
                        .supplier());
        timeOfUseCampaignService.edit(name, timeOfUseCampaignInfoFactory.build(timeOfUseCampaignInfo));
        return Response.ok(timeOfUseCampaignInfo).build();
    }

    @PUT
    @Transactional
    @Path("/{name}/cancel")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.ADMINISTER_TOU_CAMPAIGNS})
    public Response cancel(@PathParam("name") String name) {
        timeOfUseCampaignService.cancelCampaign(name);
        return Response.ok().build();
    }

    @GET
    @Transactional
    @Path("/devicetypes")
    @Produces(MediaType.APPLICATION_JSON)
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
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.VIEW_TOU_CAMPAIGNS, Privileges.Constants.ADMINISTER_TOU_CAMPAIGNS})
    public Response getSendOptionsForType(@QueryParam("type") Long deviceTypeId) {
        DeviceTypeAndOptionsInfo deviceTypeAndOptionsInfo = new DeviceTypeAndOptionsInfo();
        DeviceType deviceType = timeOfUseCampaignService.getDeviceTypesWithCalendars().stream()
                .filter(deviceType1 -> deviceType1.getId() == deviceTypeId).findAny().get();
        deviceTypeAndOptionsInfo.deviceType = new IdWithNameInfo(deviceType.getId(), deviceType.getName());
        deviceTypeAndOptionsInfo.calendars = new ArrayList<>();
        deviceTypeAndOptionsInfo.fullCalendar = false;
        deviceTypeAndOptionsInfo.withActivationDate = false;
        deviceTypeAndOptionsInfo.specialDays = false;
        deviceType.getAllowedCalendars().stream()
                .filter(allowedCalendar -> !allowedCalendar.isGhost())
                .forEach(allowedCalendar -> deviceTypeAndOptionsInfo.calendars.add(new IdWithNameInfo(allowedCalendar.getCalendar().get().getId(), allowedCalendar.getName())));
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

    private DeviceInCampaignInfo getDeviceInCampaignInfo(TimeOfUseItem o){
        return new DeviceInCampaignInfo(new IdWithNameInfo(o.getDevice().getId(),o.getDevice().getName()),
                getStatus(o.getServiceCall().getState(),thesaurus),
                        o.getServiceCall().getCreationTime(),
                        (o.getServiceCall().getState().equals(DefaultState.CANCELLED)
                                || o.getServiceCall().getState().equals(DefaultState.SUCCESSFUL)) ? o.getServiceCall().getLastModificationTime() : null);
    }

    private TimeOfUseCampaignInfo getOverviewCampaignInfo(TimeOfUseCampaign campaign, DefaultState status) {
        TimeOfUseCampaignInfo info = timeOfUseCampaignInfoFactory.from(campaign);
        ServiceCall campaignsServiceCall = timeOfUseCampaignService.findCampaignServiceCall(campaign.getName()).get();
        info.startedOn = campaignsServiceCall.getCreationTime();
        info.finishedOn = (campaignsServiceCall.getState().equals(DefaultState.CANCELLED)
                || campaignsServiceCall.getState().equals(DefaultState.SUCCESSFUL)) ? campaignsServiceCall.getLastModificationTime() : null;
        info.status = status.equals(DefaultState.SUCCESSFUL) ? thesaurus.getString(TranslationKeys.STATUS_COMPLETED.getKey(), TranslationKeys.STATUS_COMPLETED.getDefaultFormat())
                : thesaurus.getString(status.getKey(), status.getDefaultFormat());
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
                return thesaurus.getString(TranslationKeys.STATUS_SUCCESSFUL.getKey(), TranslationKeys.STATUS_SUCCESSFUL.getDefaultFormat());
            case FAILED:
                return thesaurus.getString(TranslationKeys.STATUS_FAILED.getKey(), TranslationKeys.STATUS_FAILED.getDefaultFormat());
            case REJECTED:
                return thesaurus.getString(TranslationKeys.STATUS_CONFIGURATION_ERROR.getKey(), TranslationKeys.STATUS_CONFIGURATION_ERROR.getDefaultFormat());
            case ONGOING:
                return thesaurus.getString(TranslationKeys.STATUS_ONGOING.getKey(), TranslationKeys.STATUS_ONGOING.getDefaultFormat());
            case PENDING:
                return thesaurus.getString(TranslationKeys.STATUS_PENDING.getKey(), TranslationKeys.STATUS_PENDING.getDefaultFormat());
            case CANCELLED:
                return thesaurus.getString(TranslationKeys.STATUS_CANCELED.getKey(), TranslationKeys.STATUS_CANCELED.getDefaultFormat());
        }
        return defaultState.getDisplayName(thesaurus);
    }

    public Long getCurrentCampaignVersion(long id) {
        return timeOfUseCampaignService.getCampaign(id).map(TimeOfUseCampaign::getVersion).orElse(null);
    }
}
