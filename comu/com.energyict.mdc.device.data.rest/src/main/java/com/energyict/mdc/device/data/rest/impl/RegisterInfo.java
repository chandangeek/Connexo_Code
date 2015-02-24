package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.energyict.mdc.common.rest.UnitAdapter;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.data.Reading;
import com.energyict.mdc.device.data.Register;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.util.Optional;

@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = BillingRegisterInfo.class, name = "billing"),
        @JsonSubTypes.Type(value = NumericalRegisterInfo.class, name = "numerical"),
        @JsonSubTypes.Type(value = TextRegisterInfo.class, name = "text"),
        @JsonSubTypes.Type(value = FlagsRegisterInfo.class, name = "flags")
})
public abstract class RegisterInfo<R extends Register, RE extends Reading> {
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
    @JsonProperty("unitOfMeasure")
    @XmlJavaTypeAdapter(UnitAdapter.class)
    public Unit unitOfMeasure;
    @JsonProperty("timeOfUse")
    public Integer timeOfUse;
    @JsonProperty("lastReading")
    public ReadingInfo lastReading;
    public boolean isCumulative;

    public RegisterInfo() {}

    public RegisterInfo(Register register) {
        RegisterSpec registerSpec = register.getRegisterSpec();
        this.id = registerSpec.getId();
        this.registerType = registerSpec.getRegisterType().getId();
        this.readingType = new ReadingTypeInfo(registerSpec.getRegisterType().getReadingType());
        this.timeOfUse = registerSpec.getRegisterType().getTimeOfUse();
        this.obisCode = registerSpec.getObisCode();
        this.overruledObisCode = registerSpec.getDeviceObisCode();
        this.obisCodeDescription = registerSpec.getObisCode().getDescription();
        this.unitOfMeasure = registerSpec.getUnit();
        this.isCumulative = registerSpec.getReadingType().isCumulative();

        Optional<RE> lastReading = register.getLastReading();
        if (lastReading.isPresent()) {
            this.lastReading = ReadingInfoFactory.asInfo(lastReading.get(), registerSpec);
        }
    }

}