/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.rest;

import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationVersionStatus;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Comparator;
import java.util.Optional;

@XmlRootElement
public class ValidationRuleSetInfo {

    public static Comparator<ValidationRuleSetInfo> VALIDATION_RULESET_NAME_COMPARATOR = Comparator.comparing(info -> info.name.toLowerCase());

    public long id;
    public String name;
    public String description;
    public Long startDate;
    public Long endDate;
    public int numberOfVersions;
    public Boolean hasCurrent;
    public long version;
    public Boolean isInUse;

    public ValidationRuleSetInfo() {
    }

    public ValidationRuleSetInfo(ValidationRuleSet validationRuleSet) {
        this.id = validationRuleSet.getId();
        this.name = validationRuleSet.getName();
        this.description = validationRuleSet.getDescription();
        this.hasCurrent = false;
        validationRuleSet.getRuleSetVersions().stream()
                .filter(v -> ValidationVersionStatus.CURRENT.equals(v.getStatus()))
                .findFirst()
                .ifPresent(ver -> {
                    Optional.ofNullable(ver.getStartDate()).ifPresent(sd -> this.startDate = sd.toEpochMilli());
                    Optional.ofNullable(ver.getEndDate()).ifPresent(ed -> this.endDate = ed.toEpochMilli());
                    this.hasCurrent = true;
                });
        this.numberOfVersions = validationRuleSet.getRuleSetVersions().size();
        this.version = validationRuleSet.getVersion();
    }
}
