package com.energyict.mdc.multisense.api.impl.utils;

import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

/**
 * Created by bvn on 7/15/15.
 */
public class ResourceHelper {

    private final DeviceService deviceService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public ResourceHelper(DeviceService deviceService, ExceptionFactory exceptionFactory) {
        this.deviceService = deviceService;
        this.exceptionFactory = exceptionFactory;
    }

    public Device findDeviceByMrIdOrThrowException(String mrid) {
        return deviceService
                .findByUniqueMrid(mrid)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE));
    }
}
