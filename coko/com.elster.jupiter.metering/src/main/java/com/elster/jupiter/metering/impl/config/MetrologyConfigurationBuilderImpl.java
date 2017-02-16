/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationBuilder;
import com.elster.jupiter.orm.DataModel;

public class MetrologyConfigurationBuilderImpl implements MetrologyConfigurationBuilder {

    private DataModel dataModel;
    private MetrologyConfigurationImpl underConstruction;

    public MetrologyConfigurationBuilderImpl(DataModel dataModel) {
        this.dataModel = dataModel;
        this.underConstruction = this.dataModel.getInstance(MetrologyConfigurationImpl.class);
    }

    void init(String metrologyConfigurationName, ServiceCategory serviceCategory) {
        this.underConstruction.setName(metrologyConfigurationName);
        this.underConstruction.setServiceCategory(serviceCategory);
    }

    @Override
    public MetrologyConfigurationBuilder withDescription(String description) {
        this.underConstruction.setDescription(description);
        return this;
    }

    @Override
    public MetrologyConfiguration create() {
        this.underConstruction.create();
        return this.underConstruction;
    }
}
