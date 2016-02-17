package com.elster.insight.usagepoint.config.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.ValidationRuleSet;

import javax.inject.Inject;
import java.time.Instant;

/**
 * Provides an implementation for the {@link MetrologyConfigurationValidationRuleSetUsage} interface.
 * Todo: add javax.validation that checks that rule set is linked only once to a MetrologyConfiguration
 */
class MetrologyConfigurationValidationRuleSetUsageImpl implements MetrologyConfigurationValidationRuleSetUsage {

    enum Fields {
        METROLOGY_CONFIGURATION("metrologyConfiguration"),
        VALIDATION_RULE_SET("validationRuleSet"),
        INTERVAL("interval"),
        INTERVAL_START("interval.start"),
        INTERVAL_END("interval.end");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    private Interval interval;
    @NotEmpty(message = MessageSeeds.Constants.REQUIRED)
    private Reference<ValidationRuleSet> validationRuleSet = ValueReference.absent();
    @NotEmpty(message = MessageSeeds.Constants.REQUIRED)
    private Reference<MetrologyConfiguration> metrologyConfiguration = ValueReference.absent();

    private final DataModel dataModel;

    @Inject
    MetrologyConfigurationValidationRuleSetUsageImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    MetrologyConfigurationValidationRuleSetUsageImpl initAndSave(MetrologyConfiguration metrologyConfiguration, ValidationRuleSet validationRuleSet, Instant start) {
        this.validationRuleSet.set(validationRuleSet);
        this.metrologyConfiguration.set(metrologyConfiguration);
        this.interval = Interval.startAt(start);
        this.dataModel.persist(this);
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
    public Interval getInterval() {
        return this.interval;
    }

    @Override
    public void close(Instant closingDate) {
        if (!isEffectiveAt(closingDate)) {
            throw new IllegalArgumentException();
        }
        this.interval = this.interval.withEnd(closingDate);
        this.dataModel.update(this);
    }

}