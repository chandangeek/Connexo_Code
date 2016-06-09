package com.elster.jupiter.metering.config;

import com.elster.jupiter.orm.associations.Effectivity;

import java.time.Instant;

/**
 * Models the effective relationship between
 * {@link com.elster.jupiter.metering.UsagePoint} and {@link MetrologyConfiguration}
 * that is allowed to change over time.
 */
public interface EffectiveMetrologyConfigurationOnUsagePoint extends Effectivity {

    UsagePointMetrologyConfiguration getMetrologyConfiguration();

    void close(Instant closingDate);

    boolean isActive();

    void activate();

    Instant getStart();

    Instant getEnd();
}