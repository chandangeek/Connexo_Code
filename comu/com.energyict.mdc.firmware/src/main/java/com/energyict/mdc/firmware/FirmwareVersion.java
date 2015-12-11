package com.energyict.mdc.firmware;

import com.elster.jupiter.util.HasId;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.protocol.api.firmware.BaseFirmwareVersion;

public interface FirmwareVersion extends HasId, BaseFirmwareVersion {

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

    void deprecate();

    void update();

    long getVersion();

}