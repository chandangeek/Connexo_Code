/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfigurationBuilder;
import com.elster.jupiter.orm.DataModel;

class UsagePointMetrologyConfigurationBuilderImpl implements UsagePointMetrologyConfigurationBuilder {

    private UsagePointMetrologyConfigurationImpl underConstruction;

    UsagePointMetrologyConfigurationBuilderImpl(DataModel dataModel) {
        this.underConstruction = dataModel.getInstance(UsagePointMetrologyConfigurationImpl.class);
    }

    void init(String metrologyConfigurationName, ServiceCategory serviceCategory) {
        this.underConstruction.setName(metrologyConfigurationName);
        this.underConstruction.setServiceCategory(serviceCategory);
    }

    @Override
    public UsagePointMetrologyConfigurationBuilder withDescription(String description) {
        this.underConstruction.setDescription(description);
        return this;
    }

    @Override
    public UsagePointMetrologyConfigurationBuilder withGapAllowed(boolean isGapAllowed) {
        this.underConstruction.setGapAllowed(isGapAllowed);
        return this;
    }

    @Override
    public UsagePointMetrologyConfiguration create() {
        this.underConstruction.create();
        return this.underConstruction;
    }
}
