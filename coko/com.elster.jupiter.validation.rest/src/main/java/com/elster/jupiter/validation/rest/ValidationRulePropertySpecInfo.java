package com.elster.jupiter.validation.rest;

public class ValidationRulePropertySpecInfo {

    public String key;
    public String name;
    public boolean optional;
    public String validator;

    public ValidationRulePropertySpecInfo(String name, String key, boolean optional, String validator) {
        this.key = key;
        this.name = name;
        this.optional = optional;
        this.validator = validator;
    }

    public ValidationRulePropertySpecInfo() {
    }
}
