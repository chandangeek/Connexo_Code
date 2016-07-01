package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirementChecker;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.time.TimeDuration;

import javax.inject.Inject;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

public class UsagePointChannelInfoFactory {
    private final ReadingTypeInfoFactory readingTypeInfoFactory;
    private final int SECONDS_PER_MINUTE = 60;

    @Inject
    public UsagePointChannelInfoFactory(ReadingTypeInfoFactory readingTypeInfoFactory) {
        this.readingTypeInfoFactory = readingTypeInfoFactory;
    }

    UsagePointChannelInfo from(Channel channel, UsagePoint usagePoint, UsagePointMetrologyConfiguration metrologyConfiguration) {
        ReadingType readingType = channel.getMainReadingType();
        UsagePointChannelInfo info = new UsagePointChannelInfo();

        Instant lastDateTime = channel.getLastDateTime();
        info.dataUntil = lastDateTime != null ? lastDateTime.toEpochMilli() : null;
        info.readingType = readingTypeInfoFactory.from(readingType);

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
        ReadingTypeRequirementChecker requirementChecker = new ReadingTypeRequirementChecker();
        readingTypeDeliverable.getFormula().getExpressionNode().accept(requirementChecker);


        usagePoint.getMeterActivations().stream().forEach(meterActivation -> {
            UsagePointDeviceChannelInfo deviceChannelInfo = new UsagePointDeviceChannelInfo();
            deviceChannelInfo.mRID = meterActivation.getMeter().get().getMRID();
            deviceChannelInfo.from = meterActivation.getStart().toEpochMilli();
            deviceChannelInfo.until = meterActivation.getEnd() != null ? meterActivation.getEnd().toEpochMilli() : null;
            requirementChecker.getReadingTypeRequirements().stream()
                    .flatMap(readingTypeRequirement -> readingTypeRequirement.getMatchingChannelsFor(meterActivation.getChannelsContainer()).stream())
                    .forEach(ch -> {
                        deviceChannelInfo.channel = new IdWithNameInfo(channel.getId(), channel.getMainReadingType().getFullAliasName());
                    });
            info.deviceChannels.add(deviceChannelInfo);

        });

        Collections.reverse(info.deviceChannels);

        return info;
    }
}