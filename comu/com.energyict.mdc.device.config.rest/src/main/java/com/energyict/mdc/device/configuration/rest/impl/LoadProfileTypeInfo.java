package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.device.config.LoadProfileType;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.codehaus.jackson.annotate.JsonProperty;

@XmlRootElement
public class LoadProfileTypeInfo {

    @JsonProperty("name")
    public String name;
    @JsonProperty("obisCode")
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode obisCode;
    @JsonProperty("timeDuration")
    @XmlJavaTypeAdapter(TimeDurationAdapter.class)
    public TimeDuration timeDuration;
    @JsonProperty("inUse")
    public boolean inUse;

    public LoadProfileTypeInfo() {
    }

    public static LoadProfileTypeInfo from(LoadProfileType loadProfileType) {
        LoadProfileTypeInfo info = new LoadProfileTypeInfo();
        info.name = loadProfileType.getName();
        info.obisCode = loadProfileType.getObisCode();
        info.timeDuration=loadProfileType.getInterval();
        info.inUse = loadProfileType.isInUse();
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
