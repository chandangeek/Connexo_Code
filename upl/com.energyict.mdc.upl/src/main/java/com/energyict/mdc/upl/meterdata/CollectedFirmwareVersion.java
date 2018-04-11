package com.energyict.mdc.upl.meterdata;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.tasks.DataCollectionConfiguration;

import java.util.Optional;

/**
 * A CollectedFirmwareVersion identifies a specific Active/Passive FirmwareVersion object on a device
 */
public interface CollectedFirmwareVersion extends CollectedData {

    /**
     * @return the DeviceIdentifier for which these FirmwareVersions are applicable
     */
    DeviceIdentifier getDeviceIdentifier();

    /**
     * @return the String representation of the currently active Meter firmware version. An empty optional is returned when no active Meter firmware version is present.
     */
    Optional<String> getActiveMeterFirmwareVersion();

    void setActiveMeterFirmwareVersion(String activeMeterFirmwareVersion);

    /**
     * @return the String representation of the passive Meter firmware version. An empty optional is returned when no passive Meter firmware version is present.
     */
    Optional<String> getPassiveMeterFirmwareVersion();

    void setPassiveMeterFirmwareVersion(String passiveMeterFirmwareVersion);

    /**
     * @return the String representation of the currently active Communication firmware version. An empty optional is returned when no active communication firmware version is present.
     */
    Optional<String> getActiveCommunicationFirmwareVersion();

    void setActiveCommunicationFirmwareVersion(String activeCommunicationFirmwareVersion);

    /**
     * @return the String representation of the passive Communication firmware version. An empty optional is returned when no passive Communication firmware version is present.
     */
    Optional<String> getPassiveCommunicationFirmwareVersion();

    void setPassiveCommunicationFirmwareVersion(String passiveCommunicationFirmwareVersion);

    /**
     * @return the String representation of the currently active CA config image version. An empty optional is returned when no active CA config image version is present.
     */
    Optional<String> getActiveCaConfigImageVersion();

    void setActiveCaConfigImageVersion(String activeCaConfigImageVersion);

    /**
     * @return the String representation of the passive CA config image version. An empty optional is returned when no passive CA config image version is present.
     */
    Optional<String> getPassiveCaConfigImageVersion();

    void setPassiveCaConfigImageVersion(String passiveCaConfigImageVersion);

    void setDataCollectionConfiguration(DataCollectionConfiguration configuration);

}