/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware;

import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.protocol.api.firmware.BaseFirmwareVersion;

import aQute.bnd.annotation.ProviderType;

import java.io.InputStream;
import java.time.Instant;
import java.util.Optional;

@ProviderType
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

    void delete();

    void update();

    long getVersion();

    Instant getModTime();

    String getImageIdentifier();

    void setImageIdentifier(String imageIdentifier);

    int getRank();

    Optional<FirmwareVersion> getMeterFirmwareDependency();

    void setMeterFirmwareDependency(FirmwareVersion meterFirmwareDependency);

    Optional<FirmwareVersion> getCommunicationFirmwareDependency();

    void setCommunicationFirmwareDependency(FirmwareVersion communicationFirmwareDependency);

    Optional<FirmwareVersion> getAuxiliaryFirmwareDependency();

    void setAuxiliaryFirmwareDependency(FirmwareVersion auxiliaryFirmwareDependency);

    default int compareTo(FirmwareVersion o) {
        return compare(this, o);
    }

    static int compare(FirmwareVersion fw1, FirmwareVersion fw2) {
        if (!fw1.getDeviceType().equals(fw2.getDeviceType())) {
            throw new IllegalStateException("Can't compare ranks of firmware versions on different device types!");
        }
        return Integer.compare(fw1.getRank(), fw2.getRank());
    }
}
