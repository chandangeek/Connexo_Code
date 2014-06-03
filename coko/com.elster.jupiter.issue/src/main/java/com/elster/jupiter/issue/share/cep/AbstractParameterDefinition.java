package com.elster.jupiter.issue.share.cep;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class AbstractParameterDefinition implements ParameterDefinition {
    public boolean isDependent() {
        return false;
    }

    @Override
    public String getSuffix() {
        return "";
    }

    @Override
    public Object getDefaultValue() {
        return null;
    }

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public List<Object> getDefaultValues() {
        return Collections.emptyList();
    }

    @Override
    public ParameterDefinition getValue(Map<String, Object> parameters) {
        return this;
    }

    @Override
    public List<ParameterViolation> validate(String value, ParameterDefinitionContext context) {
        if (getConstraint() != null){
            return getConstraint().validate(value, context.wrapKey(getKey()));
        }
        return Collections.emptyList();
    }
}
