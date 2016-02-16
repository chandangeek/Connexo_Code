package com.elster.insight.usagepoint.config.impl;

import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.orm.associations.Effectivity;
import com.elster.jupiter.validation.ValidationRuleSet;

import java.time.Instant;

/**
 * Models the link between {@link MetrologyConfiguration} and {@link ValidationRuleSet}.
 */
public interface MetrologyConfigurationValidationRuleSetUsage extends Effectivity {

    MetrologyConfiguration getMetrologyConfiguration();

    ValidationRuleSet getValidationRuleSet();

    void close(Instant when);

}