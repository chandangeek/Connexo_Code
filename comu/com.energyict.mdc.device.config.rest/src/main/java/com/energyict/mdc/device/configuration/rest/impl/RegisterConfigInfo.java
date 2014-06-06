package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.energyict.mdc.common.rest.UnitAdapter;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.masterdata.RegisterMapping;
import com.energyict.mdc.protocol.api.device.MultiplierMode;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

public class RegisterConfigInfo {

    public long id;
    public String name;
    public ReadingTypeInfo readingType;
    public Long registerMapping;
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode obisCode;
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode overruledObisCode;
    public String obisCodeDescription;
    @XmlJavaTypeAdapter(UnitAdapter.class)
    public Unit unitOfMeasure;
    public int numberOfDigits;
    public int numberOfFractionDigits;
    public BigDecimal multiplier;
    public BigDecimal overflowValue;
    public int timeOfUse;

    public RegisterConfigInfo() {
    }

    public static RegisterConfigInfo from(RegisterSpec registerSpec) {
        RegisterConfigInfo registerConfigInfo = new RegisterConfigInfo();
        registerConfigInfo.id = registerSpec.getId();
        registerConfigInfo.name = registerSpec.getRegisterMapping().getName();
        registerConfigInfo.readingType = new ReadingTypeInfo(registerSpec.getRegisterMapping().getReadingType());
        registerConfigInfo.obisCode = registerSpec.getObisCode();
        registerConfigInfo.overruledObisCode = registerSpec.getDeviceObisCode();
        registerConfigInfo.obisCodeDescription = registerSpec.getObisCode().getDescription();
        registerConfigInfo.unitOfMeasure = registerSpec.getUnit();
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
