package com.energyict.mdc.firmware;

import com.energyict.mdc.device.data.Device;

import java.time.Instant;

public interface DeviceInFirmwareCampaign {
    FirmwareManagementDeviceStatus getStatus();
    void setStatus(FirmwareManagementDeviceStatus status);
    Device getDevice();

    FirmwareManagementDeviceStatus updateStatus();

    Instant getStartedOn();
    Instant getFinishedOn();

    void cancel();
}
