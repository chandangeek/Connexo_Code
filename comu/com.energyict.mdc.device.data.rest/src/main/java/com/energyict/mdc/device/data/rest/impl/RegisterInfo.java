package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.validation.ValidationEvaluator;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.energyict.mdc.common.rest.UnitAdapter;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.configuration.rest.MultiplierModeAdapter;
import com.energyict.mdc.device.configuration.rest.ReadingTypeInfo;
import com.energyict.mdc.device.data.Reading;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.protocol.api.device.MultiplierMode;
import com.google.common.base.Optional;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

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
public abstract class RegisterInfo<R extends Register, RE extends Reading> {
    @JsonProperty("id")
    public Long id;
    @JsonProperty("name")
    public String name;
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
    @JsonProperty("multiplierMode")
    @XmlJavaTypeAdapter(MultiplierModeAdapter.class)
    public MultiplierMode multiplierMode;
    @JsonProperty("lastReading")
    public ReadingInfo lastReading;

    public RegisterInfo() {}

    public RegisterInfo(R register, ValidationEvaluator evaluator) {
        RegisterSpec registerSpec = register.getRegisterSpec();
        this.id = registerSpec.getId();
        this.name = registerSpec.getRegisterType().getName();
        this.registerType = registerSpec.getRegisterType().getId();
        this.readingType = new ReadingTypeInfo(registerSpec.getRegisterType().getReadingType());
        this.timeOfUse = registerSpec.getRegisterType().getTimeOfUse();
        this.obisCode = registerSpec.getObisCode();
        this.overruledObisCode = registerSpec.getDeviceObisCode();
        this.obisCodeDescription = registerSpec.getObisCode().getDescription();
        this.unitOfMeasure = registerSpec.getUnit();

        Optional<RE> lastReading = register.getLastReading();
        if(lastReading.isPresent()) {
            this.lastReading = ReadingInfoFactory.asInfo(lastReading.get(), registerSpec, evaluator);
        }
    }
}
