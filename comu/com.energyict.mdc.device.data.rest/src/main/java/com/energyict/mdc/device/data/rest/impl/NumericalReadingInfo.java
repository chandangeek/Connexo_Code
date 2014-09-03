package com.energyict.mdc.device.data.rest.impl;


import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.rest.ValidationRuleInfo;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.rest.UnitAdapter;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.data.BillingReading;
import com.energyict.mdc.device.data.NumericalReading;
import com.energyict.mdc.device.data.Register;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.util.*;

public class NumericalReadingInfo extends ReadingInfo<NumericalReading, NumericalRegisterSpec> {
    @JsonProperty("value")
    public BigDecimal value;
    @JsonProperty("rawValue")
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
    public List<ValidationRuleInfo> suspectReason;

    public NumericalReadingInfo() {}

    public NumericalReadingInfo(NumericalReading reading, NumericalRegisterSpec registerSpec, boolean isValidationStatusActive, DataValidationStatus dataValidationStatus) {
        super(reading, registerSpec);
        this.value = reading.getQuantity().getValue();
        this.rawValue = reading.getQuantity().getValue();
        this.unitOfMeasure = registerSpec.getUnit();
        this.multiplier = registerSpec.getMultiplier();

        this.validationStatus = isValidationStatusActive;
        if(isValidationStatusActive) {
            this.dataValidated = dataValidationStatus.completelyValidated();
            this.validationResult = getValidationResult(reading);
            this.suspectReason = getSuspectReason(dataValidationStatus);
        }
    }
}
