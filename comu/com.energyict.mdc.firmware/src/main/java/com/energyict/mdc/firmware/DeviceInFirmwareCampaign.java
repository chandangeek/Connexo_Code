package com.energyict.mdc.firmware;

import com.energyict.mdc.device.data.Device;

public interface DeviceInFirmwareCampaign {
    FirmwareUpgradeDeviceStatus getStatus();
    void setStatus(FirmwareUpgradeDeviceStatus status);
    Device getDevice();
}
