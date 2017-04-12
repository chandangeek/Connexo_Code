/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.config;

import com.elster.jupiter.metering.AggregatedChannel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.associations.Effectivity;
import com.elster.jupiter.util.HasId;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Models the effective relationship between
 * {@link com.elster.jupiter.metering.UsagePoint} and {@link MetrologyConfiguration}
 * that is allowed to change over time.
 */
@ProviderType
public interface EffectiveMetrologyConfigurationOnUsagePoint extends Effectivity, HasId {

    UsagePointMetrologyConfiguration getMetrologyConfiguration();

    UsagePoint getUsagePoint();

    void close(Instant closingDate);

    boolean isActive();

    void activate();

    Instant getStart();

    Instant getEnd();

    Optional<ChannelsContainer> getChannelsContainer(MetrologyContract metrologyContract);

    Optional<ChannelsContainer> getChannelsContainer(MetrologyContract metrologyContract, Instant when);
    
    Optional<AggregatedChannel> getAggregatedChannel(MetrologyContract metrologyContract, ReadingType readingType);

    void activateOptionalMetrologyContract(MetrologyContract metrologyContract, Instant when);

    void deactivateOptionalMetrologyContract(MetrologyContract metrologyContract, Instant when);

    List<ReadingTypeRequirement> getReadingTypeRequirements();
}