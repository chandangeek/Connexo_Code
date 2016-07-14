package com.elster.jupiter.metering.config;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.impl.config.EffectiveMetrologyContractOnUsagePoint;
import com.elster.jupiter.orm.associations.Effectivity;

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
public interface EffectiveMetrologyConfigurationOnUsagePoint extends Effectivity {

    UsagePointMetrologyConfiguration getMetrologyConfiguration();

    UsagePoint getUsagePoint();

    void close(Instant closingDate);

    boolean isActive();

    void activate();

    Optional<EffectiveMetrologyContractOnUsagePoint> getEffectiveContract(MetrologyContract metrologyContract);

    List<EffectiveMetrologyContractOnUsagePoint> getEffectiveContracts();
}
