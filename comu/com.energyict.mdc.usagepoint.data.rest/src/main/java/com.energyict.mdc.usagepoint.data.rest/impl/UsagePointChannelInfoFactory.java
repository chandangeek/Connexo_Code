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
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.rest.TimeDurationInfo;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import javax.inject.Inject;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Optional;

public class UsagePointChannelInfoFactory extends AbstractUsagePointChannelInfoFactory implements ChannelInfoFactory {
    private final ReadingTypeInfoFactory readingTypeInfoFactory;
    private final DeviceService deviceService;
    private final int SECONDS_PER_MINUTE = 60;

    @Inject
    public UsagePointChannelInfoFactory(ReadingTypeInfoFactory readingTypeInfoFactory, DeviceService deviceService) {
        this.readingTypeInfoFactory = readingTypeInfoFactory;
        this.deviceService = deviceService;
    }

    @Override
    protected DeviceService getDeviceService() {
        return deviceService;
    }

    @Override
    public UsagePointChannelInfo from(com.elster.jupiter.metering.Channel channel, UsagePoint usagePoint,
                                      UsagePointMetrologyConfiguration metrologyConfiguration) {
        ReadingType readingType = channel.getMainReadingType();
        UsagePointChannelInfo info = new UsagePointChannelInfo();

        info.id = channel.getId();
        Instant lastDateTime = channel.getLastDateTime();
        info.dataUntil = lastDateTime != null ? lastDateTime : null;
        info.readingType = readingTypeInfoFactory.from(readingType);
        info.flowUnit = ReadingTypeUnitConversion.isFlowUnit(readingType.getUnit()) ? "flow" : "volume";

        Optional<TemporalAmount> intervalLength = channel.getIntervalLength();
        if (intervalLength.isPresent()) {
            switch (readingType.getMacroPeriod()) {
                case DAILY:
                    info.interval = new TimeDurationInfo(TimeDuration.days(Math.toIntExact(intervalLength.get()
                            .get(ChronoUnit.DAYS))));
                    break;
                case MONTHLY:
                    info.interval = new TimeDurationInfo(TimeDuration.months(Math.toIntExact(intervalLength.get()
                            .get(ChronoUnit.MONTHS))));
                    break;
                case YEARLY:
                    info.interval = new TimeDurationInfo(TimeDuration.months(Math.toIntExact(intervalLength.get().get(ChronoUnit.YEARS))));
                    break;
                default:
                    info.interval = new TimeDurationInfo(TimeDuration.minutes(Math.toIntExact(intervalLength.get()
                            .get(ChronoUnit.SECONDS)) / SECONDS_PER_MINUTE));
            }
        }

        info.deviceChannels = new ArrayList<>();

        fillDevicePartList(info.deviceChannels, readingType, metrologyConfiguration, usagePoint);

        return info;
    }

    @Override
    IdWithNameInfo createDeviceChannelInfo(Device device, Channel ch) {
        ReadingType mainReadingType = ch.getMainReadingType();
        com.energyict.mdc.device.data.Channel channelOnDevice = device.getChannels()
                .stream()
                .filter(deviceChannel -> ch.getReadingTypes()
                        .contains(deviceChannel.getReadingType()))
                .findFirst()
                .orElse(null);
        return new IdWithNameInfo(
                channelOnDevice != null ? channelOnDevice.getId() : null,
                mainReadingType.getFullAliasName());
    }
}
