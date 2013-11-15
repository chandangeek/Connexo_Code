package com.elster.jupiter.validation;

import java.util.List;

public interface ValidationRule {
    long getId();

    boolean isActive();

    ValidationAction getAction();

    String getImplementation();

    void activate();

    void deactivate();

    ValidationRuleSet getRuleSet();

    Validator getValidator();

    void setAction(ValidationAction action);

    void setImplementation(String implementation);

    void setPosition(int position);

    List<ValidationRuleProperties> getProperties();

    ValidationRuleProperties addProperty(String name, long value);

    void deleteProperty(ValidationRuleProperties property);
}
