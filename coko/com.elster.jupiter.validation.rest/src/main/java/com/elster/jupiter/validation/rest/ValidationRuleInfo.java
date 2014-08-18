package com.elster.jupiter.validation.rest;

import java.util.ArrayList;
import java.util.List;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.validation.ValidationRule;

public class ValidationRuleInfo {

    public long id;
    public boolean active;
    public String implementation; //validator classname
    public String displayName; // readable name
    public String name;
    public int position;
    public List<PropertyInfo> properties = new ArrayList<PropertyInfo>();
    public List<ReadingTypeInfo> readingTypes = new ArrayList<ReadingTypeInfo>();
    public ValidationRuleSetInfo ruleSet;

    public ValidationRuleInfo(ValidationRule validationRule, PropertyUtils propertyUtils) {
        id = validationRule.getId();
        active = validationRule.isActive();
        implementation = validationRule.getImplementation();
        displayName = validationRule.getDisplayName();
        name = validationRule.getName();
        ruleSet = new ValidationRuleSetInfo(validationRule.getRuleSet());
        properties = propertyUtils.convertPropertySpecsToPropertyInfos(validationRule.getPropertySpecs(), validationRule.getProps());
        for (ReadingType readingType : validationRule.getReadingTypes()) {
            readingTypes.add(new ReadingTypeInfo(readingType));
        }
    }

    public static List<ValidationRuleInfo> from(List<ValidationRule> validationRules, PropertyUtils propertyUtils) {
        List<ValidationRuleInfo> infos = new ArrayList<>(validationRules.size());
        for (ValidationRule validationRule : validationRules) {
            infos.add(new ValidationRuleInfo(validationRule, propertyUtils));
        }
        return infos;
    }


    public ValidationRuleInfo() {
    }
}
