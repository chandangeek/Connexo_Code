package com.elster.jupiter.validation.rest;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleProperties;

import java.util.ArrayList;
import java.util.List;

public class ValidationRuleInfo {

    public long id;
    public boolean active;
    public String implementation; //validator classname
    public String displayName; // readable name
    public String name;
    public int position;
    public long ruleSetId;
    public List<ValidationRulePropertyInfo> properties = new ArrayList<ValidationRulePropertyInfo>();
    public List<ReadingTypeInfo> readingTypes = new ArrayList<ReadingTypeInfo>();

    public ValidationRuleInfo(ValidationRule validationRule) {
        id = validationRule.getId();
        active = validationRule.isActive();
        implementation = validationRule.getImplementation();
        displayName = validationRule.getDisplayName();
        name = validationRule.getName();
        ruleSetId = validationRule.getRuleSet().getId();
        for (ValidationRuleProperties property : validationRule.getProperties()) {
            properties.add(new ValidationRulePropertyInfo(property));
        }
        for (ReadingType readingType : validationRule.getReadingTypes()) {
            readingTypes.add(new ReadingTypeInfo(readingType));
        }
    }


    public ValidationRuleInfo() {
    }
}
