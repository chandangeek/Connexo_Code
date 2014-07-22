package com.elster.jupiter.issue.share.cep;

import java.util.List;
import java.util.Map;

public interface ParameterDefinition {
    String getKey();
    ParameterControl getControl();
    ParameterConstraint getConstraint();
    List<String> getDependOn();

    String getLabel();
    String getSuffix();
    Object getDefaultValue();
    String getHelp();
    List<Object> getDefaultValues();


    ParameterDefinition getValue(Map<String, Object> parameters);

    List<ParameterViolation> validate(String value, ParameterDefinitionContext context);
}
