package com.energyict.mdc.protocol.api.tasks.support;

import aQute.bnd.annotation.ProviderType;
import com.energyict.mdc.protocol.api.device.data.CollectedFirmwareVersion;

/**
 * Defines functionality related to Firmware Version information
 */
@ProviderType
public interface DeviceFirmwareSupport {

    /**
     * @return the collected FirmwareVersion
     */
    CollectedFirmwareVersion getFirmwareVersions();

    /**
     * Indication whether or not the FirmwareType 'Communication' is supported.
     * Most meter only have one <i>adjustable</i> firmware so by default it is set to false.
     *
     * @return FALSE by default
     */
    default boolean supportsCommunicationFirmwareVersion() {
        return false;
    }

}
