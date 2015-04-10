package com.energyict.mdc.firmware.rest.impl;

import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareVersion;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class ResourceHelper {
    private final ExceptionFactory exceptionFactory;
    private final DeviceConfigurationService deviceConfigurationService;
    private final DeviceService deviceService;
    private final FirmwareService firmwareService;

    @Inject
    public ResourceHelper(ExceptionFactory exceptionFactory, DeviceConfigurationService deviceConfigurationService, DeviceService deviceService, FirmwareService firmwareService) {
        this.exceptionFactory = exceptionFactory;
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceService = deviceService;
        this.firmwareService = firmwareService;
    }

    public DeviceType findDeviceTypeOrElseThrowException(long deviceTypeId) {
        return deviceConfigurationService.findDeviceType(deviceTypeId).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    public Device findDeviceByMridOrThrowException(String mRID) {
        return deviceService.findByUniqueMrid(mRID).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    public FirmwareVersion getFirmwareVersionByIdOrThrowException(Long id) {
        return firmwareService.getFirmwareVersionById(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }
}
