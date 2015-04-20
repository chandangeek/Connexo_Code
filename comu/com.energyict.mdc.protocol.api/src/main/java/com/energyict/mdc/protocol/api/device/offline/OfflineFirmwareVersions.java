package com.energyict.mdc.protocol.api.device.offline;

import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareType;

/**
 * Represent all the Offline FirmwareVersions of a Device
 */
public interface OfflineFirmwareVersions {

    interface OfflineFirmwareVersion {
        String getVersion();
        FirmwareStatus getFirmwareStatus();
        FirmwareType getFirmwareType();
    }

    OfflineFirmwareVersion getActiveMeterFirmwareVersion();
    OfflineFirmwareVersion getPassiveMeterFirmwareVersion();
    OfflineFirmwareVersion getActiveCommunicationFirmwareVersion();
    OfflineFirmwareVersion getPassiveCommunicationFirmwareVersion();

}
