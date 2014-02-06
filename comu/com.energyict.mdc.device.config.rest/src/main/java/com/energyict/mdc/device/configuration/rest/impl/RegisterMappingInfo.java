package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdw.amr.RegisterMapping;
import com.energyict.mdw.amrimpl.RegisterMappingImpl;
import com.energyict.mdw.shadow.amr.RegisterMappingShadow;
import java.sql.SQLException;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement
public class RegisterMappingInfo {

    @JsonProperty("id")
    public long id;
    @JsonProperty("name")
    public String name;
    @JsonProperty("obisCode")
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode obisCode;

    public RegisterMappingInfo() {
    }

    public RegisterMappingInfo(RegisterMapping registerMapping) {
        id = registerMapping.getId();
        name = registerMapping.getName();
        obisCode = registerMapping.getObisCode();
    }
}
