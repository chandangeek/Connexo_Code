/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.calendar.EventSet;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfigurationBuilder;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.search.SearchablePropertyValue;

import java.util.ArrayList;
import java.util.List;

class UsagePointMetrologyConfigurationBuilderImpl implements UsagePointMetrologyConfigurationBuilder {

    private final UsagePointMetrologyConfigurationImpl underConstruction;
    private final List<EventSet> eventSets = new ArrayList<>();
    private final List<RegisteredCustomPropertySet> customPropertySets = new ArrayList<>();
    private final List<SearchablePropertyValue.ValueBean> requirementSpecs = new ArrayList<>();

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
    public UsagePointMetrologyConfigurationBuilder withEventSet(EventSet eventSet) {
        this.eventSets.add(eventSet);
        return this;
    }

    @Override
    public UsagePointMetrologyConfigurationBuilder withCustomPropertySet(RegisteredCustomPropertySet customPropertySet) {
        this.customPropertySets.add(customPropertySet);
        return this;
    }

    @Override
    public UsagePointMetrologyConfigurationBuilder withUsagePointRequirement(SearchablePropertyValue.ValueBean requirementSpecs) {
        this.requirementSpecs.add(requirementSpecs);
        return this;
    }

    @Override
    public UsagePointMetrologyConfiguration create() {
        this.eventSets.forEach(this.underConstruction::addEventSet);
        this.customPropertySets.forEach(this.underConstruction::addCustomPropertySet);
        this.requirementSpecs.forEach(this.underConstruction::addUsagePointRequirement);
        this.underConstruction.create();
        return this.underConstruction;
    }

}