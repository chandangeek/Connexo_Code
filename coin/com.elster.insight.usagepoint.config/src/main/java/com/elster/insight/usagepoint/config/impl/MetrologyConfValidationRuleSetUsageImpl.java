package com.elster.insight.usagepoint.config.impl;

import java.util.Optional;

import javax.inject.Inject;

import com.elster.insight.usagepoint.config.MetrologyConfValidationRuleSetUsage;
import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;

public class MetrologyConfValidationRuleSetUsageImpl implements MetrologyConfValidationRuleSetUsage {

    private long validationRuleSetId;
    private long metrologyConfigurationId;

    private transient ValidationRuleSet validationRuleSet;
    private transient MetrologyConfiguration metrologyConfiguration;

    private final DataModel dataModel;
    private final EventService eventService;
    private final ValidationService validationService;

    @Inject
    MetrologyConfValidationRuleSetUsageImpl(DataModel dataModel, EventService eventService, ValidationService validationService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.validationService = validationService;
    }

    MetrologyConfValidationRuleSetUsageImpl init(MetrologyConfiguration deviceConfiguration, ValidationRuleSet validationRuleSet) {
        this.validationRuleSet = validationRuleSet;
        this.metrologyConfiguration = deviceConfiguration;
        this.validationRuleSetId = validationRuleSet.getId();
        this.metrologyConfigurationId = deviceConfiguration.getId();
        return this;
    }

    @Override
    public ValidationRuleSet getValidationRuleSet() {
        if (validationRuleSet == null) {
            Optional<? extends ValidationRuleSet> optional = validationService.getValidationRuleSet(validationRuleSetId);
            if (optional.isPresent()) {
                validationRuleSet = (ValidationRuleSet) optional.get();
            }
        }
        return validationRuleSet;
    }

    @Override
    public MetrologyConfiguration getMetrologyConfiguration() {
        if (metrologyConfiguration == null) {
            metrologyConfiguration = dataModel.mapper(MetrologyConfiguration.class).getOptional(metrologyConfigurationId).get();
        }
        return metrologyConfiguration;
    }

    @Override
    public void update() {
        dataModel.persist(this);
    }

    @Override
    public void delete() {
        dataModel.remove(this);
    }

    void setValidationRuleSetIdRuleSetId(long validationRuleSetId) {
        this.validationRuleSetId = validationRuleSetId;
    }

    void setDeviceConfigurationId(long deviceConfigurationId) {
        this.metrologyConfigurationId = deviceConfigurationId;
    }
}
