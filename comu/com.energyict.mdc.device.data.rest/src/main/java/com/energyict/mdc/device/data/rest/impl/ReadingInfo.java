package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.readings.BaseReading;
import com.energyict.mdc.device.data.Reading;
import com.energyict.mdc.device.data.Register;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.util.Date;

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
    public Date id;    
    @JsonProperty("timeStamp")
    public Date timeStamp;
    @JsonProperty("reportedDateTime")
    public Date reportedDateTime;
    @JsonProperty("modificationFlag")
    @XmlJavaTypeAdapter(ReadingModificationFlagAdapter.class)
    public ReadingModificationFlag modificationFlag;

    public ReadingInfo() {
    }

    public ReadingInfo(Reading reading) {
        this.id = reading.getTimeStamp();
        this.timeStamp = reading.getTimeStamp();
        this.reportedDateTime = reading.getReportedDateTime();
        this.modificationFlag = ReadingModificationFlag.getFlag(reading.getActualReading());
    }

    protected abstract BaseReading createNew(Register register);
}
