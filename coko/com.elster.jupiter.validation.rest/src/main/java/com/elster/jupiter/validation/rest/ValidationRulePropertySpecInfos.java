package com.elster.jupiter.validation.rest;

import com.elster.jupiter.validation.rest.ValidationRulePropertySpecInfo;

import java.util.ArrayList;
import java.util.List;

public class ValidationRulePropertySpecInfos {

    public int total;
    public List<ValidationRulePropertySpecInfo> propertySpecs = new ArrayList<ValidationRulePropertySpecInfo>();

    ValidationRulePropertySpecInfos() {
    }

    ValidationRulePropertySpecInfos(String propertyName, boolean optional, String validator) {
        add(propertyName, optional, validator);
    }

    ValidationRulePropertySpecInfo add(String propertyName, boolean optional, String validator) {
        ValidationRulePropertySpecInfo result = new ValidationRulePropertySpecInfo(propertyName, optional, validator);
        propertySpecs.add(result);
        total++;
        return result;
    }

}
