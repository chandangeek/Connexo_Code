package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.rest.IntervalInfo;
import com.energyict.mdc.common.rest.UnitAdapter;
import com.energyict.mdc.device.data.Register;

import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.validation.rest.ValidationRuleInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Range;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

public class BillingReadingInfo extends ReadingInfo {
    @JsonProperty("value")
    public BigDecimal value;
    public BigDecimal deltaValue;
    @JsonProperty("unitOfMeasure")
    @XmlJavaTypeAdapter(UnitAdapter.class)
    public Unit unitOfMeasure;
    @JsonProperty("interval")
    public IntervalInfo interval;
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

    public BillingReadingInfo() {
    }

    @Override
    protected BaseReading createNew(Register register) {
        ReadingImpl reading = ReadingImpl.of(register.getReadingType().getMRID(), this.value, this.timeStamp);
        if (this.interval != null) {
            reading.setTimePeriod(Range.openClosed(Instant.ofEpochMilli(this.interval.start), Instant.ofEpochMilli(this.interval.end)));
        }
        return reading;
    }
}