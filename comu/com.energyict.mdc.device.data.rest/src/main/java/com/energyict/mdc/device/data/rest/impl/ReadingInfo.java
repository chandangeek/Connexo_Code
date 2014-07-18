package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.Reading;
import com.energyict.mdc.device.data.Register;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;

@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = EventReadingInfo.class, name = "EVENT"),
        @JsonSubTypes.Type(value = NumericalReadingInfo.class, name = "NUMERICAL")
})
public abstract class ReadingInfo<R extends Reading> {
    @JsonProperty("timeStamp")
    public Long timeStamp;
    @JsonProperty("validationStatus")
    @XmlJavaTypeAdapter(ValidationStatusAdapter.class)
    public ValidationStatus validationStatus;
    @JsonProperty("multiplier")
    public BigDecimal multiplier;

    public ReadingInfo() {}

    public ReadingInfo(R reading, Register register) {
        this.timeStamp = reading.getTimeStamp().getTime();
        this.validationStatus = ValidationStatus.NOT_VALIDATED;
        if(reading.isValidated()) {
            if(reading.getReadingQualities().size() == 1 && reading.getReadingQualities().get(0).getType().getCode().equals("3.0.1")) {
                this.validationStatus = ValidationStatus.OK;
            } else {
                this.validationStatus = ValidationStatus.SUSPECT;
            }
        }
        this.multiplier = register.getRegisterSpec().getMultiplier();
    }
}
