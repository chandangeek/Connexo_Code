/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.config;

import com.elster.jupiter.metering.ServiceCategory;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface MetrologyConfigurationUpdater {
    MetrologyConfigurationUpdater setName(String name);

    MetrologyConfigurationUpdater setDescription(String description);

    MetrologyConfigurationUpdater setServiceCategory(ServiceCategory serviceCategory);

    MetrologyConfiguration complete();
}