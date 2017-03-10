/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;

import com.energyict.mdc.device.data.DeviceService;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;

/**
 * Factory for creating {@link UsagePointRegisterInfo}
 */
public class UsagePointRegisterInfoFactory extends AbstractUsagePointChannelInfoFactory implements RegisterInfoFactory {

    private final ReadingTypeInfoFactory readingTypeInfoFactory;
    private final DeviceService deviceService;

    @Inject
    public UsagePointRegisterInfoFactory(ReadingTypeInfoFactory readingTypeInfoFactory, DeviceService deviceService) {
        this.readingTypeInfoFactory = readingTypeInfoFactory;
        this.deviceService = deviceService;
    }

    @Override
    protected DeviceService getDeviceService() {
        return deviceService;
    }

    @Override
    public UsagePointRegisterInfo from(Channel channel, UsagePoint usagePoint, UsagePointMetrologyConfiguration metrologyConfiguration) {
        UsagePointRegisterInfo info = new UsagePointRegisterInfo();
        ReadingType readingType = channel.getMainReadingType();
        info.id = channel.getId();
        Instant lastDateTime = channel.getLastDateTime();
        info.dataUntil = lastDateTime != null ? lastDateTime.toEpochMilli() : null;
        info.measurementTime = lastDateTime != null ? lastDateTime.toEpochMilli() : null;
        info.readingType = readingTypeInfoFactory.from(readingType);
        info.deviceRegisters = new ArrayList<>();
        fillDevicePartList(info.deviceRegisters, readingType, metrologyConfiguration, usagePoint);
        return info;
    }
}
