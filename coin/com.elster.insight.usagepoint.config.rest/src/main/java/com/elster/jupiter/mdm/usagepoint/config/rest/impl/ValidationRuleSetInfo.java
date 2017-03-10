/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.rest.impl;

import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.elster.jupiter.validation.ValidationVersionStatus;
import com.elster.jupiter.validation.rest.ValidationRuleSetVersionInfo;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Optional;

@XmlRootElement
public class ValidationRuleSetInfo {
    public long id;
    public String name;
    public String description;
    public Long startDate;
    public Long endDate;
    public int numberOfVersions;
    public Boolean hasCurrent;
    public long version;
    public long currentVersionId;
    public ValidationRuleSetVersionInfo currentVersion;

    public ValidationRuleSetInfo(ValidationRuleSet validationRuleSet) {
        id = validationRuleSet.getId();
        name = validationRuleSet.getName();
        description = validationRuleSet.getDescription();
        hasCurrent = false;
        validationRuleSet.getRuleSetVersions().stream()
                .filter(v -> ValidationVersionStatus.CURRENT.equals(v.getStatus()))
                .findFirst()
                .ifPresent(ver -> {
                    Optional.ofNullable(ver.getStartDate()).ifPresent(sd -> this.startDate = sd.toEpochMilli());
                    Optional.ofNullable(ver.getEndDate()).ifPresent(ed -> this.endDate = ed.toEpochMilli());
                    this.hasCurrent = true;
                    this.currentVersionId = ver.getId();
                });
        numberOfVersions = validationRuleSet.getRuleSetVersions().size();
        version = validationRuleSet.getVersion();
    }

    public ValidationRuleSetInfo(ValidationRuleSet validationRuleSet, ValidationRuleSetVersion currentVersion) {
        this(validationRuleSet);
        this.currentVersion = new ValidationRuleSetVersionInfo(currentVersion);
    }

    public ValidationRuleSetInfo() {
    }
}
