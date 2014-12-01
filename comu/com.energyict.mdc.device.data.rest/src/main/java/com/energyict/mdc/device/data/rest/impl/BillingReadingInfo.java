package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.rest.IntervalInfo;
import com.energyict.mdc.common.rest.UnitAdapter;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.data.BillingReading;
import com.energyict.mdc.device.data.Register;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationResult;
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
    @JsonProperty("unitOfMeasure")
    @XmlJavaTypeAdapter(UnitAdapter.class)
    public Unit unitOfMeasure;
    @JsonProperty("interval")
    public IntervalInfo interval;
    @JsonProperty("multiplier")
    public BigDecimal multiplier;
    @JsonProperty("validationStatus")
    public Boolean validationStatus;
    @JsonProperty("dataValidated")
    public Boolean dataValidated;
    @JsonProperty("validationResult")
    @XmlJavaTypeAdapter(ValidationStatusAdapter.class)
    public ValidationStatus validationResult;
    @JsonProperty("suspectReason")
    public Set<ValidationRuleInfo> suspectReason;

    public BillingReadingInfo() {
    }

    public BillingReadingInfo(BillingReading reading, NumericalRegisterSpec registerSpec, boolean isValidationStatusActive, DataValidationStatus dataValidationStatus) {
        super(reading);
        if(reading.getQuantity() != null) {
            this.value = reading.getQuantity().getValue();
        }
        this.unitOfMeasure = registerSpec.getUnit();
        if (reading.getInterval().isPresent()) {
            this.interval = IntervalInfo.from(reading.getInterval().get());
        }
        this.validationStatus = isValidationStatusActive;
        if(dataValidationStatus != null) {
            this.dataValidated = dataValidationStatus.completelyValidated();
            this.validationResult = ValidationStatus.forResult(ValidationResult.getValidationResult(dataValidationStatus.getReadingQualities()));
            this.suspectReason = ValidationRuleInfo.from(dataValidationStatus);
        }
    }

    @Override
    protected BaseReading createNew(Register register) {
        ReadingImpl reading = ReadingImpl.of(register.getReadingType().getMRID(), this.value, this.timeStamp.toInstant());
        if (this.interval != null) {
            reading.setTimePeriod(Range.openClosed(Instant.ofEpochMilli(this.interval.start), Instant.ofEpochMilli(this.interval.end)));
        }
        return reading;
    }
}