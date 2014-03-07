package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.config.RegisterMapping;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonUnwrapped;

@XmlRootElement
public class RegisterMappingInfo {

    @JsonProperty("id")
    public long id;
    @JsonProperty("name")
    public String name;
    @JsonProperty("obisCode")
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode obisCode;
    @JsonProperty("isInUse")
    public boolean isInUse;
    @JsonProperty("timeOfUse")
    public int timeOfUse;
    @JsonProperty("unit")
    @XmlJavaTypeAdapter(UnitAdapter.class)
    public Unit unit;
    @JsonUnwrapped
    public ReadingTypeInfo readingTypeInfo;

    public RegisterMappingInfo() {
    }

    public RegisterMappingInfo(RegisterMapping registerMapping) {
        id = registerMapping.getId();
        name = registerMapping.getName();
        obisCode = registerMapping.getObisCode();
        isInUse = registerMapping.isInUse();
        unit = registerMapping.getUnit();
        readingTypeInfo = new ReadingTypeInfo(registerMapping.getReadingType());
    }

    public void writeTo(RegisterMapping registerMapping, ReadingType readingType) {
        registerMapping.setName(this.name);
        registerMapping.setObisCode(this.obisCode);
        registerMapping.setTimeOfUse(this.timeOfUse);
        registerMapping.setUnit(this.unit);
        registerMapping.setReadingType(readingType);
    }
}
