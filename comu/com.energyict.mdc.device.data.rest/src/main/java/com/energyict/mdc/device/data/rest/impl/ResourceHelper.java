package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class ResourceHelper {

    private final DeviceDataService deviceDataService;

    @Inject
    public ResourceHelper(DeviceDataService deviceDataService) {
        super();
        this.deviceDataService = deviceDataService;
    }

    public Device findDeviceByIdOrThrowException(long id) {
        Device device = deviceDataService.findDeviceById(id);
        if (device == null) {
            throw new WebApplicationException("No device with id " + id, Response.Status.NOT_FOUND);
        }
        return device;
    }

}
