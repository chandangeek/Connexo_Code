package com.elster.insight.usagepoint.config;

import java.time.Instant;
import java.util.List;

import com.elster.jupiter.validation.ValidationRuleSet;

public interface MetrologyConfiguration {
    long getId();

    String getName();

    void setName(String name);

    List<ValidationRuleSet> getValidationRuleSets();

    void update();

    void delete();

    long getVersion();

    Instant getCreateTime();

    Instant getModTime();

    String getUserName();
}
