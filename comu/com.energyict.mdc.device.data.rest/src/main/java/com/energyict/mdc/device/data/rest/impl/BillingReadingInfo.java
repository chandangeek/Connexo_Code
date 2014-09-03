package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.rest.ValidationRuleInfo;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.rest.IntervalInfo;
import com.energyict.mdc.common.rest.UnitAdapter;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.data.BillingReading;
import com.energyict.mdc.device.data.Register;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BillingReadingInfo extends ReadingInfo<BillingReading, NumericalRegisterSpec> {
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
    public List<ValidationRuleInfo> suspectReason;

    public BillingReadingInfo() {
    }

    public BillingReadingInfo(BillingReading reading, NumericalRegisterSpec registerSpec, boolean isValidationStatusActive, DataValidationStatus dataValidationStatus) {
        super(reading, registerSpec);
        this.value = reading.getQuantity().getValue();
        this.unitOfMeasure = registerSpec.getUnit();
        if (reading.getInterval().isPresent()) {
            this.interval = IntervalInfo.from(reading.getInterval().get());
        }
        this.validationStatus = isValidationStatusActive;
        if(isValidationStatusActive) {
            this.dataValidated = dataValidationStatus.completelyValidated();
            this.validationResult = getValidationResult(reading);
            this.suspectReason = getSuspectReason(dataValidationStatus);
        }

    }

    private ValidationStatus getValidationResult(BillingReading reading) {
        ValidationStatus validationResult = ValidationStatus.NOT_VALIDATED;
        if(reading.isValidated()) {
            if(reading.getReadingQualities().size() == 1 && reading.getReadingQualities().get(0).getTypeCode().equals(ReadingQualityType.MDM_VALIDATED_OK_CODE)) {
                validationResult = ValidationStatus.OK;
            } else {
                validationResult = ValidationStatus.SUSPECT;
            }
        }
        return validationResult;
    }

    private List<ValidationRuleInfo> getSuspectReason(DataValidationStatus dataValidationStatus) {
        Collection<ReadingQualityRecord> readingQualityRecords = dataValidationStatus.getReadingQualities();
        Collection<ValidationRule> validationRules = new ArrayList<>();
        for(ReadingQualityRecord record : readingQualityRecords) {
            validationRules.addAll(dataValidationStatus.getOffendedValidationRule(record));
        }
        List<ValidationRuleInfo> validationRuleInfos = new ArrayList<>(validationRules.size());
        for(ValidationRule validationRule : validationRules) {
            validationRuleInfos.add(new ValidationRuleInfo(validationRule));
        }
        return validationRuleInfos;
    }

}