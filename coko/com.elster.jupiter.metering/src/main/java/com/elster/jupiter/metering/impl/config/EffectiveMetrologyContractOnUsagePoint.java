package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.orm.associations.Effectivity;
import com.elster.jupiter.util.HasId;

/**
 * Models the effective relationship between
 * {@link UsagePoint} and {@link MetrologyContract} mediated with {@link EffectiveMetrologyConfigurationOnUsagePoint}.
 */
public interface EffectiveMetrologyContractOnUsagePoint extends HasId, Effectivity {
    EffectiveMetrologyConfigurationOnUsagePoint getMetrologyConfigurationOnUsagePoint();

    MetrologyContract getMetrologyContract();

    ChannelsContainer getChannelsContainer();
}
