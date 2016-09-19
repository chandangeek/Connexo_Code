package com.energyict.mdc.masterdata.rest;

import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.common.ObisCode;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.MeasurementType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoadProfileTypeInfo {

    public long id;
    public String name;
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode obisCode;
    @XmlJavaTypeAdapter(LocalizedTimeDuration.Adapter.class)
    public TimeDuration timeDuration;
    public List<RegisterTypeInfo> registerTypes;
    public Boolean isLinkedToActiveDeviceConf;
    public long version;
    public VersionInfo<Long> parent;

    public LoadProfileTypeInfo() {
    }

    @Deprecated
    public static List<LoadProfileTypeInfo> from(Iterable<? extends LoadProfileType> loadProfileTypes) {
        List<LoadProfileTypeInfo> loadProfileTypeInfos = new ArrayList<>();
        for (LoadProfileType loadProfileType : loadProfileTypes) {
            loadProfileTypeInfos.add(LoadProfileTypeInfo.from(loadProfileType, null));
        }
        return loadProfileTypeInfos;
    }

    @Deprecated
    public static LoadProfileTypeInfo from(LoadProfileType loadProfileType, Boolean isInUse) {
        LoadProfileTypeInfo info = new LoadProfileTypeInfo();
        info.id = loadProfileType.getId();
        info.name = loadProfileType.getName();
        info.obisCode = loadProfileType.getObisCode();
        info.timeDuration=loadProfileType.getInterval();

        info.registerTypes = new ArrayList<>(loadProfileType.getChannelTypes().size());
        for (MeasurementType measurementType : loadProfileType.getChannelTypes()) {
            info.registerTypes.add(new RegisterTypeInfo(measurementType, false, true));
        }

        info.isLinkedToActiveDeviceConf = isInUse;
        info.version = loadProfileType.getVersion();

        return info;
    }
}