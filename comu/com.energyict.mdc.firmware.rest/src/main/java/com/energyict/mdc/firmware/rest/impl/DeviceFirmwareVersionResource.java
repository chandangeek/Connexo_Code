/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

@Path("/devices/{name}/firmwares")
public class DeviceFirmwareVersionResource {
    private final DeviceFirmwareVersionInfoFactory versionInfoFactory;
    private final ResourceHelper resourceHelper;
    private final DeviceFirmwareHistoryInfosFactory deviceFirmwareHistoryInfosFactory;

    @Inject
    public DeviceFirmwareVersionResource(DeviceFirmwareVersionInfoFactory versionInfoFactory, ResourceHelper resourceHelper, DeviceFirmwareHistoryInfosFactory deviceFirmwareHistoryInfosFactory) {
        this.versionInfoFactory = versionInfoFactory;
        this.resourceHelper = resourceHelper;
        this.deviceFirmwareHistoryInfosFactory = deviceFirmwareHistoryInfosFactory;
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.data.security.Privileges.Constants.VIEW_DEVICE, com.energyict.mdc.device.data.security.Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public Response getFirmwareVersionsOnDevice(@PathParam("name") String name) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        return Response.ok(versionInfoFactory.from(device)).build();
    }

    @GET
    @Transactional
    @Path("/firmwarehistory")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.data.security.Privileges.Constants.VIEW_DEVICE, com.energyict.mdc.device.data.security.Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public Response getDeviceFirmwareHistory(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        List<DeviceFirmwareHistoryInfos> deviceFirmwareHistoryInfosList = deviceFirmwareHistoryInfosFactory.from(device);
        PagedInfoList pagedInfoList = PagedInfoList.fromCompleteList("firmwareHistory", deviceFirmwareHistoryInfosList, queryParameters);
        return Response.ok(pagedInfoList).build();
    }

    @PUT
    @Transactional
    @Path("/{campaign}/cancel")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_FIRMWARE_CAMPAIGN})
    public Response cancelDeviceInFirmwareCampaign(@PathParam("name") String name, @PathParam("campaign") long campaignId) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        FirmwareCampaign campaign = resourceHelper.findFirmwareCampaignOrThrowException(campaignId);
        Optional<DeviceInFirmwareCampaignInfo> deviceInFirmwareCampaignInfo = resourceHelper.cancelDeviceInFirmwareCampaign(campaign, device);
        return Response.ok(deviceInFirmwareCampaignInfo.isPresent() ? deviceInFirmwareCampaignInfo.get() : "").build();
    }

    @PUT
    @Transactional
    @Path("/{campaign}/retry")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_FIRMWARE_CAMPAIGN})
    public Response retryDeviceInFirmwareCampaign(@PathParam("name") String name, @PathParam("campaign") long campaignId) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        FirmwareCampaign campaign = resourceHelper.findFirmwareCampaignOrThrowException(campaignId);
        Optional<DeviceInFirmwareCampaignInfo> deviceInFirmwareCampaignInfo = resourceHelper.retryDeviceInFirmwareCampaign(campaign, device);
        return Response.ok(deviceInFirmwareCampaignInfo.isPresent() ? deviceInFirmwareCampaignInfo.get() : "").build();
    }

}