package com.energyict.mdc.firmware;

import com.energyict.mdc.device.config.DeviceType;

public interface FirmwareVersion {

    long getId();

    FirmwareType getFirmwareType();

    FirmwareStatus getFirmwareStatus();

    String getFirmwareVersion();

    byte[] getFirmwareFile();

    DeviceType getDeviceType();

    void validate();

}
