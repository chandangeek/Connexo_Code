package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

import java.util.Optional;

/**
 * Represents a <i>holder</i> for collected FirmwareVersions
 */
public interface ServerCollectedFirmwareVersions {

    /**
     * @return the DeviceIdentifier for which these FirmwareVersions are applicable
     */
    DeviceIdentifier getDeviceIdentifier();

    /**
     * @return the String representation of the currently active Meter firmware version. An empty optional is returned when no active Meter firmware version is present.
     */
    Optional<String> getActiveMeterFirmwareVersion();

    /**
     * @return the String representation of the passive Meter firmware version. An empty optional is returned when no passive Meter firmware version is present.
     */
    Optional<String> getPassiveMeterFirmwareVersion();

    /**
     * @return the String representation of the currently active Communication firmware version. An empty optional is returned when no active communication firmware version is present.
     */
    Optional<String> getActiveCommunicationFirmwareVersion();

    /**
     * @return the String representation of the passive Communication firmware version. An empty optional is returned when no passive Communication firmware version is present.
     */
    Optional<String> getPassiveCommunicationFirmwareVersion();
}
