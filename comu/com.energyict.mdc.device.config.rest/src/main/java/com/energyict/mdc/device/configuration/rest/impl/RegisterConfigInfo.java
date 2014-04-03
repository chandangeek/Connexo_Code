package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.protocol.api.device.MultiplierMode;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class RegisterConfigInfo {

    @JsonProperty("id")
    public long id;
    @JsonProperty("name")
    public String name;
    @JsonProperty("readingType")
    public ReadingTypeInfo readingTypeInfo;
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
    public Unit unit;
    @JsonProperty("numberOfDigits")
    public int numberOfDigits;
    @JsonProperty("numberOfFractionDigits")
    public int numberOfFractionDigits;
    @JsonProperty("multiplier")
    public BigDecimal multiplier;
    @JsonProperty("overflowValue")
    public BigDecimal overflowValue;
    @JsonProperty("timeOfUse")
    public int timeOfUse;

    public RegisterConfigInfo() {
    }

    public static RegisterConfigInfo from(RegisterSpec registerSpec) {
        RegisterConfigInfo registerConfigInfo = new RegisterConfigInfo();
        registerConfigInfo.id = registerSpec.getId();
        registerConfigInfo.name = registerSpec.getRegisterMapping().getName();
        registerConfigInfo.readingTypeInfo = new ReadingTypeInfo(registerSpec.getRegisterMapping().getReadingType());
        registerConfigInfo.obisCode = registerSpec.getObisCode();
        registerConfigInfo.overruledObisCode = registerSpec.getDeviceObisCode();
        registerConfigInfo.obisCodeDescription = registerSpec.getObisCode().getDescription();
        registerConfigInfo.unit = registerSpec.getUnit();
        registerConfigInfo.numberOfDigits = registerSpec.getNumberOfDigits();
        registerConfigInfo.numberOfFractionDigits = registerSpec.getNumberOfFractionDigits();
        registerConfigInfo.multiplier = registerSpec.getMultiplier();
        registerConfigInfo.overflowValue = registerSpec.getOverflowValue();
        registerConfigInfo.registerMapping = registerSpec.getRegisterMapping().getId();
        registerConfigInfo.timeOfUse = registerSpec.getRegisterMapping().getTimeOfUse();
        return registerConfigInfo;
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
        registerSpec.setMultiplier(this.multiplier);
        registerSpec.setOverflow(this.overflowValue);
        registerSpec.setNumberOfDigits(this.numberOfDigits);
        registerSpec.setNumberOfFractionDigits(this.numberOfFractionDigits);
        registerSpec.setOverruledObisCode(this.overruledObisCode);
        registerSpec.setRegisterMapping(registerMapping);
    }
}
