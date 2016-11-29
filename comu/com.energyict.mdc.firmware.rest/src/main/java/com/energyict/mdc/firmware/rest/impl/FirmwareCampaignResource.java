package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.firmware.DeviceInFirmwareCampaign;
import com.energyict.mdc.firmware.DevicesInFirmwareCampaignFilter;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaignStatus;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.security.Privileges;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@Path("/campaigns")
public class FirmwareCampaignResource {
    private final FirmwareService firmwareService;
    private final ResourceHelper resourceHelper;
    private final FirmwareCampaignInfoFactory campaignInfoFactory;
    private final DeviceInFirmwareCampaignInfoFactory deviceInCampaignInfoFactory;
    private final MdcPropertyUtils mdcPropertyUtils;

    @Inject
    public FirmwareCampaignResource(FirmwareService firmwareService, ResourceHelper resourceHelper, FirmwareCampaignInfoFactory campaignInfoFactory, DeviceInFirmwareCampaignInfoFactory deviceInCampaignInfoFactory, MdcPropertyUtils mdcPropertyUtils) {
        this.firmwareService = firmwareService;
        this.resourceHelper = resourceHelper;
        this.campaignInfoFactory = campaignInfoFactory;
        this.deviceInCampaignInfoFactory = deviceInCampaignInfoFactory;
        this.mdcPropertyUtils = mdcPropertyUtils;
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.VIEW_FIRMWARE_CAMPAIGN})
    public Response getFirmwareCampaigns(@BeanParam JsonQueryParameters queryParameters){
        List<FirmwareCampaignInfo> firmwareCampaigns = firmwareService.getFirmwareCampaigns()
                .from(queryParameters)
                .stream()
                .map(campaignInfoFactory::from)
                .collect(Collectors.toList());
        return Response.ok(PagedInfoList.fromPagedList("firmwareCampaigns", firmwareCampaigns, queryParameters)).build();
    }

    @GET @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.VIEW_FIRMWARE_CAMPAIGN})
    public Response getFirmwareCampaignById(@PathParam("id") long firmwareCampaignId){
        FirmwareCampaign firmwareCampaign = resourceHelper.findFirmwareCampaignOrThrowException(firmwareCampaignId);
        return Response.ok(campaignInfoFactory.from(firmwareCampaign)).build();
    }

    @POST @Transactional
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_FIRMWARE_CAMPAIGN})
    public Response addFirmwareCampaign(FirmwareCampaignInfo info){
        FirmwareCampaign firmwareCampaign = campaignInfoFactory.create(info);
        firmwareCampaign.save();
        return Response.ok(campaignInfoFactory.from(firmwareCampaign)).build();
    }

    @PUT @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_FIRMWARE_CAMPAIGN})
    public Response editFirmwareCampaign(@PathParam("id") long firmwareCampaignId, FirmwareCampaignInfo info){
        info.id = firmwareCampaignId;
        FirmwareCampaign firmwareCampaign = resourceHelper.lockFirmwareCampaign(info);
        if(info.status.id.equals(FirmwareCampaignStatus.CANCELLED.name())){
            this.firmwareService.cancelFirmwareCampaign(firmwareCampaign);
        } else {
            // Since only the name/comWindow is editable
            // info.writeTo(firmwareCampaign, mdcPropertyUtils);
            if (firmwareCampaign.getName().compareTo(info.name) != 0) {
                firmwareCampaign.setName(info.name);
            }
            if (info.timeBoundaryStart != null && info.timeBoundaryEnd != null) {
                ComWindow newWindow = new ComWindow(info.timeBoundaryStart, info.timeBoundaryEnd);
                if (!firmwareCampaign.getComWindow().equals(newWindow)) {
                    firmwareCampaign.setComWindow(newWindow);
                }
            }
            firmwareCampaign.save();
        }
        return Response.ok(campaignInfoFactory.from(firmwareCampaign)).build();
    }

    @DELETE @Transactional
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_FIRMWARE_CAMPAIGN})
    public Response deleteFirmwareCampaign(@PathParam("id") long firmwareCampaignId, FirmwareCampaignInfo info){
        info.id = firmwareCampaignId;
        FirmwareCampaign firmwareCampaign = resourceHelper.lockFirmwareCampaign(info);
        firmwareCampaign.delete();
        return Response.noContent().build();
    }

    @GET @Transactional
    @Path("/{id}/devices")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.VIEW_FIRMWARE_CAMPAIGN})
    public Response getDevicesForFirmwareCampaign(@PathParam("id") long firmwareCampaignId, @BeanParam JsonQueryFilter jsonQueryFilter, @BeanParam JsonQueryParameters queryParameters){
        DevicesInFirmwareCampaignFilter filter = buildFilterFromJsonQuery(jsonQueryFilter).withFirmwareCampaignId(firmwareCampaignId);
        List<DeviceInFirmwareCampaign> devices = firmwareService.getDevicesForFirmwareCampaign(filter).from(queryParameters).find();
        return Response.ok(PagedInfoList.fromPagedList("devices", deviceInCampaignInfoFactory.from(devices), queryParameters)).build();
    }

    private DevicesInFirmwareCampaignFilter buildFilterFromJsonQuery(JsonQueryFilter jsonQueryFilter){
        DevicesInFirmwareCampaignFilter filter = firmwareService.filterForDevicesInFirmwareCampaign();
        if (jsonQueryFilter.hasProperty(FilterOption.status.name())) {
            filter.withStatus(jsonQueryFilter.getStringList(FilterOption.status.name()));
        }
        return filter;
    }

    enum FilterOption {
        campaign,
        status
    }
}
