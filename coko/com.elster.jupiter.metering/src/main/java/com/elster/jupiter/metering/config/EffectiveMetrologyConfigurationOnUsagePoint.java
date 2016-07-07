package com.elster.jupiter.metering.config;

import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.associations.Effectivity;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.Optional;

/**
 * Models the effective relationship between
 * {@link com.elster.jupiter.metering.UsagePoint} and {@link MetrologyConfiguration}
 * that is allowed to change over time.
 */
@ProviderType
public interface EffectiveMetrologyConfigurationOnUsagePoint extends Effectivity {

    UsagePointMetrologyConfiguration getMetrologyConfiguration();

    UsagePoint getUsagePoint();

    void close(Instant closingDate);

    boolean isActive();

    void activate();

    Optional<ChannelsContainer> getChannelsContainer(MetrologyContract metrologyContract);
}