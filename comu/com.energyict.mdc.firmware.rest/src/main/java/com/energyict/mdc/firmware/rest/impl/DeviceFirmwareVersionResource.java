/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.ListPager;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.firmware.rest.impl.campaign.DeviceInFirmwareCampaignInfo;
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
    private final DeviceFirmwareLifecycleHistoryInfoFactory deviceFirmwareLifecycleHistoryInfoFactory;

    @Inject
    public DeviceFirmwareVersionResource(DeviceFirmwareVersionInfoFactory versionInfoFactory, ResourceHelper resourceHelper, DeviceFirmwareLifecycleHistoryInfoFactory deviceFirmwareLifecycleHistoryInfoFactory) {
        this.versionInfoFactory = versionInfoFactory;
        this.resourceHelper = resourceHelper;
        this.deviceFirmwareLifecycleHistoryInfoFactory = deviceFirmwareLifecycleHistoryInfoFactory;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.data.security.Privileges.Constants.VIEW_DEVICE, com.energyict.mdc.device.data.security.Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public Response getFirmwareVersionsOnDevice(@PathParam("name") String name) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        return Response.ok(versionInfoFactory.from(device)).build();
    }

    @GET
    @Transactional
    @Path("/firmwarehistory")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.data.security.Privileges.Constants.VIEW_DEVICE, com.energyict.mdc.device.data.security.Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public Response getDeviceFirmwareHistory(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        List<DeviceFirmwareLifecycleHistoryInfo> deviceFirmwareLifecycleHistoryInfoList = deviceFirmwareLifecycleHistoryInfoFactory.getDeviceFirmwareHistoryInfosListFromDevice(device);
        PagedInfoList pagedInfoList = PagedInfoList.fromPagedList("firmwareHistory", ListPager.of(deviceFirmwareLifecycleHistoryInfoList).from(queryParameters).find(), queryParameters);
        return Response.ok(pagedInfoList).build();
    }

    @PUT
    @Transactional
    @Path("/{campaign}/cancel")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_FIRMWARE_CAMPAIGN})
    public Response cancelDeviceInFirmwareCampaign(@PathParam("name") String name, @PathParam("campaign") long campaignId) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        Optional<DeviceInFirmwareCampaignInfo> deviceInFirmwareCampaignInfo = resourceHelper.cancelDeviceInFirmwareCampaign(device, campaignId);
        return Response.ok(deviceInFirmwareCampaignInfo.isPresent() ? deviceInFirmwareCampaignInfo.get() : "").build();
    }

    @PUT
    @Transactional
    @Path("/{campaign}/retry")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_FIRMWARE_CAMPAIGN})
    public Response retryDeviceInFirmwareCampaign(@PathParam("name") String name, @PathParam("campaign") long campaignId) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        Optional<DeviceInFirmwareCampaignInfo> deviceInFirmwareCampaignInfo = resourceHelper.retryDeviceInFirmwareCampaign(device, campaignId);
        return Response.ok(deviceInFirmwareCampaignInfo.isPresent() ? deviceInFirmwareCampaignInfo.get() : "").build();
    }

}