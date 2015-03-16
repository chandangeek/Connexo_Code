package com.energyict.mdc.firmware;

import com.energyict.mdc.common.HasId;
import com.energyict.mdc.device.config.DeviceType;

public interface FirmwareVersion extends HasId{

    long getId();

    FirmwareType getFirmwareType();

    FirmwareStatus getFirmwareStatus();

    String getFirmwareVersion();

    byte[] getFirmwareFile();

    DeviceType getDeviceType();

    void validate();

}
