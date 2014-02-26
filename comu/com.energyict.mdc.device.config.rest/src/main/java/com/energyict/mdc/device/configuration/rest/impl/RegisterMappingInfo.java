package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.ObisCode;
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
    @JsonUnwrapped
    public ReadingTypeInfo readingTypeInfo;

    public RegisterMappingInfo() {
    }

    public RegisterMappingInfo(RegisterMapping registerMapping) {
        id = registerMapping.getId();
        name = registerMapping.getName();
        obisCode = registerMapping.getObisCode();
        isInUse = registerMapping.isInUse();
        readingTypeInfo = new ReadingTypeInfo(registerMapping.getReadingType());
    }

    public void writeTo(RegisterMapping registerMapping) {
        registerMapping.setName(this.name);
        registerMapping.setObisCode(this.obisCode);
//        readingTypeInfo.writeTo(registerMapping.getReadingType());

    }
}
