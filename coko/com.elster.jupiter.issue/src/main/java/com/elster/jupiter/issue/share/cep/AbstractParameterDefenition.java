package com.elster.jupiter.issue.share.cep;

import java.util.List;
import java.util.Map;

public abstract class AbstractParameterDefenition implements ParameterDefinition {
    public boolean isDependant() {
        return false;
    }

    @Override
    public String getSuffix() {
        return null;
    }

    @Override
    public String getDefaultValue() {
        return null;
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public List<String> getDefaultValues() {
        return null;
    }

    @Override
    public ParameterDefinition getValue(Map<String, Object> parameters) {
        return this;
    }

    @Override
    public List<ParameterViolation> validate(String value, ParameterDefinitionContext context) {
        return getConstraint().validate(value, context.wrapKey(getKey()));
    }
}
