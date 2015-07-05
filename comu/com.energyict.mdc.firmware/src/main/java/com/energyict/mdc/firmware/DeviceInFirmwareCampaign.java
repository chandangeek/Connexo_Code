package com.energyict.mdc.firmware;

import com.energyict.mdc.device.data.Device;

import java.time.Instant;

public interface DeviceInFirmwareCampaign {
    FirmwareManagementDeviceStatus getStatus();
    void setStatus(FirmwareManagementDeviceStatus status);
    Device getDevice();
    Instant getStartedOn();
    Instant getFinishedOn();
    void cancel();
}
