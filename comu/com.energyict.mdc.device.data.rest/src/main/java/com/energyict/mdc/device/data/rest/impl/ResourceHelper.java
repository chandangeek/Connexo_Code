package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class ResourceHelper {

    private final DeviceDataService deviceDataService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public ResourceHelper(DeviceDataService deviceDataService, ExceptionFactory exceptionFactory) {
        super();
        this.deviceDataService = deviceDataService;
        this.exceptionFactory = exceptionFactory;
    }

    public Device findDeviceByIdOrThrowException(long id) {
        Device device = deviceDataService.findDeviceById(id);
        if (device == null) {
            throw new WebApplicationException("No device with id " + id, Response.Status.NOT_FOUND);
        }
        return device;
    }

    public Device findDeviceByMrIdOrThrowException(String mRID) {
        Device device = deviceDataService.findByUniqueMrid(mRID);
        if (device == null) {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_DEVICE, mRID);
        }
        return device;
    }

}
