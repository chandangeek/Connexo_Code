package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.orm.associations.Effectivity;

import java.time.Instant;

/**
 * Models the effective relationship between
 * {@link com.elster.jupiter.metering.UsagePoint} and {@link MetrologyConfiguration}
 * that is allowed to change over time.
 */
public interface EffectiveMetrologyConfigurationOnUsagePoint extends Effectivity {

    MetrologyConfiguration getMetrologyConfiguration();

    UsagePointMetrologyConfiguration getUsagePointMetrologyConfiguration();

    void close(Instant closingDate);

}