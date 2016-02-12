package com.elster.insight.usagepoint.config;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.metering.UsagePoint;

@ProviderType
public interface UsagePointMetrologyConfiguration {
    UsagePoint getUsagePoint();

    MetrologyConfiguration getMetrologyConfiguration();

    void updateMetrologyConfiguration(MetrologyConfiguration mc);

    void delete();

    long getId();

    long getVersion();
}
