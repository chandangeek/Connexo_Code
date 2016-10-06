package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.masterdata.MeasurementType;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.rest.RegisterTypeInfo;
import com.energyict.mdc.masterdata.rest.RegisterTypeInfoFactory;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class RegisterGroupInfoFactory {

    private  final RegisterTypeInfoFactory registerTypeInfoFactory;

    @Inject
    public RegisterGroupInfoFactory(RegisterTypeInfoFactory registerTypeInfoFactory) {
        this.registerTypeInfoFactory = registerTypeInfoFactory;
    }

    public RegisterGroupInfo asInfo(long id, String name, long version){
        RegisterGroupInfo info = new RegisterGroupInfo();
        info.id = id;
        info.name = name;
        info.version = version;
        return info;
    }

    public RegisterGroupInfo asInfo(RegisterGroup registerGroup){
        RegisterGroupInfo info = new RegisterGroupInfo();
        info.id = registerGroup.getId();
        info.name = registerGroup.getName();

        info.registerTypes = new ArrayList<>();
        for (MeasurementType measurementType : registerGroup.getRegisterTypes()) {
            info.registerTypes.add(registerTypeInfoFactory.asInfo(measurementType, false, false));
        }

        Collections.sort(info.registerTypes, (rm1, rm2) -> rm1.readingType.aliasName.compareTo(rm2.readingType.aliasName));

        info.version = registerGroup.getVersion();
        return info;
    }
}
