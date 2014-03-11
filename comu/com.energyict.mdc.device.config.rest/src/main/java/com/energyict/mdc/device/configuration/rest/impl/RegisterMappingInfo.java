package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.config.RegisterMapping;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.codehaus.jackson.annotate.JsonProperty;

@XmlRootElement
public class RegisterMappingInfo {

    @JsonProperty("id")
    public long id;
    @JsonProperty("name")
    public String name;
    @JsonProperty("obisCode")
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode obisCode;
    @JsonProperty("isLinkedByDeviceType")
    public boolean isLinkedByDeviceType;
    @JsonProperty("isLinkedByRegisterConfig")
    public Boolean isLinkedByRegisterConfig; // This property makes no sense if the register mapping was retrieved outside the scope of a device type. It will be null.
    @JsonProperty("timeOfUse")
    public int timeOfUse;
    @JsonProperty("unit")
    @XmlJavaTypeAdapter(UnitAdapter.class)
    public Unit unit;
    @JsonProperty("readingType")
    public ReadingTypeInfo readingTypeInfo;

    public RegisterMappingInfo() {
    }

    public RegisterMappingInfo(RegisterMapping registerMapping) {
        id = registerMapping.getId();
        name = registerMapping.getName();
        obisCode = registerMapping.getObisCode();
        isLinkedByDeviceType = registerMapping.isLinkedByDeviceType();
        timeOfUse = registerMapping.getTimeOfUse();
        unit = registerMapping.getUnit();
        readingTypeInfo = new ReadingTypeInfo(registerMapping.getReadingType());
    }

    public RegisterMappingInfo(RegisterMapping registerMapping, boolean linkedByRegisterSpec) {
        this(registerMapping);
        this.isLinkedByRegisterConfig = linkedByRegisterSpec;
    }

    public void writeTo(RegisterMapping registerMapping, ReadingType readingType) {
        registerMapping.setName(this.name);
        registerMapping.setObisCode(this.obisCode);
        registerMapping.setTimeOfUse(this.timeOfUse);
        registerMapping.setUnit(this.unit);
        registerMapping.setReadingType(readingType);
    }
}
