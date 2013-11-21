package com.elster.jupiter.validation.rest.impl;

import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationRule;

public class ValidationRuleInfo {

    public long id;
    public boolean active;
    public ValidationAction action;
    public String implementation; //validator name
    public int position;

    public ValidationRuleInfo(ValidationRule validationRule) {
        id = validationRule.getId();
        active = validationRule.isActive();
        action = validationRule.getAction();
        implementation = validationRule.getImplementation();
    }

    public ValidationRuleInfo() {
    }
}
