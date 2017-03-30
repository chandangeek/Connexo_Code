/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.orm.associations.Effectivity;
import com.elster.jupiter.util.HasId;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Provides functionality of storing a group of channels related to some configuration currently activated
 * on meter or usage point. Two main implementations are present for different channels type:
 * meter-related (input) channels grouped by common {@link MeterActivation}, and usage point related
 * (output) channels grouped by common {@link MetrologyContract}. Specific extension of this interface
 * {@link MetrologyContractChannelsContainer} is provided for the latter case.
 */
@ProviderType
public interface ChannelsContainer extends ReadingContainer, Effectivity, HasId {

    Channel createChannel(ReadingType main, ReadingType... readingTypes);

    List<Channel> getChannels();

    default Optional<Channel> getChannel(ReadingType readingType) {
        return getChannels().stream().filter(channel -> channel.hasReadingType(readingType)).findFirst();
    }

    // Is this a good idea ?
    Instant getStart();


    Optional<Meter> getMeter();

    Optional<UsagePoint> getUsagePoint();

    Optional<BigDecimal> getMultiplier(MultiplierType type);

    /**
     * Finds aggregated {@link Channel Channels} dependent on the provided scope channel data and returns them
     * mapped with {@link Range Ranges} of time where data from the provided channels is actually used to calculate aggregated data.
     * @param scope The scope of channel data defined by {@link Channel}/{@link Range} of time.
     * The behavior is not guaranteed if these channels do not belong to this channels container.
     * @return Dependent {@link Channel Channels} mapped to {@link Range Ranges} of time
     * where data from the provided channels is actually used to calculate aggregated data.
     */
    Map<Channel, Range<Instant>> findDependentChannelScope(Map<Channel, Range<Instant>> scope);
}
