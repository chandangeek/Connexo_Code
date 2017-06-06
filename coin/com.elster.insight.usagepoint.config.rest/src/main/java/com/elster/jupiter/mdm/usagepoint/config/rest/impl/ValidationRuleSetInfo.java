/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.rest.impl;

import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointLifeCycleStateInfo;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.elster.jupiter.validation.rest.ValidationRuleSetVersionInfo;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Optional;

@XmlRootElement
public class ValidationRuleSetInfo {
    public long id;
    public String name;
    public String description;
    public Long startDate;
    public Long endDate;
    public int numberOfVersions;
    public long version;
    public Long currentVersionId;
    public ValidationRuleSetVersionInfo currentVersion;
    public List<UsagePointLifeCycleStateInfo> lifeCycleStates;

    public ValidationRuleSetInfo(ValidationRuleSet validationRuleSet) {
        id = validationRuleSet.getId();
        name = validationRuleSet.getName();
        description = validationRuleSet.getDescription();
        validationRuleSet.getRuleSetVersions().stream()
                .findFirst()
                .ifPresent(ver -> {
                    Optional.ofNullable(ver.getStartDate()).ifPresent(sd -> this.startDate = sd.toEpochMilli());
                    Optional.ofNullable(ver.getEndDate()).ifPresent(ed -> this.endDate = ed.toEpochMilli());
                    this.currentVersionId = ver.getId();
                });
        numberOfVersions = validationRuleSet.getRuleSetVersions().size();
        version = validationRuleSet.getVersion();
    }

    public ValidationRuleSetInfo(ValidationRuleSet validationRuleSet,  List<UsagePointLifeCycleStateInfo> lifeCycleStates) {
        this(validationRuleSet);
        this.lifeCycleStates = lifeCycleStates;
    }

    public ValidationRuleSetInfo(ValidationRuleSet validationRuleSet, ValidationRuleSetVersion currentVersion, List<UsagePointLifeCycleStateInfo> lifeCycleStates) {
        this(validationRuleSet);
        this.currentVersion = new ValidationRuleSetVersionInfo(currentVersion);
        this.lifeCycleStates = lifeCycleStates;
    }

    public ValidationRuleSetInfo() {
    }
}
