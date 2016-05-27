package com.energyict.mdc.firmware;

import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.protocol.api.firmware.BaseFirmwareVersion;

import java.io.InputStream;
import java.time.Instant;

public interface FirmwareVersion extends BaseFirmwareVersion {

    FirmwareType getFirmwareType();

    void setFirmwareType(FirmwareType firmwareType);

    FirmwareStatus getFirmwareStatus();

    void setFirmwareStatus(FirmwareStatus firmwareStatus);

    String getFirmwareVersion();

    void setFirmwareVersion(String firmwareVersion);

    byte[] getFirmwareFile();

    InputStream getFirmwareFileAsStream();

    DeviceType getDeviceType();

    void setFirmwareFile(byte[] firmwareFile);

    void setExpectedFirmwareSize(long fileSize);

    void validate();

    void deprecate();

    void update();

    long getVersion();

    Instant getModTime();
}