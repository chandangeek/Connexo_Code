package com.elster.jupiter.validation.rest;


import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.elster.jupiter.validation.ValidationVersionStatus;

import javax.xml.bind.annotation.XmlRootElement;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@XmlRootElement
public class ValidationRuleSetVersionInfo{

    public long id;
    public String description;
    public Long startDate;
    public Long endDate;
    public ValidationVersionStatus status;
    public int numberOfInactiveRules;
    public int numberOfRules;
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
        if(validationRuleSetVersion==null)
            return;
        id = validationRuleSetVersion.getId();
        status = validationRuleSetVersion.getStatus();
        description = validationRuleSetVersion.getDescription();
        Optional.ofNullable(validationRuleSetVersion.getStartDate()).ifPresent(sd->{
            this.startDate = sd.toEpochMilli();
        });
        Optional.ofNullable(validationRuleSetVersion.getEndDate()).ifPresent(ed->{
            this.endDate = ed.toEpochMilli();
        });
        ruleSet = new ValidationRuleSetInfo(validationRuleSetVersion.getRuleSet());
        List<? extends ValidationRule> rules = validationRuleSetVersion.getRules();
        numberOfRules = rules.size();
        numberOfInactiveRules = (int) rules.stream().filter(r -> !r.isActive()).count();
    }


}
