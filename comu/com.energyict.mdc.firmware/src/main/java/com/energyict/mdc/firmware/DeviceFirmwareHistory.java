/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware;

import java.util.List;

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
