package com.elster.jupiter.validation;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface ValidationRuleProperties {
    String getName();

    String getDisplayName();

    Object getValue();

    void setValue(Object value);

    ValidationRule getRule();
}
