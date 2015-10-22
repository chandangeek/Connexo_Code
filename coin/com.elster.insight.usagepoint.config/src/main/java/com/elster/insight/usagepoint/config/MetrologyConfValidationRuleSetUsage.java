package com.elster.insight.usagepoint.config;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.validation.ValidationRuleSet;

@ProviderType
public interface MetrologyConfValidationRuleSetUsage {

    MetrologyConfiguration getMetrologyConfiguration();

    ValidationRuleSet getValidationRuleSet();

    void delete();

    void update();

}