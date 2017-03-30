/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.rest.ValidationRuleInfo;
import com.energyict.mdc.common.rest.IntervalInfo;
import com.energyict.mdc.device.data.rest.BigDecimalAsStringAdapter;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

public class ChannelDataInfo {
    @JsonProperty("interval")
    public IntervalInfo interval;

    @JsonProperty("readingTime")
    public Instant readingTime;

    @JsonProperty("value")
    @XmlJavaTypeAdapter(BigDecimalAsStringAdapter.class)
    public BigDecimal value;

    @JsonProperty("dataValidated")
    public Boolean dataValidated;

    @JsonProperty("validationRules")
    Set<ValidationRuleInfo> validationRules;

    @XmlJavaTypeAdapter(ValidationStatusAdapter.class)
    ValidationStatus validationResult;

    @JsonProperty("validationAction")
    ValidationAction validationAction;
}
