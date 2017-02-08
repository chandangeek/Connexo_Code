/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

public class DeviceHistoryResource {

    private ResourceHelper resourceHelper;
    private DeviceLifeCycleHistoryInfoFactory deviceLifeCycleHistoryInfoFactory;
    private DeviceFirmwareHistoryInfoFactory deviceFirmwareHistoryInfoFactory;
    private MeterActivationInfoFactory meterActivationInfoFactory;

    @Inject
    public DeviceHistoryResource(ResourceHelper resourceHelper, DeviceLifeCycleHistoryInfoFactory deviceLifeCycleStatesHistoryInfoFactory,
                                 DeviceFirmwareHistoryInfoFactory deviceFirmwareHistoryInfoFactory, MeterActivationInfoFactory meterActivationInfoFactory) {
        this.resourceHelper = resourceHelper;
        this.deviceLifeCycleHistoryInfoFactory = deviceLifeCycleStatesHistoryInfoFactory;
        this.deviceFirmwareHistoryInfoFactory = deviceFirmwareHistoryInfoFactory;
        this.meterActivationInfoFactory = meterActivationInfoFactory;
    }

    @GET
    @Transactional
    @Path("/devicelifecyclechanges")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE})
    public Response getDeviceLifeCycleStatesHistory(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        return Response.ok(deviceLifeCycleHistoryInfoFactory.createDeviceLifeCycleChangeInfos(device)).build();
    }

    @GET
    @Transactional
    @Path("/firmwarechanges")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE})
    public Response getFirmwareHistory(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        return Response.ok(deviceFirmwareHistoryInfoFactory.createDeviceFirmwareHistoryInfos(device)).build();
    }

    @GET
    @Transactional
    @Path("/meteractivations")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE})
    public PagedInfoList getMeterActivationsHistory(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        List<MeterActivationInfo> meterActivationInfoList = device.getMeterActivationsMostRecentFirst().stream()
                .map(meterActivation -> meterActivationInfoFactory.asInfo(meterActivation, device))
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("meterActivations", meterActivationInfoList, queryParameters);
    }
}
