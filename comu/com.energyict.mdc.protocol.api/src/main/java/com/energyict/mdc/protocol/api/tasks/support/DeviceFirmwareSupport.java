package com.energyict.mdc.protocol.api.tasks.support;

import com.energyict.mdc.protocol.api.device.data.CollectedFirmwareVersion;

/**
 * Defines functionality related to Firmware Version information
 */
public interface DeviceFirmwareSupport {

    /**
     * @return the collected FirmwareVersion
     */
    CollectedFirmwareVersion getFirmwareVersions();

}
