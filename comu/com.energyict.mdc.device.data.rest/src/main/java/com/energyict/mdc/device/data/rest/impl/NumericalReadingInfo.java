/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.validation.rest.ValidationRuleInfo;
import com.energyict.mdc.common.device.data.Register;
import com.energyict.mdc.common.rest.IntervalInfo;
import com.energyict.mdc.common.rest.UnitAdapter;
import com.energyict.mdc.device.data.rest.BigDecimalAsStringAdapter;

import com.energyict.cbo.Unit;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

public class NumericalReadingInfo extends ReadingInfo {
    @JsonProperty("value")
    @XmlJavaTypeAdapter(BigDecimalAsStringAdapter.class)
    public BigDecimal value;
    @XmlJavaTypeAdapter(UnitAdapter.class)
    public Unit unit;
    @JsonProperty("deltaValue")
    @XmlJavaTypeAdapter(BigDecimalAsStringAdapter.class)
    public BigDecimal deltaValue;
    @JsonProperty("rawValue")
    @XmlJavaTypeAdapter(BigDecimalAsStringAdapter.class)
    public BigDecimal rawValue;
    @JsonProperty("calculatedValue")
    @XmlJavaTypeAdapter(BigDecimalAsStringAdapter.class)
    public BigDecimal calculatedValue;
    @XmlJavaTypeAdapter(UnitAdapter.class)
    public Unit calculatedUnit;
    @JsonProperty("validationStatus")
    public Boolean validationStatus;
    @JsonProperty("dataValidated")
    public Boolean dataValidated;
    @JsonProperty("validationResult")
    @XmlJavaTypeAdapter(ValidationStatusAdapter.class)
    public ValidationStatus validationResult;
    @JsonProperty("suspectReason")
    public Set<ValidationRuleInfo> suspectReason;
    @JsonProperty("estimatedByRule")
    public EstimationRuleInfo estimatedByRule;
    @JsonProperty("isConfirmed")
    public Boolean isConfirmed;
    @JsonProperty("confirmedInApps")
    public Set<IdWithNameInfo> confirmedInApps;
    @JsonProperty("multiplier")
    public BigDecimal multiplier;
    @JsonProperty("interval")
    public IntervalInfo interval;
    @JsonProperty("eventDate")
    public Instant eventDate;

    public NumericalReadingInfo() {}

    @Override
    protected BaseReading createNew(Register<?, ?> register) {
        if(interval != null){
            return ReadingImpl.of(register.getReadingType().getMRID(), this.value, register.isBilling()?this.eventDate:this.timeStamp, Instant.ofEpochMilli(interval.start), Instant.ofEpochMilli(interval.end));
        } else if (!register.isBilling()){
            return ReadingImpl.of(register.getReadingType().getMRID(), this.value, register.isBilling()?this.eventDate:this.timeStamp);
        } else {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
    }
}