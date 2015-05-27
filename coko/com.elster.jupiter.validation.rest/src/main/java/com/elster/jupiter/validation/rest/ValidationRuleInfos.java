package com.elster.jupiter.validation.rest;

import com.elster.jupiter.validation.ValidationRule;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class ValidationRuleInfos {

    public int total;
    public List<ValidationRuleInfo> rules = new ArrayList<ValidationRuleInfo>();

    public ValidationRuleInfos() {
    }

    public ValidationRuleInfos(ValidationRule validationRule) {
        add(validationRule);
    }

    public ValidationRuleInfo add(ValidationRule validationRule) {
        ValidationRuleInfo result = new ValidationRuleInfo(validationRule);
        rules.add(result);
        total++;
        return result;
    }

    public void addAll(Iterable<? extends ValidationRule> validationRules) {
        for (ValidationRule each : validationRules) {
            add(each);
        }
    }
}
