package com.energyict.mdc.device.configuration.rest;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.energyict.mdc.common.rest.UnitAdapter;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.masterdata.RegisterMapping;
import com.energyict.mdc.protocol.api.device.MultiplierMode;
import com.google.common.base.Optional;
import org.codehaus.jackson.annotate.JsonProperty;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

public class RegisterConfigInfo {
    @JsonProperty("id")
    public Long id;
    @JsonProperty("name")
    public String name;
    @JsonProperty("readingType")
    public ReadingTypeInfo readingType;
    @JsonProperty("registerMapping")
    public Long registerMapping;
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
    @JsonProperty("numberOfDigits")
    public Integer numberOfDigits;
    @JsonProperty("numberOfFractionDigits")
    public Integer numberOfFractionDigits;
    @JsonProperty("multiplier")
    public BigDecimal multiplier;
    @JsonProperty("overflow")
    public BigDecimal overflow;
    @JsonProperty("timeOfUse")
    public Integer timeOfUse;
    @JsonProperty("multiplierMode")
    @XmlJavaTypeAdapter(MultiplierModeAdapter.class)
    public MultiplierMode multiplierMode;

    public RegisterConfigInfo() {
    }

    public RegisterConfigInfo(RegisterSpec registerSpec) {
        this.id = registerSpec.getId();
        this.name = registerSpec.getRegisterMapping().getName();
        this.readingType = new ReadingTypeInfo(registerSpec.getRegisterMapping().getReadingType());
        this.obisCode = registerSpec.getObisCode();
        this.overruledObisCode = registerSpec.getDeviceObisCode();
        this.obisCodeDescription = registerSpec.getObisCode().getDescription();
        this.unitOfMeasure = registerSpec.getUnit();
        this.numberOfDigits = registerSpec.getNumberOfDigits();
        this.numberOfFractionDigits = registerSpec.getNumberOfFractionDigits();
        this.multiplier = registerSpec.getMultiplier();
        this.overflow = registerSpec.getOverflowValue();
        this.registerMapping = registerSpec.getRegisterMapping().getId();
        this.timeOfUse = registerSpec.getRegisterMapping().getTimeOfUse();
        this.multiplierMode = registerSpec.getMultiplierMode();
    }

    public static RegisterConfigInfo from(RegisterSpec registerSpec) {
        return new RegisterConfigInfo(registerSpec);
    }

    public static List<RegisterConfigInfo> from(List<RegisterSpec> registerSpecList) {
        List<RegisterConfigInfo> registerConfigs = new ArrayList<>(registerSpecList.size());
        for (RegisterSpec registerSpec : registerSpecList) {
            registerConfigs.add(RegisterConfigInfo.from(registerSpec));
        }
        return registerConfigs;
    }

    public void writeTo(RegisterSpec registerSpec, RegisterMapping registerMapping) {
        registerSpec.setMultiplierMode(MultiplierMode.CONFIGURED_ON_OBJECT);
        Optional<BigDecimal> multiplier = Optional.fromNullable(this.multiplier);
        if(multiplier.isPresent()) {
            registerSpec.setMultiplier(multiplier.get());
        }
        Optional<BigDecimal> overflow = Optional.fromNullable(this.overflow);
        if(overflow.isPresent()) {
            registerSpec.setOverflow(overflow.get());
        }
        Optional<Integer> numberOfDigits = Optional.fromNullable(this.numberOfDigits);
        if(numberOfDigits.isPresent()) {
            registerSpec.setNumberOfDigits(numberOfDigits.get());
        }
        Optional<Integer> numberOfFractionDigits = Optional.fromNullable(this.numberOfFractionDigits);
        if(numberOfFractionDigits.isPresent()) {
            registerSpec.setNumberOfFractionDigits(numberOfFractionDigits.get());
        }
        Optional<ObisCode> overruledObisCode = Optional.fromNullable(this.overruledObisCode);
        if(overruledObisCode.isPresent()) {
            registerSpec.setOverruledObisCode(overruledObisCode.get());
        }

        registerSpec.setRegisterMapping(registerMapping);
    }
}
