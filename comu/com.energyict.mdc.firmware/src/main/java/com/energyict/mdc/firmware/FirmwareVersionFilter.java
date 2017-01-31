/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware;

import com.energyict.mdc.device.config.DeviceType;

import java.util.List;
/**
 * Models the filtering that can be applied by client code to count
 * or find {@link FirmwareVersion}s
 * by a number of criteria that can be mixed.
 */

public interface FirmwareVersionFilter {
    /**
     * add a List of {@link FirmwareType} as criterion to the filter
     * @param firmwareTypes to add as criterion
     */
    void addFirmwareTypes(List<FirmwareType> firmwareTypes);

    /**
     * add a List of {@link FirmwareStatus} as a criterion to the filter
     * @param firmwareStatuses to add as criterion
     */
    void addFirmwareStatuses(List<FirmwareStatus> firmwareStatuses);

    /**
     * add a List of {@link FirmwareVersion} as a criterion to the filter
     * @param firmwareVersions to add as criterion
     */
    void addFirmwareVersions(List<String> firmwareVersions);

    /**
     * The {@link DeviceType} criterion of the filter
     * @return  the DeviceType of the filter
     */
    DeviceType getDeviceType();


    /**
     * Return the {@link FirmwareVersion} set as criterion as a list of Strings (FirmwareVersion.getFirmwareVersion)
     * @return  the list of {@link FirmwareVersion} set as criterion
     */
    List<String> getFirmwareVersions();

    /**
     * Return the List of {@link FirmwareType} set as criterion
      * @return  the list of {@link FirmwareType} set as criterion
     */
    List<FirmwareType> getFirmwareTypes();

    /**
    * Return the list of {@link FirmwareStatus} set as criterion
    * @return  the list of {@link FirmwareStatus} set as criterion
    */
    List<FirmwareStatus> getFirmwareStatuses();
}