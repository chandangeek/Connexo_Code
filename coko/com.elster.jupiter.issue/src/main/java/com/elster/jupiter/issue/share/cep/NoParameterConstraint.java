package com.elster.jupiter.issue.share.cep;

import java.util.Collections;
import java.util.List;

public class NoParameterConstraint implements ParameterConstraint {
    private boolean isOptional;

    public NoParameterConstraint() {
        this(true);
    }

    public NoParameterConstraint(boolean isOptional) {
        this.isOptional = isOptional;
    }

    @Override
    public boolean isOptional() {
        return true;
    }

    @Override
    public String getRegexp() {
        return null;
    }

    @Override
    public Integer getMin() {
        return null;
    }

    @Override
    public Integer getMax() {
        return null;
    }

    @Override
    public List<ParameterViolation> validate(String value, String paramKey) {
        return Collections.emptyList();
    }
}
