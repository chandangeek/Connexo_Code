package com.energyict.mdc.device.configuration.rest;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.TextualRegisterSpec;

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
    @JsonProperty("numberOfFractionDigits")
    public Integer numberOfFractionDigits;
    @JsonProperty("overflow")
    public BigDecimal overflow;
    @JsonProperty("asText")
    public boolean asText;
    @JsonProperty("useMultiplier")
    public Boolean useMultiplier;
    @JsonProperty("calculatedReadingType")
    public ReadingTypeInfo calculatedReadingType;
    public List<ReadingTypeInfo> possibleCalculatedReadingTypes = new ArrayList<>();
    @JsonProperty("collectedReadingType")
    public ReadingTypeInfo collectedReadingType;
    public long version;
    public VersionInfo<Long> parent;

    public RegisterConfigInfo() {
    }

    @Deprecated //use RegisterConfigInfoFactory
    public RegisterConfigInfo(NumericalRegisterSpec registerSpec, List<ReadingType> multipliedCalculatedRegisterTypes) {
        this.id = registerSpec.getId();
        this.name = registerSpec.getRegisterType().getReadingType().getFullAliasName();
        this.registerType = registerSpec.getRegisterType().getId();
        this.readingType = new ReadingTypeInfo(registerSpec.getRegisterType().getReadingType());
        this.obisCode = registerSpec.getObisCode();
        this.overruledObisCode = registerSpec.getDeviceObisCode();
        this.obisCodeDescription = registerSpec.getObisCode().getDescription();
        this.asText = registerSpec.isTextual();
        this.collectedReadingType = new ReadingTypeInfo(registerSpec.getReadingType());
        this.version = registerSpec.getVersion();
        this.parent = new VersionInfo<>(registerSpec.getDeviceConfiguration().getId(), registerSpec.getDeviceConfiguration().getVersion());
        this.numberOfFractionDigits = registerSpec.getNumberOfFractionDigits();
        registerSpec.getOverflowValue().ifPresent(overflow -> this.overflow = overflow);
        this.useMultiplier = registerSpec.isUseMultiplier();
        if(this.useMultiplier){
            this.calculatedReadingType = new ReadingTypeInfo(registerSpec.getCalculatedReadingType().get());
        }
        multipliedCalculatedRegisterTypes.forEach(readingTypeConsumer -> possibleCalculatedReadingTypes.add(new ReadingTypeInfo(readingTypeConsumer)));
    }

    @Deprecated //use RegisterConfigInfoFactory
    public RegisterConfigInfo(TextualRegisterSpec registerSpec, List<ReadingType> multipliedCalculatedRegisterTypes) {
        this.id = registerSpec.getId();
        this.name = registerSpec.getRegisterType().getReadingType().getFullAliasName();
        this.registerType = registerSpec.getRegisterType().getId();
        this.readingType = new ReadingTypeInfo(registerSpec.getRegisterType().getReadingType());
        this.obisCode = registerSpec.getObisCode();
        this.overruledObisCode = registerSpec.getDeviceObisCode();
        this.obisCodeDescription = registerSpec.getObisCode().getDescription();
        this.asText = registerSpec.isTextual();
        this.collectedReadingType = new ReadingTypeInfo(registerSpec.getReadingType());
        this.version = registerSpec.getVersion();
        this.parent = new VersionInfo<>(registerSpec.getDeviceConfiguration().getId(), registerSpec.getDeviceConfiguration().getVersion());
        this.numberOfFractionDigits = null;
        this.overflow = null;
        multipliedCalculatedRegisterTypes.forEach(readingTypeConsumer -> possibleCalculatedReadingTypes.add(new ReadingTypeInfo(readingTypeConsumer)));
    }

    @Deprecated //use RegisterConfigInfoFactory
    public static RegisterConfigInfo from(RegisterSpec registerSpec, List<ReadingType> multipliedCalculatedRegisterTypes) {
        if (registerSpec.isTextual()) {
            return new RegisterConfigInfo((TextualRegisterSpec) registerSpec, multipliedCalculatedRegisterTypes);
        } else {
            return new RegisterConfigInfo((NumericalRegisterSpec) registerSpec, multipliedCalculatedRegisterTypes);
        }
    }

    public void writeTo(TextualRegisterSpec.Updater textualRegisterSpecUpdater){
        textualRegisterSpecUpdater.overruledObisCode(this.overruledObisCode);
    }

    public void writeTo(NumericalRegisterSpec.Updater numericalRegisterSpecUpdater, ReadingType calculatedReadingType){
        numericalRegisterSpecUpdater.overruledObisCode(this.overruledObisCode);
        numericalRegisterSpecUpdater.overflowValue(this.overflow);
        numericalRegisterSpecUpdater.numberOfFractionDigits(this.numberOfFractionDigits != null ? this.numberOfFractionDigits : 0);
        if(this.useMultiplier) {
            numericalRegisterSpecUpdater.useMultiplierWithCalculatedReadingType(calculatedReadingType);
        } else {
            numericalRegisterSpecUpdater.noMultiplier();
        }
    }

}
