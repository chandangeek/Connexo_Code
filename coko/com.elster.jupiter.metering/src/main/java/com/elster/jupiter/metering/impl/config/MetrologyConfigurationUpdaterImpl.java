/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationUpdater;

public class MetrologyConfigurationUpdaterImpl implements MetrologyConfigurationUpdater {

    private MetrologyConfigurationImpl underConstruction;

    public MetrologyConfigurationUpdaterImpl(MetrologyConfigurationImpl metrologyConfiguration) {
        this.underConstruction = metrologyConfiguration;
    }

    @Override
    public MetrologyConfigurationUpdater setName(String name) {
        this.underConstruction.setName(name);
        return this;
    }

    @Override
    public MetrologyConfigurationUpdater setDescription(String description) {
        this.underConstruction.setDescription(description);
        return this;
    }

    @Override
    public MetrologyConfigurationUpdater setServiceCategory(ServiceCategory serviceCategory) {
        this.underConstruction.setServiceCategory(serviceCategory);
        return this;
    }

    @Override
    public MetrologyConfiguration complete() {
        this.underConstruction.update();
        return this.underConstruction;
    }
}
