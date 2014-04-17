package com.energyict.mdc.masterdata.rest.impl;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.energyict.mdc.common.rest.TimeDurationAdapter;
import com.energyict.mdc.masterdata.LoadProfileType;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement
public class LoadProfileTypeInfo {

    public String name;
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode obisCode;
    @XmlJavaTypeAdapter(TimeDurationAdapter.class)
    public TimeDuration timeDuration;

    public LoadProfileTypeInfo() {
    }

    public static LoadProfileTypeInfo from(LoadProfileType loadProfileType) {
        LoadProfileTypeInfo info = new LoadProfileTypeInfo();
        info.name = loadProfileType.getName();
        info.obisCode = loadProfileType.getObisCode();
        info.timeDuration=loadProfileType.getInterval();
        return info;
    }

    public static List<LoadProfileTypeInfo> from(List<LoadProfileType> loadProfileTypes) {
        List<LoadProfileTypeInfo> loadProfileTypeInfos = new ArrayList<>(loadProfileTypes.size());
        for (LoadProfileType loadProfileType : loadProfileTypes) {
            loadProfileTypeInfos.add(LoadProfileTypeInfo.from(loadProfileType));
        }
        return loadProfileTypeInfos;
    }

}