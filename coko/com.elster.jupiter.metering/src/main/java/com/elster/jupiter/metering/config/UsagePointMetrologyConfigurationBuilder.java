/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.config;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface UsagePointMetrologyConfigurationBuilder {

    UsagePointMetrologyConfigurationBuilder withDescription(String description);

    UsagePointMetrologyConfiguration create();

}
