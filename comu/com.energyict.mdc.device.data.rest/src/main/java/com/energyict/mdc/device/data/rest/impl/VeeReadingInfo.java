/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.validation.rest.ValidationRuleInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by tgr on 5/09/2014.
 */
public class VeeReadingInfo {
    @JsonProperty("dataValidated")
    public Boolean dataValidated;

    @JsonProperty("validationActive")
    public Boolean validationActive;

    @JsonProperty("mainValidationInfo")
    public VeeReadingValueInfo mainValidationInfo = new VeeReadingValueInfo();

    @JsonProperty("bulkValidationInfo")
    public VeeReadingValueInfo bulkValidationInfo = new VeeReadingValueInfo();
    @JsonProperty("readingQualities")
    public List<ReadingQualityInfo> readingQualities;

    public VeeReadingInfo() {
    }
}

class VeeReadingValueInfo {
    @JsonProperty("validationResult")
    @XmlJavaTypeAdapter(ValidationStatusAdapter.class)
    public ValidationStatus validationResult;

    @JsonProperty("validationRules")
    public Set<ValidationRuleInfo> validationRules = Collections.emptySet();

    @JsonProperty("estimatedByRule")
    public EstimationRuleInfo estimatedByRule;

    @JsonProperty("valueModificationFlag")
    @XmlJavaTypeAdapter(ReadingModificationFlagAdapter.class)
    public ReadingModificationFlag valueModificationFlag;

    @JsonProperty("editedInApp")
    public IdWithNameInfo editedInApp;

    @JsonProperty("isConfirmed")
    public Boolean isConfirmed;

    @JsonProperty("confirmedInApps")
    public Set<IdWithNameInfo> confirmedInApps;

    public VeeReadingValueInfo() {
    }
}
