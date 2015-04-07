package com.elster.jupiter.validation.rest;


import com.elster.jupiter.validation.ValidationRuleSetVersion;

import javax.xml.bind.annotation.XmlRootElement;
import java.time.Instant;

@XmlRootElement
public class ValidationRuleSetVersionInfo{

    public long id;
    public String name;
    public String description;
    public Instant startDate;
    public ValidationRuleSetInfo ruleSet;

    public ValidationRuleSetVersionInfo() {
    }

    public ValidationRuleSetVersionInfo(ValidationRuleSetVersion validationRuleSetVersion){
        populate(validationRuleSetVersion);
    }

    public void populate(ValidationRuleSetVersion validationRuleSetVersion){
        doPopulate(validationRuleSetVersion);
    }

    private void doPopulate(ValidationRuleSetVersion validationRuleSetVersion){
        id = validationRuleSetVersion.getId();
        name = validationRuleSetVersion.getName();
        description = validationRuleSetVersion.getDescription();
        startDate = validationRuleSetVersion.getStartDate();
        ruleSet = new ValidationRuleSetInfo(validationRuleSetVersion.getRuleSet());
    }
}
