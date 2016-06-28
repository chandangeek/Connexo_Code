package com.elster.jupiter.metering.config;

//import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.associations.Effectivity;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.time.Interval;

import java.time.Instant;

/**
 * Models the effective relationship between
 * {@link com.elster.jupiter.metering.UsagePoint} and {@link MetrologyConfiguration}
 * that is allowed to change over time.
 */
public interface EffectiveMetrologyConfigurationOnUsagePoint extends HasId, Effectivity {

    UsagePointMetrologyConfiguration getMetrologyConfiguration();

    void close(Instant closingDate);

    boolean isActive();

    void activate();

    void update(UsagePointMetrologyConfiguration metrologyConfiguration, Interval interval);

    Instant getStart();

    Instant getEnd();
}