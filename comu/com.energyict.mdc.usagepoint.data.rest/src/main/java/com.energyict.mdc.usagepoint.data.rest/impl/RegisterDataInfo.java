/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.usagepoint.data.rest.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Represents register data.<br>
 * There are a lot of register types, ref to {@link RegisterDataInfoFactory.RegisterType} that are represented differently in front end.<br>
 * Type of register type depends on attributes such as {@link RegisterDataInfo#isCumulative}, {@link RegisterDataInfo#hasEvent} and
 * {@link RegisterDataInfo#isBilling}<br>
 * Different set of attributes are used for different register types.
 */
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterDataInfo {

    public boolean isCumulative;

    public boolean hasEvent;

    public boolean isBilling;

    public BigDecimal collectedValue;

    public Instant measurementTime;

    public MeasurementPeriod measurementPeriod;

    public BigDecimal deltaValue;

    public Instant eventDate;

    @XmlJavaTypeAdapter(ValidationStatusAdapter.class)
    ValidationStatus validationResult;

    public List<ReadingQualityInfo> readingQualities;
}


