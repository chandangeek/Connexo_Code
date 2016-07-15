package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.util.streams.Functions;

import javax.inject.Inject;
import java.util.List;

public class ResourceHelper {
    private final MeteringService meteringService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public ResourceHelper(MeteringService meteringService,
                          ExceptionFactory exceptionFactory) {
        super();
        this.meteringService = meteringService;
        this.exceptionFactory = exceptionFactory;
    }

    public UsagePoint fetchUsagePoint(String mRID) {
        return meteringService.findUsagePoint(mRID)
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_USAGE_POINT_FOR_MRID, mRID));
    }

    public EffectiveMetrologyConfigurationOnUsagePoint getEffectiveMetrologyConfigurationOnUsagePoint(String mRID) {
        UsagePoint usagePoint = this.fetchUsagePoint(mRID);

        return this.getEffectiveMetrologyConfigurationOnUsagePoint(usagePoint);
    }

    public EffectiveMetrologyConfigurationOnUsagePoint getEffectiveMetrologyConfigurationOnUsagePoint(UsagePoint usagePoint) {
        return usagePoint.getEffectiveMetrologyConfiguration()
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_EFFECTIVE_METROLOGY_CONFIGURATION_ON_USAGE_POINT, usagePoint.getMRID()));
    }

    public Channel findChannelOnUsagePointOrThrowException(String mRID, long channelId) {
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration = this.getEffectiveMetrologyConfigurationOnUsagePoint(mRID);

        return this.findChannelOnUsagePointOrThrowException(effectiveMetrologyConfiguration, channelId);
    }

    public Channel findChannelOnUsagePointOrThrowException(EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration, long channelId) {

        return effectiveMetrologyConfiguration
                .getMetrologyConfiguration().getContracts().stream()
                .map(effectiveMetrologyConfiguration::getChannelsContainer)
                .flatMap(Functions.asStream())
                .flatMap(channelsContainer -> channelsContainer.getChannels().stream())
                .filter(channel -> channel.getId() == channelId)
                .findFirst().orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_CHANNEL_WITH_ID, channelId));
    }

    public IntervalReadingRecord findReadingOrThrowException(List<IntervalReadingRecord> intervalReadings) {
        return intervalReadings.stream()
                .findFirst()
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_READING_FOUND));
    }
}
