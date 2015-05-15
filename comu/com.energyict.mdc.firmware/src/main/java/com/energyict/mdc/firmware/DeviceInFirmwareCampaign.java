package com.energyict.mdc.firmware;

import com.energyict.mdc.device.data.Device;

public interface DeviceInFirmwareCampaign {
    FirmwareManagementDeviceStatus getStatus();
    void setStatus(FirmwareManagementDeviceStatus status);
    Device getDevice();

    void startFirmwareProcess();
}
