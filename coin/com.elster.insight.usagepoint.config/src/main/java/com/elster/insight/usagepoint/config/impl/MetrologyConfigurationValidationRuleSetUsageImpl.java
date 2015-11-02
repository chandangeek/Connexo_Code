package com.elster.insight.usagepoint.config.impl;

import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;

import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.insight.usagepoint.config.MetrologyConfigurationValidationRuleSetUsage;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;

public class MetrologyConfigurationValidationRuleSetUsageImpl implements MetrologyConfigurationValidationRuleSetUsage {
    private Reference<ValidationRuleSet> validationRuleSet = ValueReference.absent();
    private Reference<MetrologyConfiguration> metrologyConfiguration = ValueReference.absent();

    private final DataModel dataModel;
    private final EventService eventService;
    private final ValidationService validationService;

    @Inject
    MetrologyConfigurationValidationRuleSetUsageImpl(DataModel dataModel, EventService eventService, ValidationService validationService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.validationService = validationService;
    }

    MetrologyConfigurationValidationRuleSetUsageImpl init(MetrologyConfiguration deviceConfiguration, ValidationRuleSet validationRuleSet) {
        this.validationRuleSet.set(Objects.requireNonNull(validationRuleSet));
        this.metrologyConfiguration.set(Objects.requireNonNull(deviceConfiguration));
        return this;
    }

    @Override
    public ValidationRuleSet getValidationRuleSet() {
        return validationRuleSet.get();
    }

    @Override
    public MetrologyConfiguration getMetrologyConfiguration() {
        return metrologyConfiguration.get();
    }

    @Override
    public void delete() {
        dataModel.remove(this);
    }
}
