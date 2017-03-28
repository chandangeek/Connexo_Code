/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.DeliverableType;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.util.streams.Functions;

import javax.inject.Inject;
import java.util.Optional;
import java.util.function.Predicate;

public class ResourceHelper {
    private final MeteringService meteringService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public ResourceHelper(MeteringService meteringService, ExceptionFactory exceptionFactory) {
        this.meteringService = meteringService;
        this.exceptionFactory = exceptionFactory;
    }

    UsagePoint findUsagePointOrThrowException(String name) {
        return meteringService.findUsagePointByName(name)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_USAGE_POINT_WITH_NAME, name));
    }

    MetrologyContract findMetrologyContractOfChannelOrThrowException(EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfigurationOnUsagePoint, long channelId) {
        for (MetrologyContract metrologyContract : effectiveMetrologyConfigurationOnUsagePoint.getMetrologyConfiguration()
                .getContracts()) {
            Optional<ChannelsContainer> container = effectiveMetrologyConfigurationOnUsagePoint.getChannelsContainer(metrologyContract);
            if (container.isPresent()) {
                Optional<Channel> foundChannel = container.get()
                        .getChannels()
                        .stream()
                        .filter(channel -> channel.getId() == channelId)
                        .findAny();
                if (foundChannel.isPresent()) {
                    return metrologyContract;
                }
            }
        }
        throw exceptionFactory.newException(MessageSeeds.NO_SUCH_CHANNEL_FOR_USAGE_POINT,
                effectiveMetrologyConfigurationOnUsagePoint.getUsagePoint().getName(), channelId);
    }

    Channel findChannelOnUsagePointOrThrowException(EffectiveMetrologyConfigurationOnUsagePoint
                                                            effectiveMetrologyConfiguration, long channelId,
                                                    UsagePointChannelRepresentationType usagePointChannelType) {
        return effectiveMetrologyConfiguration.getMetrologyConfiguration().getContracts().stream()
                .map(effectiveMetrologyConfiguration::getChannelsContainer)
                .flatMap(Functions.asStream())
                .flatMap(channelsContainer -> channelsContainer.getChannels().stream())
                .filter(usagePointChannelType.getFilterPredicate())
                .filter(channel -> channel.getId() == channelId)
                .findAny()
                .orElseThrow(() -> exceptionFactory.newException(usagePointChannelType.getNoSuchElementMessageSeed(),
                        effectiveMetrologyConfiguration.getUsagePoint().getName(), channelId));
    }

    DeliverableType identifyDeliverableType(EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration, Channel channel, UsagePointChannelRepresentationType usagePointChannelType) {

        ReadingType readingType = channel.getMainReadingType();
        long channelId = channel.getId();

        return effectiveMetrologyConfiguration.getMetrologyConfiguration().getContracts().stream()
                .filter(metrologyContract -> doesChannelBelongContract(metrologyContract, effectiveMetrologyConfiguration, channelId, usagePointChannelType.getFilterPredicate()))
                .map(metrologyContract -> findDeliverableType(metrologyContract, readingType))
                .findFirst().orElse(null);
    }

    boolean doesChannelBelongContract(MetrologyContract metrologyContract, EffectiveMetrologyConfigurationOnUsagePoint
            effectiveMetrologyConfiguration, long channelId, Predicate<Channel> channelFilterPredicate){
        Optional<ChannelsContainer> channelsContainer = effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract);
        return doesChannelBelongContainer(channelsContainer.get(), channelId, channelFilterPredicate);

    }

    boolean doesChannelBelongContainer(ChannelsContainer channelsContainer, long channelId, Predicate<Channel> channelFilterPredicate) {
        return channelsContainer.getChannels().stream()
                .filter(channelFilterPredicate)
                .anyMatch(channel -> channel.getId() == channelId);
    }

    DeliverableType findDeliverableType(MetrologyContract contract, ReadingType readingType){
        return contract.getDeliverables().stream()
                .filter(readingTypeDeliverable -> readingTypeDeliverable.getReadingType().equals(readingType))
                .findFirst()
                .orElse(null)
                .getType();
    }

}
