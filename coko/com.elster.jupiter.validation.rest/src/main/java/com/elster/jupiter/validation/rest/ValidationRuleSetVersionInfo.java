/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.rest;


import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
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
    public VersionInfo<Long> parent;
    public long version;

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
        ValidationRuleSet validationRuleSet = validationRuleSetVersion.getRuleSet();
        this.ruleSet = new ValidationRuleSetInfo(validationRuleSet);
        parent = new VersionInfo<>(validationRuleSet.getId(), validationRuleSet.getVersion());
        List<? extends ValidationRule> rules = validationRuleSetVersion.getRules();
        numberOfRules = rules.size();
        numberOfInactiveRules = (int) rules.stream().filter(r -> !r.isActive()).count();
        version = validationRuleSetVersion.getVersion();
    }


}
