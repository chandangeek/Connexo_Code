package com.energyict.mdc.device.configuration.rest;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.energyict.mdc.common.rest.UnitAdapter;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.masterdata.rest.PhenomenonInfo;
import com.energyict.mdc.device.config.TextualRegisterSpec;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.protocol.api.device.MultiplierMode;

import org.codehaus.jackson.annotate.JsonProperty;

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
    public PhenomenonInfo unitOfMeasure;
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

    public RegisterConfigInfo(NumericalRegisterSpec registerSpec) {
        this.id = registerSpec.getId();
        this.name = registerSpec.getRegisterType().getName();
        this.registerType = registerSpec.getRegisterType().getId();
        this.readingType = new ReadingTypeInfo(registerSpec.getRegisterType().getReadingType());
        this.timeOfUse = registerSpec.getRegisterType().getTimeOfUse();
        this.obisCode = registerSpec.getObisCode();
        this.overruledObisCode = registerSpec.getDeviceObisCode();
        this.obisCodeDescription = registerSpec.getObisCode().getDescription();
        this.unitOfMeasure = PhenomenonInfo.from(registerSpec.getRegisterMapping().getPhenomenon());
        this.numberOfDigits = registerSpec.getNumberOfDigits();
        this.numberOfFractionDigits = registerSpec.getNumberOfFractionDigits();
        this.multiplier = registerSpec.getMultiplier();
        this.overflow = registerSpec.getOverflowValue();
        this.multiplierMode = registerSpec.getMultiplierMode();
    }

    public RegisterConfigInfo(TextualRegisterSpec registerSpec) {
        this.id = registerSpec.getId();
        this.name = registerSpec.getRegisterType().getName();
        this.registerType = registerSpec.getRegisterType().getId();
        this.readingType = new ReadingTypeInfo(registerSpec.getRegisterType().getReadingType());
        this.timeOfUse = registerSpec.getRegisterType().getTimeOfUse();
        this.obisCode = registerSpec.getObisCode();
        this.overruledObisCode = registerSpec.getDeviceObisCode();
        this.obisCodeDescription = registerSpec.getObisCode().getDescription();
        this.unitOfMeasure = registerSpec.getUnit();
        this.numberOfDigits = null;
        this.numberOfFractionDigits = null;
        this.multiplier = null;
        this.overflow = null;
        this.multiplierMode = null;
    }

    public static RegisterConfigInfo from(RegisterSpec registerSpec) {
        if (registerSpec.isTextual()) {
            return new RegisterConfigInfo((TextualRegisterSpec) registerSpec);
        }
        else {
            return new RegisterConfigInfo((NumericalRegisterSpec) registerSpec);
        }
    }

    public static List<RegisterConfigInfo> from(List<RegisterSpec> registerSpecList) {
        List<RegisterConfigInfo> registerConfigs = new ArrayList<>(registerSpecList.size());
        for (RegisterSpec registerSpec : registerSpecList) {
            registerConfigs.add(RegisterConfigInfo.from(registerSpec));
        }
        return registerConfigs;
    }

    public void writeTo(RegisterSpec registerSpec, RegisterType registerType) {
        registerSpec.setOverruledObisCode(this.overruledObisCode);
        registerSpec.setRegisterType(registerType);
        if (!registerSpec.isTextual()) {
            this.writeTo((NumericalRegisterSpec) registerSpec);
        }
    }

    private void writeTo(NumericalRegisterSpec registerSpec) {
        registerSpec.setMultiplierMode(MultiplierMode.CONFIGURED_ON_OBJECT);
        registerSpec.setMultiplier(this.multiplier);
        registerSpec.setOverflowValue(this.overflow);
        registerSpec.setNumberOfDigits(this.numberOfDigits != null ? this.numberOfDigits : 0);
        registerSpec.setNumberOfFractionDigits(this.numberOfFractionDigits != null ? this.numberOfFractionDigits : 0);
    }

}
