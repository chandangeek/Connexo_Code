package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.MeasurementType;
import com.energyict.mdc.masterdata.rest.LocalizedTimeDuration;
import com.energyict.mdc.masterdata.rest.RegisterTypeInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoadProfileTypeOnDeviceTypeInfo {

    public long id;
    public String name;
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode obisCode;
    @XmlJavaTypeAdapter(LocalizedTimeDuration.Adapter.class)
    public TimeDuration timeDuration;
    public List<RegisterTypeInfo> registerTypes;
    public Boolean isLinkedToActiveDeviceConf;
    public DeviceTypeCustomPropertySetInfo customPropertySet;
    public long version;
    public VersionInfo<Long> parent;

    public LoadProfileTypeOnDeviceTypeInfo() {
    }

    public static List<LoadProfileTypeOnDeviceTypeInfo> from(Iterable<? extends LoadProfileType> loadProfileTypes, DeviceType deviceType) {
        List<LoadProfileTypeOnDeviceTypeInfo> loadProfileTypeInfos = new ArrayList<>();
        for (LoadProfileType loadProfileType : loadProfileTypes) {
            loadProfileTypeInfos.add(new LoadProfileTypeOnDeviceTypeInfo(loadProfileType, deviceType));
        }
        return loadProfileTypeInfos;
    }

    public LoadProfileTypeOnDeviceTypeInfo(LoadProfileType loadProfileType, DeviceType deviceType) {
        this.id = loadProfileType.getId();
        this.name = loadProfileType.getName();
        this.obisCode = loadProfileType.getObisCode();
        this.timeDuration = loadProfileType.getInterval();
        this.registerTypes = new ArrayList<>(loadProfileType.getChannelTypes().size());
        for (MeasurementType measurementType : loadProfileType.getChannelTypes()) {
            this.registerTypes.add(new RegisterTypeInfo(measurementType, false, true));
        }
        this.version = loadProfileType.getVersion();
        this.parent = new VersionInfo<>(deviceType.getId(), deviceType.getVersion());
        Optional<RegisteredCustomPropertySet> registeredCustomPropertySet = deviceType.getLoadProfileTypeCustomPropertySet(loadProfileType);
        if (registeredCustomPropertySet.isPresent()) {
            this.customPropertySet = new DeviceTypeCustomPropertySetInfo(registeredCustomPropertySet.get());
        }
    }
}