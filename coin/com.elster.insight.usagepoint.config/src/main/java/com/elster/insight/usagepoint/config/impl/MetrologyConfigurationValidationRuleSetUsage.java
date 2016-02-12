package com.elster.insight.usagepoint.config.impl;

import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.jupiter.validation.ValidationRuleSet;

public interface MetrologyConfigurationValidationRuleSetUsage {

    MetrologyConfiguration getMetrologyConfiguration();

    ValidationRuleSet getValidationRuleSet();

    void delete();
}