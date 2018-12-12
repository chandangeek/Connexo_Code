/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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

    /**
     * Initializes the firmware file without updating database<br/>
     * This should only be used when constructing a new {@link FirmwareVersion},
     * elese please use {@link #setFirmwareFile(byte[])}
     *
     * @param firmwareFile byte array containing the firmware
     */
    void initFirmwareFile(byte[] firmwareFile);

    void setExpectedFirmwareSize(long fileSize);

    void validate();

    void deprecate();

    void update();

    long getVersion();

    Instant getModTime();

    String getImageIdentifier();

    void setImageIdentifier(String imageIdentifier);
}