package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.rest.util.ExceptionFactory;

import javax.inject.Inject;

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

    public Channel findChannelOnUsagePointOrThrowException(String mRID, long channelId) {
        UsagePoint usagePoint = this.fetchUsagePoint(mRID);
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration = usagePoint.getEffectiveMetrologyConfiguration().orElse(null);

        return effectiveMetrologyConfiguration
                .getMetrologyConfiguration()
                .getContracts().stream().flatMap(metrologyContract -> {
                    return effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract).orElse(null).getChannels().stream().filter(channel -> channel.getId() == channelId);
                }).findFirst().orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_CHANNEL_WITH_ID, channelId));
    }
}
