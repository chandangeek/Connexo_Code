/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.rest.ValidationRuleInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Instant;
import java.util.List;
import java.util.Set;

@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = BillingOutputRegisterDataInfo.class, name = "billing"),
        @JsonSubTypes.Type(value = NumericalOutputRegisterDataInfo.class, name = "numerical"),
        @JsonSubTypes.Type(value = TextOutputRegisterDataInfo.class, name = "text"),
        @JsonSubTypes.Type(value = FlagsOutputRegisterDataInfo.class, name = "flags")
})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class OutputRegisterDataInfo {

    public Instant timeStamp;

    public Instant reportedDateTime;

    public Boolean dataValidated;

    @XmlJavaTypeAdapter(ValidationStatusAdapter.class)
    public ValidationStatus validationResult;

    public ValidationAction action;

    public Set<ValidationRuleInfo> validationRules;

    @XmlJavaTypeAdapter(ReadingModificationFlagAdapter.class)
    public ReadingModificationFlag modificationFlag;

    public Instant modificationDate;

    public IdWithNameInfo editedInApp;

    public List<ReadingQualityInfo> readingQualities;

    public abstract BaseReading createNew(ReadingType readingType);
}
