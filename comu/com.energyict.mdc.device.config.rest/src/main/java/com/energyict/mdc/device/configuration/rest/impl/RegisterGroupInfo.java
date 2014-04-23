package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.RegisterMapping;
import com.energyict.mdc.masterdata.rest.RegisterMappingInfo;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class RegisterGroupInfo {
    @JsonProperty("id")
    public long id;
    @JsonProperty("name")
    public String name;
    @JsonProperty("registerTypes")
    public List<RegisterMappingInfo> registerTypes;

    public RegisterGroupInfo(RegisterGroup registerGroup){
        this.id = registerGroup.getId();
        this.name = registerGroup.getName();

        for (RegisterMapping registerMapping : registerGroup.getRegisterMappings()) {
            registerTypes.add(new RegisterMappingInfo(registerMapping, false));
        }
    }
}
