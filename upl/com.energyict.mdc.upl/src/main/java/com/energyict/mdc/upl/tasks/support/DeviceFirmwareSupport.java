package com.energyict.mdc.upl.tasks.support;

import aQute.bnd.annotation.ProviderType;
import com.energyict.mdc.upl.meterdata.CollectedFirmwareVersion;

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

    /**
     * Indication whether or not the FirmwareType 'HES CA config image' is supported.
     *
     * @return FALSE by default
     */
    default boolean supportsCaConfigImageVersion() {
        return false;
    }
}