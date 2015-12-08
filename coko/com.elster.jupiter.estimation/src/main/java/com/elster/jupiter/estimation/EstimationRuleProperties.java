package com.elster.jupiter.estimation;

public interface EstimationRuleProperties {
    String getName();

    String getDisplayName();

    String getDescription();

    Object getValue();

    void setValue(Object value);

    EstimationRule getRule();
}
