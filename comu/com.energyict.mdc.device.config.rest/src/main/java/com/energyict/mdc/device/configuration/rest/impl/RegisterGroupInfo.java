package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.RegisterMapping;
import com.energyict.mdc.masterdata.rest.RegisterMappingInfo;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;

@XmlRootElement
public class RegisterGroupInfo {
    @JsonProperty("id")
    public long id;
    @JsonProperty("name")
    public String name;
    @JsonProperty("registerTypes")
    public List<RegisterMappingInfo> registerTypes;

    public RegisterGroupInfo() {
    }

    public RegisterGroupInfo(long id, String name){
        this.id = id;
        this.name = name;
    }

    public RegisterGroupInfo(RegisterGroup registerGroup){
        this.id = registerGroup.getId();
        this.name = registerGroup.getName();

        registerTypes = new ArrayList<>();
        for (RegisterMapping registerMapping : registerGroup.getRegisterMappings()) {
            registerTypes.add(new RegisterMappingInfo(registerMapping, false));
        }

        Collections.sort(registerTypes, new Comparator<RegisterMappingInfo>(){
            public int compare(RegisterMappingInfo rm1, RegisterMappingInfo rm2){
                return rm1.name.compareTo(rm2.name);
            }
        });
    }
}
