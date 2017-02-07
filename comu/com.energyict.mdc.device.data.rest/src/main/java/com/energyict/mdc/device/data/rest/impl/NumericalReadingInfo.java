package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.validation.rest.ValidationRuleInfo;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.rest.IntervalInfo;
import com.energyict.mdc.common.rest.UnitAdapter;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.rest.BigDecimalAsStringAdapter;

import com.fasterxml.jackson.annotation.JsonProperty;

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
    public NumericalReadingInfo() {}
    @JsonProperty("interval")
    public IntervalInfo interval;
    @JsonProperty("eventDate")
    public Instant eventDate;

    @Override
    protected BaseReading createNew(Register<?, ?> register) {
        return ReadingImpl.of(register.getReadingType().getMRID(), this.value, this.timeStamp);
    }

}