/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.IntervalInfo;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.rest.ValidationRuleInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OutputChannelDataInfo {

    public IntervalInfo interval;

    @XmlJavaTypeAdapter(BigDecimalAsStringAdapter.class)
    public BigDecimal value;

    @XmlJavaTypeAdapter(BigDecimalAsStringAdapter.class)
    public BigDecimal calculatedValue;

    public Instant reportedDateTime;

    public Boolean dataValidated;

    @XmlJavaTypeAdapter(ValidationStatusAdapter.class)
    public ValidationStatus validationResult;

    public ValidationAction action;

    public Set<ValidationRuleInfo> validationRules;

    public Boolean isConfirmed;

    @XmlJavaTypeAdapter(ReadingModificationFlagAdapter.class)
    public ReadingModificationFlag modificationFlag;

    public Instant modificationDate;

    public IdWithNameInfo editedInApp;

    public List<ReadingQualityInfo> readingQualities;

    public String calendarName;

    public boolean partOfTimeOfUseGap;

    public EstimationQuantityInfo estimatedByRule;

    public boolean isProjected;

    public long ruleId;

    public long commentId;

    public String commentValue;

    public IntervalReadingImpl createNew() {
        return IntervalReadingImpl.of(Instant.ofEpochMilli(this.interval.end), this.value, Collections.emptyList());
    }

    public IntervalReadingImpl createConfirm() {
        return IntervalReadingImpl.of(Instant.ofEpochMilli(this.interval.end), null, Collections.emptyList());
    }
}
