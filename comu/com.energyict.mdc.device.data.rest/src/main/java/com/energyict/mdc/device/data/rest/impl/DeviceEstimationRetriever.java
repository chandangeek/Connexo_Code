package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceEstimation;
import com.energyict.mdc.device.data.DeviceService;

import java.util.List;

/**
 * Created by bbl on 18/06/2016.
 */
public class DeviceEstimationRetriever {

    private final List<DeviceEstimation> deviceEstimations;

    public DeviceEstimationRetriever(DeviceService deviceService) {
        this(deviceService, null);
    }

    public DeviceEstimationRetriever(DeviceService deviceService, List<Device> domainObjects) {
        if (domainObjects != null) {
            deviceEstimations = deviceService.findDeviceEstimations(domainObjects).find();
        } else {
            deviceEstimations = null;
        }
    }

    public boolean isEstimationActive(Device device) {
        if (deviceEstimations != null) {
            return deviceEstimations.stream().filter(de -> de.getDevice().equals(device)).findFirst().map(DeviceEstimation::isEstimationActive).orElse(false);
        }
        return device.forEstimation().isEstimationActive();
    }
}
