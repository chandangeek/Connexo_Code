package com.elster.jupiter.validation.rest.impl;

public class ValidationRulePropertySpecInfo {

    public String name;
    public boolean optional;
    public String validator;

    public ValidationRulePropertySpecInfo(String name, boolean optional, String validator) {
        this.name = name;
        this.optional = optional;
        this.validator = validator;
    }

    public ValidationRulePropertySpecInfo() {
    }
}
