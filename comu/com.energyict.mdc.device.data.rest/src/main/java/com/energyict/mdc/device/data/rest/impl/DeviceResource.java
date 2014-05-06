package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.imp.DeviceImportService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/devices")
public class DeviceResource {
    private final DeviceImportService deviceImportService;
    private final DeviceDataService deviceDataService;
    private final ResourceHelper resourceHelper;


    @Inject
    public DeviceResource(ResourceHelper resourceHelper, DeviceImportService deviceImportService, DeviceDataService deviceDataService) {
        this.resourceHelper = resourceHelper;
        this.deviceImportService = deviceImportService;
        this.deviceDataService = deviceDataService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getAllDevices(@BeanParam QueryParameters queryParameters) {
        List<Device> allDevices = deviceDataService.findAllDevices();
        List<DeviceInfo> deviceInfos = DeviceInfo.from(allDevices, deviceImportService);
        return PagedInfoList.asJson("devices", deviceInfos, queryParameters);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceInfo findDeviceType(@PathParam("id") long id) {
        Device device = resourceHelper.findDeviceByIdOrThrowException(id);


        return DeviceInfo.from(device, deviceImportService);
    }




}
