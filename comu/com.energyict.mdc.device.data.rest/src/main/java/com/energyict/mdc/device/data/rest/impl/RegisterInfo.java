package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;

@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = RegisterInfo.class, name = "numerical"),
        @JsonSubTypes.Type(value = TextRegisterInfo.class, name = "text"),
        @JsonSubTypes.Type(value = FlagsRegisterInfo.class, name = "flags")
})
public class RegisterInfo {
    @JsonProperty("id")
    public Long id;
    @JsonProperty("readingType")
    public ReadingTypeInfo readingType;
    @JsonProperty("registerType")
    public Long registerType;
    @JsonProperty("obisCode")
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode obisCode;
    @JsonProperty("overruledObisCode")
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode overruledObisCode;
    @JsonProperty("obisCodeDescription")
    public String obisCodeDescription;
    @JsonProperty("lastReading")
    public ReadingInfo lastReading;
    public boolean isCumulative;
    public boolean hasEvent;
    public boolean isBilling;
    public String deviceName;
    public long version;
    public VersionInfo<Long> parent;
    public String dataloggerSlaveName;
    @JsonProperty("numberOfFractionDigits")
    public Integer numberOfFractionDigits;
    @JsonProperty("overruledNumberOfFractionDigits")
    public Integer overruledNumberOfFractionDigits;
    @JsonProperty("overflow")
    public BigDecimal overflow;
    @JsonProperty("overruledOverflow")
    public BigDecimal overruledOverflow;
    public DetailedValidationInfo detailedValidationInfo;
    public BigDecimal multiplier;
    public Boolean useMultiplier;
    @JsonProperty("calculatedReadingType")
    public ReadingTypeInfo calculatedReadingType;


    public RegisterInfo() {}

}