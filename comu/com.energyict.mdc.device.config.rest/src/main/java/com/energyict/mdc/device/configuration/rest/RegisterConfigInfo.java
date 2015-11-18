package com.energyict.mdc.device.configuration.rest;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.TextualRegisterSpec;
import com.energyict.mdc.masterdata.RegisterType;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class RegisterConfigInfo {
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
    public String unitOfMeasure;
    @JsonProperty("numberOfFractionDigits")
    public Integer numberOfFractionDigits;
    @JsonProperty("overflow")
    public BigDecimal overflow;
    @JsonProperty("timeOfUse")
    public Integer timeOfUse;
    @JsonProperty("asText")
    public boolean asText;
    @JsonProperty("useMultiplier")
    public Boolean useMultiplier;
    @JsonProperty("calculatedReadingType")
    public ReadingTypeInfo calculatedReadingType;
    public List<ReadingTypeInfo> possibleCalculatedReadingTypes = new ArrayList<>();
    public long version;
    public VersionInfo<Long> parent;

    public RegisterConfigInfo() {
    }

    public RegisterConfigInfo(NumericalRegisterSpec registerSpec, List<ReadingType> multipliedCalculatedRegisterTypes) {
        this.id = registerSpec.getId();
        this.name = registerSpec.getRegisterType().getReadingType().getAliasName();
        this.registerType = registerSpec.getRegisterType().getId();
        this.readingType = new ReadingTypeInfo(registerSpec.getRegisterType().getReadingType());
        this.timeOfUse = registerSpec.getRegisterType().getTimeOfUse();
        this.obisCode = registerSpec.getObisCode();
        this.overruledObisCode = registerSpec.getDeviceObisCode();
        this.obisCodeDescription = registerSpec.getObisCode().getDescription();
        this.unitOfMeasure = registerSpec.getUnit().toString();
        this.numberOfFractionDigits = registerSpec.getNumberOfFractionDigits();
        this.overflow = registerSpec.getOverflowValue();
        this.asText = registerSpec.isTextual();
        this.useMultiplier = registerSpec.isUseMultiplier();
        if(this.useMultiplier){
            this.calculatedReadingType = new ReadingTypeInfo(registerSpec.getCalculatedReadingType());
        }
        multipliedCalculatedRegisterTypes.forEach(readingTypeConsumer -> possibleCalculatedReadingTypes.add(new ReadingTypeInfo(readingTypeConsumer)));
        this.version = registerSpec.getVersion();
        this.parent = new VersionInfo<>(registerSpec.getDeviceConfiguration().getId(), registerSpec.getDeviceConfiguration().getVersion());
    }

    public RegisterConfigInfo(TextualRegisterSpec registerSpec) {
        this.id = registerSpec.getId();
        this.name = registerSpec.getRegisterType().getReadingType().getAliasName();
        this.registerType = registerSpec.getRegisterType().getId();
        this.readingType = new ReadingTypeInfo(registerSpec.getRegisterType().getReadingType());
        this.timeOfUse = registerSpec.getRegisterType().getTimeOfUse();
        this.obisCode = registerSpec.getObisCode();
        this.overruledObisCode = registerSpec.getDeviceObisCode();
        this.obisCodeDescription = registerSpec.getObisCode().getDescription();
        this.unitOfMeasure = registerSpec.getUnit().toString();
        this.numberOfFractionDigits = null;
        this.overflow = null;
        this.asText = registerSpec.isTextual();
        this.version = registerSpec.getVersion();
        this.parent = new VersionInfo<>(registerSpec.getDeviceConfiguration().getId(), registerSpec.getDeviceConfiguration().getVersion());
    }

    public static RegisterConfigInfo from(RegisterSpec registerSpec, List<ReadingType> multipliedCalculatedRegisterTypes) {
        if (registerSpec.isTextual()) {
            return new RegisterConfigInfo((TextualRegisterSpec) registerSpec);
        } else {
            return new RegisterConfigInfo((NumericalRegisterSpec) registerSpec, multipliedCalculatedRegisterTypes);
        }
    }

    public void writeTo(RegisterSpec registerSpec, RegisterType registerType, ReadingType calculatedReadingType) {
        registerSpec.setOverruledObisCode(this.overruledObisCode);
        registerSpec.setRegisterType(registerType);
        if (!registerSpec.isTextual()) {
            this.writeTo((NumericalRegisterSpec) registerSpec, calculatedReadingType);
        }
    }

    private void writeTo(NumericalRegisterSpec registerSpec, ReadingType calculatedReadingType) {
        registerSpec.setOverflowValue(this.overflow);
        registerSpec.setNumberOfFractionDigits(this.numberOfFractionDigits != null ? this.numberOfFractionDigits : 0);
        registerSpec.setUseMultiplier(this.useMultiplier);
        registerSpec.setCalculatedReadingType(calculatedReadingType);
    }

}
