package com.energyict.mdc.firmware;

import com.energyict.mdc.device.data.Device;

import java.time.Instant;

public interface DeviceInFirmwareCampaign {
    FirmwareCampaign getFirmwareCampaign();
    Device getDevice();
    FirmwareManagementDeviceStatus getStatus();
    void setStatus(FirmwareManagementDeviceStatus status);
    Instant getStartedOn();
    Instant getFinishedOn();
    void cancel();
    void retry();
}
