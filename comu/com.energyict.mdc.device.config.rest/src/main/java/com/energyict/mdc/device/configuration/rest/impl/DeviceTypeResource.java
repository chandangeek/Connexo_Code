package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.common.services.SortOrder;
import com.energyict.mdc.services.DeviceConfigurationService;
import com.energyict.mdw.core.DeviceType;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/devicetypes")
public class DeviceTypeResource {

    private final DeviceConfigurationService deviceConfigurationService;

    @Inject
    public DeviceTypeResource(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceTypeInfos getAllDeviceTypes(@QueryParam("start") Integer start, @QueryParam("limit") Integer limit, @QueryParam("sortColumns") List<String> sortColumns) {
        DeviceTypeInfos deviceTypeInfos = new DeviceTypeInfos();
        deviceTypeInfos.deviceTypes = new ArrayList<>();
        Finder<DeviceType> deviceTypeFinder = deviceConfigurationService.allDeviceTypes().paged(start, limit);
        for (String sortColumn : sortColumns) {
            deviceTypeFinder.sorted(sortColumn, SortOrder.ASCENDING);
        }
        List<DeviceType> allDeviceTypes = deviceTypeFinder.find();

        for (DeviceType deviceType : allDeviceTypes) {
            deviceTypeInfos.deviceTypes.add(new DeviceTypeInfo(deviceType));
        }

        return deviceTypeInfos;
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public DeviceTypeInfo createDeviceType(DeviceTypeInfo deviceTypeInfo) {

        return new DeviceTypeInfo(null);
    }
}
