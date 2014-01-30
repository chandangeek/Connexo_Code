package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.services.DeviceConfigurationService;
import com.energyict.mdw.core.DeviceType;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
    public DeviceTypeInfos getAllDeviceTypes() {
        DeviceTypeInfos deviceTypeInfos = new DeviceTypeInfos();
        deviceTypeInfos.deviceTypes = new ArrayList<>();
        for (DeviceType deviceType : deviceConfigurationService.findAllDeviceTypes()) {
            deviceTypeInfos.deviceTypes.add(new DeviceTypeInfo(deviceType));
        }

        return deviceTypeInfos;
    }
}
