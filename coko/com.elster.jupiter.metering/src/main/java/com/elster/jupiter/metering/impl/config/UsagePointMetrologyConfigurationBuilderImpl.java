package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfigurationBuilder;
import com.elster.jupiter.orm.DataModel;

public class UsagePointMetrologyConfigurationBuilderImpl implements UsagePointMetrologyConfigurationBuilder {

    private DataModel dataModel;
    private UsagePointMetrologyConfigurationImpl underConstruction;

    public UsagePointMetrologyConfigurationBuilderImpl(DataModel dataModel) {
        this.dataModel = dataModel;
        this.underConstruction = this.dataModel.getInstance(UsagePointMetrologyConfigurationImpl.class);
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
    public UsagePointMetrologyConfiguration create() {
        this.underConstruction.create();
        return this.underConstruction;
    }
}
