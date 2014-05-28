package com.elster.jupiter.issue.rest.response.cep;

import com.elster.jupiter.issue.share.cep.ParameterConstraint;

public class ParameterConstraintInfo {
    boolean isOptional;
    Integer min;
    Integer max;

    public ParameterConstraintInfo(ParameterConstraint constraint) {
        isOptional = constraint.isOptional();
        min = constraint.getMin();
        max = constraint.getMax();
    }
    public Integer getMax() {
        return max;
    }

    public Integer getMin() {
        return min;
    }

    public boolean isOptional() {
        return isOptional;
    }




}
