/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.ReadingTypeUnitConversion;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirementsCollector;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.data.DeviceService;

import javax.inject.Inject;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsagePointChannelInfoFactory {
    private final ReadingTypeInfoFactory readingTypeInfoFactory;
    private final DeviceService deviceService;
    private final int SECONDS_PER_MINUTE = 60;

    @Inject
    public UsagePointChannelInfoFactory(ReadingTypeInfoFactory readingTypeInfoFactory, DeviceService deviceService) {
        this.readingTypeInfoFactory = readingTypeInfoFactory;
        this.deviceService = deviceService;
    }

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

        ReadingTypeDeliverable readingTypeDeliverable = metrologyConfiguration.getDeliverables().stream()
                .filter(deliverable -> deliverable.getReadingType().equals(readingType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Mismatch between channels configuration and reading type deliverable"));
        ReadingTypeRequirementsCollector requirementsCollector = new ReadingTypeRequirementsCollector();
        readingTypeDeliverable.getFormula().getExpressionNode().accept(requirementsCollector);
        MeterActivation meterActivationOld = null;
        List<MeterActivation> meterActivations = usagePoint.getMeterActivations();
        for (int i = meterActivations.size() - 1; i >= 0; i--) {
            MeterActivation meterActivation = meterActivations.get(i);
            if (meterActivationOld != null
                    && meterActivation.getMeter().equals(meterActivationOld.getMeter())
                    && !info.deviceChannels.isEmpty()
                    && meterActivationOld.getStart().equals(meterActivation.getEnd())) {
                info.deviceChannels.get(info.deviceChannels.size() - 1).from = meterActivation.getStart().toEpochMilli();
            } else {
                UsagePointDeviceChannelInfo deviceChannelInfo = new UsagePointDeviceChannelInfo();
                meterActivation.getMeter()
                        .map(Meter::getAmrId)
                        .map(Long::parseLong)
                        .flatMap(deviceService::findDeviceById)
                        .ifPresent(device -> {
                            deviceChannelInfo.device = device.getName();
                            deviceChannelInfo.from = meterActivation.getStart().toEpochMilli();
                            deviceChannelInfo.until = meterActivation.getEnd() != null ?
                                    meterActivation.getEnd().toEpochMilli() :
                                    null;
                            requirementsCollector.getReadingTypeRequirements().stream()
                                    .flatMap(readingTypeRequirement -> readingTypeRequirement.getMatchingChannelsFor(
                                            meterActivation.getChannelsContainer()).stream())
                                    .forEach(ch -> {
                                        ReadingType mainReadingType = ch.getMainReadingType();
                                        com.energyict.mdc.device.data.Channel channelOnDevice = device.getChannels().stream()
                                                .filter(deviceChannel -> ch.getReadingTypes()
                                                        .contains(deviceChannel.getReadingType()))
                                                .findFirst()
                                                .orElse(null);
                                        deviceChannelInfo.channel = new IdWithNameInfo(
                                                channelOnDevice != null ? channelOnDevice.getId() : null,
                                                mainReadingType.getFullAliasName());
                                    });
                            info.deviceChannels.add(deviceChannelInfo);
                        });
            }
            meterActivationOld = meterActivation;
        }

        return info;
    }
}
