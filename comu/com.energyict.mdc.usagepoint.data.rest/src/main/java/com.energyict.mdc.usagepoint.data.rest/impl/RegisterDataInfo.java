/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.validation.ValidationAction;
import com.energyict.mdc.device.data.rest.BigDecimalAsStringAdapter;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Represents register data
 */
public class RegisterDataInfo {

    public Instant measurementTime;

    public Instant readingTime;

    @XmlJavaTypeAdapter(BigDecimalAsStringAdapter.class)
    public BigDecimal value;

    public Boolean dataValidated;

    @XmlJavaTypeAdapter(ValidationStatusAdapter.class)
    ValidationStatus validationResult;

    ValidationAction validationAction;

}
