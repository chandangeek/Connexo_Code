package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceAttributesProvider;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Optional;

@Component(name = "com.energyict.mdc.device.data.impl.EndDeviceAttributesProvider",
        service = {EndDeviceAttributesProvider.class},
        property = "name=MultiSenseEndDeviceAttributesProvider", immediate = true)
public class MultiSenseEndDeviceAttributesProvider implements EndDeviceAttributesProvider {

    private volatile DeviceService deviceService;

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Override
    public Optional<String> getSerialNumber(EndDevice endDevice) {
        return deviceService.findDeviceByMrid(endDevice.getMRID()).map(Device::getSerialNumber);
    }

    @Override
    public Optional<String> getType(EndDevice endDevice) {
        return deviceService.findDeviceByMrid(endDevice.getMRID()).map(Device::getDeviceType).map(DeviceType::getName);
    }
}
