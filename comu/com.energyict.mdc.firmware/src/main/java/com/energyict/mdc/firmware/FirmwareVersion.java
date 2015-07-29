package com.energyict.mdc.firmware;

import com.elster.jupiter.util.HasId;
import com.energyict.mdc.device.config.DeviceType;

public interface FirmwareVersion extends HasId {

    FirmwareType getFirmwareType();

    void setFirmwareType(FirmwareType firmwareType);

    FirmwareStatus getFirmwareStatus();

    void setFirmwareStatus(FirmwareStatus firmwareStatus);

    String getFirmwareVersion();

    void setFirmwareVersion(String firmwareVersion);

    byte[] getFirmwareFile();

    DeviceType getDeviceType();

    void setFirmwareFile(byte[] firmwareFile);

    void setExpectedFirmwareSize(long fileSize);

    void validate();

    void save();

    void deprecate();

}