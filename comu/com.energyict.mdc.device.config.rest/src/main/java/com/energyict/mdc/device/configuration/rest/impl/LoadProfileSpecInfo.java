package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.masterdata.rest.LocalizedTimeDuration;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoadProfileSpecInfo {

    public long id;
    public String name;
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode obisCode;
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode overruledObisCode;
    @XmlJavaTypeAdapter(LocalizedTimeDuration.Adapter.class)
    public TimeDuration timeDuration;
    public List<ChannelSpecInfo> channels;
    public long version;
    public VersionInfo<Long> parent;

    public LoadProfileSpecInfo() {
    }

    public static List<LoadProfileSpecInfo> from(List<LoadProfileSpec> loadProfileSpecs) {
        List<LoadProfileSpecInfo> loadProfileTypeInfos = new ArrayList<>(loadProfileSpecs.size());
        for (LoadProfileSpec loadProfileSpec : loadProfileSpecs) {
            loadProfileTypeInfos.add(LoadProfileSpecInfo.from(loadProfileSpec, null));
        }
        return loadProfileTypeInfos;
    }

    public static LoadProfileSpecInfo from(LoadProfileSpec loadProfileSpec, List<ChannelSpec> channelSpecs) {
        LoadProfileSpecInfo info = new LoadProfileSpecInfo();
        info.id = loadProfileSpec.getId();
        info.name = loadProfileSpec.getLoadProfileType().getName();
        info.obisCode = loadProfileSpec.getObisCode();
        info.overruledObisCode = loadProfileSpec.getDeviceObisCode();
        info.timeDuration=loadProfileSpec.getInterval();
        info.channels = ChannelSpecInfo.from(channelSpecs);
        info.version = loadProfileSpec.getVersion();
        DeviceConfiguration deviceConfiguration = loadProfileSpec.getDeviceConfiguration();
        info.parent = new VersionInfo<>(deviceConfiguration.getId(), deviceConfiguration.getVersion());
        return info;
    }
}