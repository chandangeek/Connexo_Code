/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.calendar.EventSet;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationBuilder;
import com.elster.jupiter.orm.DataModel;

import java.util.ArrayList;
import java.util.List;

public class MetrologyConfigurationBuilderImpl implements MetrologyConfigurationBuilder {

    private final MetrologyConfigurationImpl underConstruction;
    private final List<EventSet> eventSets = new ArrayList<>();
    private final List<RegisteredCustomPropertySet> customPropertySets = new ArrayList<>();

    public MetrologyConfigurationBuilderImpl(DataModel dataModel) {
        this.underConstruction = dataModel.getInstance(MetrologyConfigurationImpl.class);
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
    public MetrologyConfigurationBuilder withEventSet(EventSet eventSet) {
        this.eventSets.add(eventSet);
        return this;
    }

    @Override
    public MetrologyConfigurationBuilder withCustomPropertySet(RegisteredCustomPropertySet customPropertySet) {
        this.customPropertySets.add(customPropertySet);
        return this;
    }

    @Override
    public MetrologyConfiguration create() {
        this.eventSets.forEach(this.underConstruction::addEventSet);
        this.customPropertySets.forEach(this.underConstruction::addCustomPropertySet);
        this.underConstruction.create();
        return this.underConstruction;
    }

}