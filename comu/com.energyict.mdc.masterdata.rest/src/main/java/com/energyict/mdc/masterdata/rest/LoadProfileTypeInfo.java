package com.energyict.mdc.masterdata.rest;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.RegisterMapping;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoadProfileTypeInfo {

    public long id;
    public String name;
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode obisCode;
    @XmlJavaTypeAdapter(LocalizedTimeDuration.Adapter.class)
    public TimeDuration timeDuration;
    @JsonProperty("measurementTypes")
    public List<RegisterMappingInfo> registerMappings;
    public Boolean isLinkedToActiveDeviceConf;

    public LoadProfileTypeInfo() {
    }

    public static List<LoadProfileTypeInfo> from(Collection<LoadProfileType> loadProfileTypes) {
        List<LoadProfileTypeInfo> loadProfileTypeInfos = new ArrayList<>(loadProfileTypes.size());
        for (LoadProfileType loadProfileType : loadProfileTypes) {
            loadProfileTypeInfos.add(LoadProfileTypeInfo.from(loadProfileType, null));
        }
        return loadProfileTypeInfos;
    }

    public static LoadProfileTypeInfo from(LoadProfileType loadProfileType, Boolean isInUse) {
        LoadProfileTypeInfo info = new LoadProfileTypeInfo();
        info.id = loadProfileType.getId();
        info.name = loadProfileType.getName();
        info.obisCode = loadProfileType.getObisCode();
        info.timeDuration=loadProfileType.getInterval();

        info.registerMappings = new ArrayList<>(loadProfileType.getRegisterMappings().size());
        for (RegisterMapping registerMapping : loadProfileType.getRegisterMappings()) {
            info.registerMappings.add(new RegisterMappingInfo(registerMapping, false));
        }

        info.isLinkedToActiveDeviceConf = isInUse;
        return info;
    }
}