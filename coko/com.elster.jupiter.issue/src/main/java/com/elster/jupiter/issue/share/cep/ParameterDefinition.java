package com.elster.jupiter.issue.share.cep;

import java.util.List;
import java.util.Map;

public interface ParameterDefinition {
    String getKey();
    ParameterControl getControl();
    ParameterConstraint getConstraint();
    boolean isDependant();

    String getLabel();
    String getSuffix();
    String getDefaultValue();
    String getHelp();
    List<String> getDefaultValues();


    ParameterDefinition getValue(Map<String, Object> parameters);

    List<ParameterViolation> validate(String value, ParameterDefinitionContext context);
}
