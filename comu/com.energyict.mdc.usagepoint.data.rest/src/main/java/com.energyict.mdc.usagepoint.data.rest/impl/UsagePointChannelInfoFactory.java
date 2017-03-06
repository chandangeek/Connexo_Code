/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.ReadingTypeUnitConversion;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.data.DeviceService;

import javax.inject.Inject;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Predicate;

public class UsagePointChannelInfoFactory extends AbstractUsagePointChannelInfoFactory<UsagePointChannelInfo, UsagePointDeviceChannelInfo>{
    private final ReadingTypeInfoFactory readingTypeInfoFactory;
    private final DeviceService deviceService;
    private final int SECONDS_PER_MINUTE = 60;

    @Inject
    public UsagePointChannelInfoFactory(ReadingTypeInfoFactory readingTypeInfoFactory, DeviceService deviceService) {
        super("channels");
        this.readingTypeInfoFactory = readingTypeInfoFactory;
        this.deviceService = deviceService;
    }

    @Override
    protected ReadingTypeInfoFactory getReadingTypeInfoFactory() {
        return readingTypeInfoFactory;
    }

    @Override
    protected DeviceService getDeviceService() {
        return deviceService;
    }

    @Override
    Predicate<Channel> getRegisterFilter() { return Channel::isRegular; }

    @Override
    UsagePointDevicePartInfoBuilder<UsagePointDeviceChannelInfo> getUsagePointDevicePartInfoBuilder() {
        return new UsagePointDeviceChannelInfoBuilder();
    }

    @Override
    void setFrom(UsagePointDeviceChannelInfo element, long value) {
        element.from = value;
    }

    @Override
    UsagePointChannelInfo from(Channel channel, UsagePoint usagePoint, UsagePointMetrologyConfiguration metrologyConfiguration) {
        ReadingType readingType = channel.getMainReadingType();
        UsagePointChannelInfo info = new UsagePointChannelInfo();

        info.id = channel.getId();
        Instant lastDateTime = channel.getLastDateTime();
        info.dataUntil = lastDateTime != null ? lastDateTime.toEpochMilli() : null;
        info.readingType = readingTypeInfoFactory.from(readingType);
        info.flowUnit = ReadingTypeUnitConversion.isFlowUnit(readingType.getUnit()) ? "flow" : "volume";

        Optional<TemporalAmount> intervalLength = channel.getIntervalLength();
        if (intervalLength.isPresent()) {
            switch (readingType.getMacroPeriod()) {
                case DAILY:
                    info.interval = new TimeDurationInfo(TimeDuration.days(Math.toIntExact(intervalLength.get().get(ChronoUnit.DAYS))));
                    break;
                case MONTHLY:
                    info.interval = new TimeDurationInfo(TimeDuration.months(Math.toIntExact(intervalLength.get().get(ChronoUnit.MONTHS))));
                    break;
                default:
                    info.interval = new TimeDurationInfo(TimeDuration.minutes(Math.toIntExact(intervalLength.get().get(ChronoUnit.SECONDS)) / SECONDS_PER_MINUTE));
            }
        }

        info.deviceChannels = new ArrayList<>();

        fillDevicePartList(info.deviceChannels,readingType,metrologyConfiguration,usagePoint);

        return info;
    }

    private class UsagePointDeviceChannelInfoBuilder extends UsagePointDevicePartInfoBuilder<UsagePointDeviceChannelInfo> {
        @Override
        UsagePointDeviceChannelInfo build() {
            UsagePointDeviceChannelInfo info = new UsagePointDeviceChannelInfo();
            info.from = from;
            info.channel = channel;
            info.device = device;
            info.until = until;
            return info;
        }
    }
}
