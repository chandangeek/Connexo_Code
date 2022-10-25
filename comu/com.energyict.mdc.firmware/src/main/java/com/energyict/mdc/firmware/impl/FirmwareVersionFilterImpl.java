/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersionFilter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation of {@link FirmwareVersionFilter}
 */
public class FirmwareVersionFilterImpl implements FirmwareVersionFilter {

    private final DeviceType deviceType;
    private final Set<FirmwareType> firmwareTypes = new HashSet<>();
    private final Set<FirmwareStatus> firmwareStatuses = new HashSet<>();
    private final Set<String> firmwareVersions = new HashSet<>();
    private Range<Integer> rankRange = Range.all();

    public FirmwareVersionFilterImpl(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    @Override
    public DeviceType getDeviceType() {
        return this.deviceType;
    }

    @Override
    public void addFirmwareTypes(Collection<FirmwareType> firmwareTypes) {
        this.firmwareTypes.addAll(firmwareTypes);
    }

    @Override
    public void addFirmwareStatuses(Collection<FirmwareStatus> firmwareStatuses) {
        this.firmwareStatuses.addAll(firmwareStatuses);
    }

    @Override
    public void addFirmwareVersions(Collection<String> firmwareVersions) {
        this.firmwareVersions.addAll(firmwareVersions);
    }

    @Override
    public List<String> getFirmwareVersions() {
        return ImmutableList.copyOf(firmwareVersions);
    }

    @Override
    public List<FirmwareType> getFirmwareTypes() {
        return ImmutableList.copyOf(this.firmwareTypes);
    }

    @Override
    public List<FirmwareStatus> getFirmwareStatuses() {
        return ImmutableList.copyOf(this.firmwareStatuses);
    }

    @Override
    public void setRankRange(Range<Integer> rankRange) {
        this.rankRange = rankRange;
    }

    @Override
    public Range<Integer> getRankRange() {
        return rankRange;
    }
}
