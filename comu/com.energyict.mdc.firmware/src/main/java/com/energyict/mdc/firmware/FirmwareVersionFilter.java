/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware;

import com.energyict.mdc.common.device.config.DeviceType;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.util.Collection;
import java.util.List;

/**
 * Models the filtering that can be applied by client code to count
 * or find {@link FirmwareVersion}s
 * by a number of criteria that can be mixed.
 */
@ProviderType
public interface FirmwareVersionFilter {
    /**
     * add a List of {@link FirmwareType} as criterion to the filter
     *
     * @param firmwareTypes to add as criterion
     */
    void addFirmwareTypes(Collection<FirmwareType> firmwareTypes);

    /**
     * add a List of {@link FirmwareStatus} as a criterion to the filter
     *
     * @param firmwareStatuses to add as criterion
     */
    void addFirmwareStatuses(Collection<FirmwareStatus> firmwareStatuses);

    /**
     * add a List of {@link FirmwareVersion} as a criterion to the filter
     *
     * @param firmwareVersions to add as criterion
     */
    void addFirmwareVersions(Collection<String> firmwareVersions);

    /**
     * add a Range of ranks as a criterion to the filter
     *
     * @param rankRange to add as criterion
     */
    void setRankRange(Range<Integer> rankRange);

    /**
     * The {@link DeviceType} criterion of the filter
     *
     * @return the DeviceType of the filter
     */
    DeviceType getDeviceType();

    /**
     * Return the {@link FirmwareVersion} set as criterion as a list of Strings (FirmwareVersion.getFirmwareVersion)
     *
     * @return the list of {@link FirmwareVersion} set as criterion
     */
    List<String> getFirmwareVersions();

    /**
     * Return the List of {@link FirmwareType} set as criterion
     *
     * @return the list of {@link FirmwareType} set as criterion
     */
    List<FirmwareType> getFirmwareTypes();

    /**
     * Return the list of {@link FirmwareStatus} set as criterion
     *
     * @return the list of {@link FirmwareStatus} set as criterion
     */
    List<FirmwareStatus> getFirmwareStatuses();

    /**
     * Return the range of ranks set as criterion
     *
     * @return the range of ranks set as criterion
     */
    Range<Integer> getRankRange();
}
