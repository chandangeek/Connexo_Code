package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.config.UPMetrologyConfiguration;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfigurationBuilder;
import com.elster.jupiter.orm.DataModel;

public class UsagePointMetrologyConfigurationBuilderImpl implements UsagePointMetrologyConfigurationBuilder {

    private DataModel dataModel;
    private UPMetrologyConfigurationImpl underConstruction;

    public UsagePointMetrologyConfigurationBuilderImpl(DataModel dataModel) {
        this.dataModel = dataModel;
        this.underConstruction = this.dataModel.getInstance(UPMetrologyConfigurationImpl.class);
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
    public UPMetrologyConfiguration create() {
        this.underConstruction.create();
        return this.underConstruction;
    }
}
