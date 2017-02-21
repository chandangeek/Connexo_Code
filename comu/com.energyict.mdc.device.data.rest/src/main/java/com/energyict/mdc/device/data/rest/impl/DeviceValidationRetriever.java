/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import java.util.List;

/**
 * Created by bbl on 18/06/2016.
 */
public class DeviceValidationRetriever {

    private List<Device> activeValidatedDevices;

    public DeviceValidationRetriever(DeviceService deviceService) {
        this(deviceService, null);
    }

    public DeviceValidationRetriever(DeviceService deviceService, List<Device> domainObjects) {
        if (domainObjects != null) {
            activeValidatedDevices = deviceService.findActiveValidatedDevices(domainObjects);
        } else {
            activeValidatedDevices = null;
        }
    }

    public boolean isValidationActive(Device device) {
        if (activeValidatedDevices != null) {
            return activeValidatedDevices.contains(device);
        }
        return device.forValidation().isValidationActive();
    }
}
