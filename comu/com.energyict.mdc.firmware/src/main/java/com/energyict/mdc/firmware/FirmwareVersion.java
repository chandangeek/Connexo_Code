package com.energyict.mdc.firmware;

import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.protocol.api.firmware.BaseFirmwareVersion;

public interface FirmwareVersion extends BaseFirmwareVersion {

    FirmwareType getFirmwareType();

    void setFirmwareType(FirmwareType firmwareType);

    FirmwareStatus getFirmwareStatus();

    void setFirmwareStatus(FirmwareStatus firmwareStatus);

    String getFirmwareVersion();

    void setFirmwareVersion(String firmwareVersion);

    byte[] getFirmwareFile();

    DeviceType getDeviceType();

    void setFirmwareFile(byte[] firmwareFile);

    /**
     * Initializes the firmware file without updating database<br/>
     * This should only be used when constructing a new {@Link FirmwareVersion},
     * elese please use {@link #setFirmwareFile(byte[])}
     *
     * @param firmwareFile
     */
    void initFirmwareFile(byte[] firmwareFile);

    void setExpectedFirmwareSize(long fileSize);

    void validate();

    void deprecate();

    void update();

    long getVersion();

}