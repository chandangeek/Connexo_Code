package com.elster.jupiter.validation.rest;

import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.rest.ValidationRuleInfo;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class ValidationRuleInfos {

    public int total;
    public List<ValidationRuleInfo> rules = new ArrayList<ValidationRuleInfo>();

    ValidationRuleInfos() {
    }

    ValidationRuleInfos(ValidationRule validationRule) {
        add(validationRule);
    }

    ValidationRuleInfo add(ValidationRule validationRule) {
        ValidationRuleInfo result = new ValidationRuleInfo(validationRule);
        rules.add(result);
        total++;
        return result;
    }

    void addAll(Iterable<? extends ValidationRule> validationRules) {
        for (ValidationRule each : validationRules) {
            add(each);
        }
    }
}
