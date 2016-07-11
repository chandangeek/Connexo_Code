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