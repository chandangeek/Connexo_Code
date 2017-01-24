package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.obis.ObisCode;
import com.energyict.mdc.common.rest.ObisCodeAdapter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = BillingRegisterInfo.class, name = "billing"),
        @JsonSubTypes.Type(value = NumericalRegisterInfo.class, name = "numerical"),
        @JsonSubTypes.Type(value = TextRegisterInfo.class, name = "text"),
        @JsonSubTypes.Type(value = FlagsRegisterInfo.class, name = "flags")
})
public abstract class RegisterInfo {
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
    public String deviceName;
    public long version;
    public VersionInfo<Long> parent;
    public String dataloggerSlaveName;

    public RegisterInfo() {}

}