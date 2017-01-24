package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.energyict.mdc.device.data.Register;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Instant;
import java.util.List;

@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = BillingReadingInfo.class, name = "billing"),
        @JsonSubTypes.Type(value = NumericalReadingInfo.class, name = "numerical"),
        @JsonSubTypes.Type(value = TextReadingInfo.class, name = "text"),
        @JsonSubTypes.Type(value = FlagsReadingInfo.class, name = "flags")
})
@JsonIgnoreProperties(ignoreUnknown=true)
public abstract class ReadingInfo {
    @JsonProperty("id")
    public String id;
    @JsonProperty("timeStamp")
    public Instant timeStamp;
    @JsonProperty("reportedDateTime")
    public Instant reportedDateTime;
    @JsonProperty("readingQualities")
    public List<ReadingQualityInfo> readingQualities;
    @JsonProperty("modificationFlag")
    @XmlJavaTypeAdapter(ReadingModificationFlagAdapter.class)
    public ReadingModificationFlag modificationFlag;
    @JsonProperty("editedInApp")
    public IdWithNameInfo editedInApp;
    public SlaveRegisterInfo slaveRegister;
    public IdWithNameInfo register;

    public ReadingInfo() {
    }

    protected abstract BaseReading createNew(Register<?, ?> register);

}