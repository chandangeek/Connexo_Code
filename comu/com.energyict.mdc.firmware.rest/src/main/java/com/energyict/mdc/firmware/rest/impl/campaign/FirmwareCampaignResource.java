/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl.campaign;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Where;
import com.energyict.mdc.firmware.DeviceInFirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaignException;
import com.energyict.mdc.firmware.FirmwareCampaignService;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.rest.impl.MessageSeeds;
import com.energyict.mdc.firmware.rest.impl.ResourceHelper;
import com.energyict.mdc.firmware.security.Privileges;

import com.google.common.collect.Range;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Path("/campaigns")
public class FirmwareCampaignResource {

    private final FirmwareCampaignService firmwareCampaignService;
    private final FirmwareCampaignInfoFactory campaignInfoFactory;
    private final DeviceInFirmwareCampaignInfoFactory deviceInCampaignInfoFactory;
    private final ResourceHelper resourceHelper;
    private final Thesaurus thesaurus;
    private final ConcurrentModificationExceptionFactory conflictFactory;

    @Inject
    public FirmwareCampaignResource(FirmwareService firmwareService, ResourceHelper resourceHelper, FirmwareCampaignInfoFactory campaignInfoFactory,
                                    DeviceInFirmwareCampaignInfoFactory deviceInCampaignInfoFactory, Thesaurus thesaurus, ConcurrentModificationExceptionFactory conflictFactory) {
        this.firmwareCampaignService = firmwareService.getFirmwareCampaignService();
        this.resourceHelper = resourceHelper;
        this.campaignInfoFactory = campaignInfoFactory;
        this.deviceInCampaignInfoFactory = deviceInCampaignInfoFactory;
        this.thesaurus = thesaurus;
        this.conflictFactory = conflictFactory;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.VIEW_FIRMWARE_CAMPAIGN, Privileges.Constants.ADMINISTRATE_FIRMWARE_CAMPAIGN})
    public Response getFirmwareCampaigns(@BeanParam JsonQueryParameters queryParameters) {
        QueryStream<? extends FirmwareCampaign> campaigns = firmwareCampaignService.streamAllCampaigns().join(ServiceCall.class)
                .sorted(Order.descending("serviceCall.createTime"));
        queryParameters.getStart().ifPresent(campaigns::skip);
        queryParameters.getLimit().ifPresent(limit -> campaigns.limit(limit + 1));
        List<FirmwareCampaignInfo> touCampaigns = campaigns
                .map(campaignInfoFactory::getOverviewCampaignInfo).collect(Collectors.toList());
        return Response.ok(PagedInfoList.fromPagedList("touCampaigns", touCampaigns, queryParameters)).build();
    }

    @GET
    @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.VIEW_FIRMWARE_CAMPAIGN, Privileges.Constants.ADMINISTRATE_FIRMWARE_CAMPAIGN})
    public Response getFirmwareCampaignById(@PathParam("id") long firmwareCampaignId) {
        FirmwareCampaign firmwareCampaign = firmwareCampaignService.getFirmwareCampaignById(firmwareCampaignId)
                .orElseThrow(() -> new FirmwareCampaignException(thesaurus, MessageSeeds.FIRMWARE_CAMPAIGN_NOT_FOUND, firmwareCampaignId));
        FirmwareCampaignInfo firmwareCampaignInfo = campaignInfoFactory.getOverviewCampaignInfo(firmwareCampaign);
        return Response.ok(firmwareCampaignInfo).build();
    }

    @POST
    @Transactional
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_FIRMWARE_CAMPAIGN})
    public Response addFirmwareCampaign(FirmwareCampaignInfo info) {
        FirmwareCampaign firmwareCampaign = campaignInfoFactory.build(info);
        return Response.ok(campaignInfoFactory.from(firmwareCampaign)).build();
    }

    @PUT
    @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_FIRMWARE_CAMPAIGN})
    public Response editFirmwareCampaign(@PathParam("id") long firmwareCampaignId, FirmwareCampaignInfo firmwareCampaignInfo) {
        FirmwareCampaign firmwareCampaign = firmwareCampaignService.findAndLockFirmwareCampaignByIdAndVersion(firmwareCampaignId, firmwareCampaignInfo.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(firmwareCampaignInfo.name)
                        .withActualVersion(() -> getCurrentCampaignVersion(firmwareCampaignInfo.id))
                        .supplier());
        Range<Instant> timeRange = campaignInfoFactory.retrieveRealUploadRange(firmwareCampaignInfo);
        firmwareCampaign.setName(firmwareCampaignInfo.name);
        firmwareCampaign.setUploadPeriodStart(timeRange.lowerEndpoint());
        firmwareCampaign.setUploadPeriodEnd(timeRange.upperEndpoint());
        firmwareCampaign.update();
        return Response.ok(campaignInfoFactory.from(firmwareCampaign)).build();
    }

    @PUT
    @Transactional
    @Path("/{id}/cancel")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_FIRMWARE_CAMPAIGN})
    public Response cancel(@PathParam("id") long id, FirmwareCampaignInfo firmwareCampaignInfo) {
        FirmwareCampaign firmwareCampaign = firmwareCampaignService.findAndLockFirmwareCampaignByIdAndVersion(firmwareCampaignInfo.id, firmwareCampaignInfo.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(firmwareCampaignInfo.name)
                        .withActualVersion(() -> getCurrentCampaignVersion(firmwareCampaignInfo.id))
                        .supplier());
        firmwareCampaign.cancel();
        return Response.ok().build();
    }

    @GET
    @Transactional
    @Path("/{id}/devices")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.VIEW_FIRMWARE_CAMPAIGN, Privileges.Constants.ADMINISTRATE_FIRMWARE_CAMPAIGN})
    public Response getDevicesForFirmwareCampaign(@PathParam("id") long firmwareCampaignId, @BeanParam JsonQueryFilter jsonQueryFilter, @BeanParam JsonQueryParameters queryParameters) {
        List<String> states = jsonQueryFilter.getStringList("status").stream().map(DefaultState::valueOf).map(DefaultState::getKey).collect(Collectors.toList());
        QueryStream<? extends DeviceInFirmwareCampaign> devices = firmwareCampaignService.streamDevicesInCampaigns().join(ServiceCall.class).join(ServiceCall.class).join(State.class)
                .sorted(Order.ascending("device")).filter(Where.where("serviceCall.parent.id").isEqualTo(firmwareCampaignId));
        if (states.size() > 0) {
            devices.filter(Where.where("serviceCall.state.name").in(states));
        }
        queryParameters.getStart().ifPresent(devices::skip);
        queryParameters.getLimit().ifPresent(limit -> devices.limit(limit + 1));
        List<DeviceInFirmwareCampaignInfo> deviceInCampaignInfo = devices.map(o -> deviceInCampaignInfoFactory.create(o.getDevice(), o.getServiceCall())).collect(Collectors.toList());
        return Response.ok(PagedInfoList.fromPagedList("devicesInCampaign", deviceInCampaignInfo, queryParameters)).build();
    }

    public Long getCurrentCampaignVersion(long id) {
        return firmwareCampaignService.getFirmwareCampaignById(id).map(FirmwareCampaign::getVersion).orElse(null);
    }
}
