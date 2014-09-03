package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.rest.ValidationRuleInfo;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.data.NumericalReading;
import com.energyict.mdc.device.data.Reading;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = BillingReadingInfo.class, name = "billing"),
        @JsonSubTypes.Type(value = NumericalReadingInfo.class, name = "numerical"),
        @JsonSubTypes.Type(value = TextReadingInfo.class, name = "text"),
        @JsonSubTypes.Type(value = FlagsReadingInfo.class, name = "flags")
})
public abstract class ReadingInfo<R extends Reading, S extends RegisterSpec> {
    @JsonProperty("timeStamp")
    public Long timeStamp;
    @JsonProperty("reportedDateTime")
    public Long reportedDateTime;

    public ReadingInfo() {}

    public ReadingInfo(R reading, S registerSpec) {
        this.timeStamp = reading.getTimeStamp().getTime();
        this.reportedDateTime = reading.getReportedDateTime().getTime();
    }

    protected List<ValidationRuleInfo> getSuspectReason(DataValidationStatus dataValidationStatus) {
        Collection<ReadingQualityRecord> readingQualityRecords = dataValidationStatus.getReadingQualities();
        Collection<ValidationRule> validationRules = new ArrayList<>();
        for(ReadingQualityRecord record : readingQualityRecords) {
            validationRules.addAll(dataValidationStatus.getOffendedValidationRule(record));
        }
        List<ValidationRuleInfo> validationRuleInfos = new ArrayList<>(validationRules.size());
        for(ValidationRule validationRule : validationRules) {
            if(validationRule.getObsoleteDate() == null) {
                validationRuleInfos.add(new ValidationRuleInfo(validationRule));
            } else {
                ValidationRuleInfo validationRuleInfo = new ValidationRuleInfo();
                validationRuleInfo.name = "removed rule";
                validationRuleInfos.add(validationRuleInfo);
            }
        }
        return validationRuleInfos;
    }

    protected ValidationStatus getValidationResult(R reading) {
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
}
