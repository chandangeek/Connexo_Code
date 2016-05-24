package com.elster.jupiter.metering.config;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface UsagePointMetrologyConfigurationBuilder {

    UsagePointMetrologyConfigurationBuilder withDescription(String description);

    UsagePointMetrologyConfiguration create();

}
