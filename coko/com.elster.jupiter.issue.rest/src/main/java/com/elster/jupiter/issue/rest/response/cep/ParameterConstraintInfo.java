package com.elster.jupiter.issue.rest.response.cep;

import com.elster.jupiter.issue.share.cep.ParameterConstraint;

public class ParameterConstraintInfo {
    private boolean required;
    private Integer min;
    private Integer max;
    private String regexp;

    public ParameterConstraintInfo(ParameterConstraint constraint) {
        required = !constraint.isOptional();
        min = constraint.getMin();
        max = constraint.getMax();
        regexp = constraint.getRegexp();
    }

    public boolean isRequired() {
        return required;
    }

    public Integer getMin() {
        return min;
    }

    public Integer getMax() {
        return max;
    }

    public String getRegexp() {
        return regexp;
    }
}
