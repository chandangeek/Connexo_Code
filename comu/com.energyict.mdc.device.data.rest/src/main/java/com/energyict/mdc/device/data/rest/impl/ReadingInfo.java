package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.readings.BaseReading;
import com.energyict.mdc.device.data.Reading;
import com.energyict.mdc.device.data.Register;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.time.Instant;

@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = BillingReadingInfo.class, name = "billing"),
        @JsonSubTypes.Type(value = NumericalReadingInfo.class, name = "numerical"),
        @JsonSubTypes.Type(value = TextReadingInfo.class, name = "text"),
        @JsonSubTypes.Type(value = FlagsReadingInfo.class, name = "flags")
})
public abstract class ReadingInfo {
    @JsonProperty("id")
    public Instant id;
    @JsonProperty("timeStamp")
    public Instant timeStamp;
    @JsonProperty("reportedDateTime")
    public Instant reportedDateTime;
    @JsonProperty("modificationFlag")
    @XmlJavaTypeAdapter(ReadingModificationFlagAdapter.class)
    public ReadingModificationFlag modificationFlag;

    public ReadingInfo() {
    }

    public ReadingInfo(Reading reading) {
        this.id = reading.getTimeStamp();
        this.timeStamp = reading.getTimeStamp();
        this.reportedDateTime = reading.getReportedDateTime();
        this.modificationFlag = ReadingModificationFlag.getModificationFlag(reading.getActualReading());
    }

    protected abstract BaseReading createNew(Register register);

}