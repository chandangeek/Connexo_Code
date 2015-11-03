package com.elster.insight.usagepoint.config;

import java.time.Instant;
import java.util.List;

import aQute.bnd.annotation.ProviderType;

import com.elster.jupiter.validation.ValidationRuleSet;

@ProviderType
public interface MetrologyConfiguration {
    long getId();

    String getName();

    void updateName(String name);

    MetrologyConfigurationValidationRuleSetUsage addValidationRuleSet(ValidationRuleSet validationRuleSet);
    List<ValidationRuleSet> getValidationRuleSets();
    void removeValidationRuleSet(ValidationRuleSet validationRuleSet);

    void delete();

    long getVersion();

    Instant getCreateTime();

    Instant getModTime();

    String getUserName();
}
