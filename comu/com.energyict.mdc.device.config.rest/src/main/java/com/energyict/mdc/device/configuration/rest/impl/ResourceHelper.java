package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class ResourceHelper {

    private final DeviceConfigurationService deviceConfigurationService;

    @Inject
    public ResourceHelper(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    public DeviceType findDeviceTypeByIdOrThrowException(long id) {
        DeviceType deviceType = deviceConfigurationService.findDeviceType(id);
        if (deviceType == null) {
            throw new WebApplicationException("No device type with id " + id, Response.Status.NOT_FOUND);
        }
        return deviceType;
     }

    public DeviceConfiguration findDeviceConfigurationForDeviceTypeOrThrowException(DeviceType deviceType, long deviceConfigurationId) {
        for (DeviceConfiguration deviceConfiguration : deviceType.getConfigurations()) {
            if (deviceConfiguration.getId()==deviceConfigurationId) {
                return deviceConfiguration;
            }
        }
        throw new WebApplicationException("No such device configuration for the device type", Response.status(Response.Status.NOT_FOUND).entity("No such device configuration for the device type").build());
    }

}
