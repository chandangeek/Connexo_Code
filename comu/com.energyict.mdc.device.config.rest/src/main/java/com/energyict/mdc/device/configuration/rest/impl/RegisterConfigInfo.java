package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.RegisterSpec;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.codehaus.jackson.annotate.JsonProperty;

public class RegisterConfigInfo {

    @JsonProperty("id")
    public long id;
    @JsonProperty("name")
    public String name;
    @JsonProperty("readingType")
    public ReadingTypeInfo readingTypeInfo;
    @JsonProperty("obisCode")
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode obisCode;
    @JsonProperty("obisCodeDescription")
    public String obisCodeDescription;

    public RegisterConfigInfo() {
    }

    public RegisterConfigInfo(RegisterSpec registerSpec) {
        this.id = registerSpec.getId();
        this.name = registerSpec.getRegisterMapping().getName();
        this.readingTypeInfo = new ReadingTypeInfo(registerSpec.getRegisterMapping().getReadingType());
        this.obisCode = registerSpec.getObisCode();
        this.obisCodeDescription = registerSpec.getObisCode().getDescription();
    }
}
