package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.ReadingQualityType;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.data.Reading;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

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
    @JsonProperty("validationStatus")
    @XmlJavaTypeAdapter(ValidationStatusAdapter.class)
    public ValidationStatus validationStatus;

    public ReadingInfo() {}

    public ReadingInfo(R reading, S registerSpec) {
        this.timeStamp = reading.getTimeStamp().getTime();
        this.reportedDateTime = reading.getReportedDateTime().getTime();
        this.validationStatus = ValidationStatus.NOT_VALIDATED;
        if(reading.isValidated()) {
            if(reading.getReadingQualities().size() == 1 && reading.getReadingQualities().get(0).getType().getCode().equals(ReadingQualityType.MDM_VALIDATED_OK_CODE)) {
                this.validationStatus = ValidationStatus.OK;
            } else {
                this.validationStatus = ValidationStatus.SUSPECT;
            }
        }
    }
}
