package com.elster.jupiter.issue.share.cep;

import java.util.List;

public interface ParameterConstraint {
    boolean isOptional();
    String getRegexp();
    Integer getMin();
    Integer getMax();

    List<ParameterViolation> validate(String value, String paramKey);
}
