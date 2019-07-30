package com.energyict.mdc.upl.tasks.support;

import aQute.bnd.annotation.ProviderType;
import com.energyict.mdc.upl.meterdata.CollectedFirmwareVersion;

/**
 * Defines functionality related to Firmware Version information
 */
@ProviderType
public interface DeviceFirmwareSupport {

    /**
     * Calls the protocol for the firmware versions.
     * The default implementation will assume it's a single meter or master. For slave topologies use the variant with
     * serial number as parameter
     *
     *  @return the collected FirmwareVersion
     */
    default CollectedFirmwareVersion getFirmwareVersions() {
        return getFirmwareVersions(null);
    }

    /**
     * Calls the protocol for the firmware versions.
     *  @param serialNumber - optional parameter used to identify the actual device requesting the serial number
     *                      this is used where there are slave devices without direct communication, so the master
     *                      will be called, thus we need to know that it is the slave device firmware needed
     *
     * @return the collected FirmwareVersion
     */
    default CollectedFirmwareVersion getFirmwareVersions(String serialNumber){
        return null;
    }

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