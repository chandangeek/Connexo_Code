package com.elster.jupiter.validation.rest;

import java.util.ArrayList;
import java.util.List;

public class ValidationRulePropertySpecInfos {

    public int total;
    public List<ValidationRulePropertySpecInfo> propertySpecs = new ArrayList<ValidationRulePropertySpecInfo>();

    ValidationRulePropertySpecInfos() {
    }

    ValidationRulePropertySpecInfos(String propertyName, String key, boolean optional, String validator) {
        add(propertyName, key, optional, validator);
    }

    ValidationRulePropertySpecInfo add(String propertyName, String key, boolean optional, String validator) {
        ValidationRulePropertySpecInfo result = new ValidationRulePropertySpecInfo(propertyName, key, optional, validator);
        propertySpecs.add(result);
        total++;
        return result;
    }

}
