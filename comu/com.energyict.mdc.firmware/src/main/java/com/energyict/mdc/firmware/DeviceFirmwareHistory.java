package com.energyict.mdc.firmware;

import java.util.List;

/**
 * History of ActivatedFirmwareVersions of a device
 * Copyrights EnergyICT
 * Date: 1/04/2016
 * Time: 12:53
 */
public interface DeviceFirmwareHistory {

    /**
     * The list of {@link DeviceFirmwareVersionHistoryRecord}s for given device
     * @return the list of {@link ActivatedFirmwareVersion} owned by given device
     */
    List<DeviceFirmwareVersionHistoryRecord> history();

    /**
     * The list of {@link DeviceFirmwareVersionHistoryRecord}s for given
     * firmwareType owned by given device
     * @param firmwareType the firmware type to match
     * @return the list of {@link ActivatedFirmwareVersion} owned by given device
     */
    List<DeviceFirmwareVersionHistoryRecord> history(FirmwareType firmwareType);



}
