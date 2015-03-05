package com.elster.jupiter.estimation;

public interface EstimationRuleProperties {
    String getName();

    String getDisplayName();

    Object getValue();

    void setValue(Object value);

    EstimationRule getRule();
}
