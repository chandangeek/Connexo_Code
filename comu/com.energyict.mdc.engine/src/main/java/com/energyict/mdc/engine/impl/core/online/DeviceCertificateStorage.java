package com.energyict.mdc.engine.impl.core.online;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

public class DeviceCertificateStorage {

    private final DeviceService deviceService;

    public DeviceCertificateStorage(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    void updateDeviceCSR(Device device, byte [] csr){
        deviceService.newDeviceCSRFrom(device, csr);
    }
}
