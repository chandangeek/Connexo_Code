package com.elster.jupiter.validation.rest;


import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.elster.jupiter.validation.ValidationVersionStatus;

import javax.xml.bind.annotation.XmlRootElement;
import java.time.Instant;

@XmlRootElement
public class ValidationRuleSetVersionInfo{

    public long id;
    public String description;
    public Instant startDate;
    public Instant endDate;
    public ValidationVersionStatus status;
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
        status = validationRuleSetVersion.getStatus();
        description = validationRuleSetVersion.getDescription();
        startDate = validationRuleSetVersion.getStartDate();
        endDate = validationRuleSetVersion.getEndDate();
        ruleSet = new ValidationRuleSetInfo(validationRuleSetVersion.getRuleSet());
    }
}
