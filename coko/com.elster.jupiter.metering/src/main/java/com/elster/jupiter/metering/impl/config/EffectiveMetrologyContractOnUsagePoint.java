package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.orm.associations.Effectivity;
import com.elster.jupiter.util.HasId;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

/**
 * Models the effective relationship between
 * {@link UsagePoint} and {@link MetrologyContract} mediated with {@link EffectiveMetrologyConfigurationOnUsagePoint}.
 */
@ProviderType
public interface EffectiveMetrologyContractOnUsagePoint extends HasId, Effectivity {
    EffectiveMetrologyConfigurationOnUsagePoint getMetrologyConfigurationOnUsagePoint();

    MetrologyContract getMetrologyContract();

    ChannelsContainer getChannelsContainer();

    void close(Instant closingDate);
}