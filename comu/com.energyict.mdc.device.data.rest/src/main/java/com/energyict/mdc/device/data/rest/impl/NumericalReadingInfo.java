package com.energyict.mdc.device.data.rest.impl;


import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.rest.UnitAdapter;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.data.NumericalReading;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.rest.BigDecimalAsStringAdapter;

import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.rest.ValidationRuleInfo;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.util.Set;

public class NumericalReadingInfo extends ReadingInfo {
    @JsonProperty("value")
    @XmlJavaTypeAdapter(BigDecimalAsStringAdapter.class)
    public BigDecimal value;
    @JsonProperty("rawValue")
    @XmlJavaTypeAdapter(BigDecimalAsStringAdapter.class)
    public BigDecimal rawValue;
    @JsonProperty("unitOfMeasure")
    @XmlJavaTypeAdapter(UnitAdapter.class)
    public Unit unitOfMeasure;
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

    public NumericalReadingInfo() {}

    public NumericalReadingInfo(NumericalReading reading, NumericalRegisterSpec registerSpec, boolean isValidationStatusActive, DataValidationStatus dataValidationStatus) {
        super(reading);
        if(reading.getQuantity() != null) {
            this.value = reading.getQuantity().getValue();
            this.rawValue = reading.getQuantity().getValue();
        }
        if (this.value != null){
            int numberOfFractionDigits = registerSpec.getNumberOfFractionDigits();
            this.value = this.value.setScale(numberOfFractionDigits, BigDecimal.ROUND_UP);
            this.rawValue = this.value;
        }
        this.unitOfMeasure = registerSpec.getUnit();
        this.multiplier = registerSpec.getMultiplier();

        this.validationStatus = isValidationStatusActive;
        if(dataValidationStatus != null) {
            this.dataValidated = dataValidationStatus.completelyValidated();
            this.validationResult = ValidationStatus.forResult(ValidationResult.getValidationResult(dataValidationStatus.getReadingQualities()));
            this.suspectReason = ValidationRuleInfo.from(dataValidationStatus);
        }
    }

    @Override
    protected BaseReading createNew(Register register) {
        return ReadingImpl.of(register.getReadingType().getMRID(), this.value, this.timeStamp.toInstant());
    }

}