package com.elster.jupiter.validation.rest;

import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@XmlRootElement
public class ValidationRuleSetInfo {

    public long id;
	public String name;
	public String description;
    public int numberOfInactiveRules;
    public int numberOfRules;

	public ValidationRuleSetInfo(ValidationRuleSet validationRuleSet) {
        id = validationRuleSet.getId();
        name = validationRuleSet.getName();
        description = validationRuleSet.getDescription();
        List<? extends ValidationRule> rules = validationRuleSet.getRules();
        numberOfRules = rules.size();
        numberOfInactiveRules = 0;
        for (ValidationRule rule : validationRuleSet.getRules()) {
            if (!rule.isActive()) {
                numberOfInactiveRules++;
            }
        }
    }

    public ValidationRuleSetInfo() {
    }

    public static Comparator<ValidationRuleSetInfo> VALIDATION_RULESET_NAME_COMPARATOR
            = new Comparator<ValidationRuleSetInfo>() {

        public int compare(ValidationRuleSetInfo ruleset1, ValidationRuleSetInfo ruleset2) {
            if(ruleset1 == null || ruleset1.name == null || ruleset2 == null || ruleset2.name == null) {
                throw new IllegalArgumentException("Ruleset information is missed");
            }
            return ruleset1.name.compareToIgnoreCase(ruleset2.name);
        }
    };
}
