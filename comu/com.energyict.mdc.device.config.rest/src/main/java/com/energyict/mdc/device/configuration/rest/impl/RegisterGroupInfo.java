package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.MeasurementType;
import com.energyict.mdc.masterdata.rest.RegisterTypeInfo;
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
    public List<RegisterTypeInfo> registerTypes;

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
        for (MeasurementType measurementType : registerGroup.getRegisterTypes()) {
            registerTypes.add(new RegisterTypeInfo(measurementType, false, false));
        }

        Collections.sort(registerTypes, new Comparator<RegisterTypeInfo>(){
            public int compare(RegisterTypeInfo rm1, RegisterTypeInfo rm2){
                return rm1.name.compareTo(rm2.name);
            }
        });
    }
}
