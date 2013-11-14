package com.elster.jupiter.validation;

public interface ValidationRule {
    long getId();

    boolean isActive();

    ValidationAction getAction();

    String getImplementation();

    void activate();

    void deactivate();

    ValidationRuleSet getRuleSet();

    Validator getValidator();

}
