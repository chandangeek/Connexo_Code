package com.elster.jupiter.validation.rest.impl;

import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleProperties;

import java.util.ArrayList;
import java.util.List;

public class ValidationRuleInfo {

    public long id;
    public boolean active;
    public ValidationAction action;
    public String implementation; //validator name
    public int position;
    public List<ValidationRulePropertyInfo> properties = new ArrayList<ValidationRulePropertyInfo>();

    public ValidationRuleInfo(ValidationRule validationRule) {
        id = validationRule.getId();
        active = validationRule.isActive();
        action = validationRule.getAction();
        implementation = validationRule.getImplementation();
        for (ValidationRuleProperties property : validationRule.getProperties()) {
            properties.add(new ValidationRulePropertyInfo(property));
        }
    }

    public ValidationRuleInfo() {
    }
}
