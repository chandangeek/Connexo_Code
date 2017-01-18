package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.common.interval.Temporals;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.MeasurementType;
import com.energyict.mdc.masterdata.rest.RegisterTypeInfoFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LoadProfileTypeOnDeviceTypeInfoFactory {

    private final RegisterTypeInfoFactory registerTypeInfoFactory;

    @Inject
    public LoadProfileTypeOnDeviceTypeInfoFactory(RegisterTypeInfoFactory registerTypeInfoFactory) {
        this.registerTypeInfoFactory = registerTypeInfoFactory;
    }

    public List<LoadProfileTypeOnDeviceTypeInfo> from(Iterable<? extends LoadProfileType> loadProfileTypes, DeviceType deviceType) {
        List<LoadProfileTypeOnDeviceTypeInfo> loadProfileTypeInfos = new ArrayList<>();
        for (LoadProfileType loadProfileType : loadProfileTypes) {
            loadProfileTypeInfos.add(this.from(loadProfileType, deviceType));
        }
        return loadProfileTypeInfos;
    }

    public LoadProfileTypeOnDeviceTypeInfo from(LoadProfileType loadProfileType, DeviceType deviceType) {
        LoadProfileTypeOnDeviceTypeInfo info = new LoadProfileTypeOnDeviceTypeInfo();
        info.id = loadProfileType.getId();
        info.name = loadProfileType.getName();
        info.obisCode = loadProfileType.getObisCode();
        info.timeDuration = Temporals.toTimeDuration(loadProfileType.interval());
        info.registerTypes = new ArrayList<>(loadProfileType.getChannelTypes().size());
        for (MeasurementType measurementType : loadProfileType.getChannelTypes()) {
            info.registerTypes.add(registerTypeInfoFactory.asInfo(measurementType, false, true));
        }
        info.version = loadProfileType.getVersion();
        info.parent = new VersionInfo<>(deviceType.getId(), deviceType.getVersion());
        Optional<RegisteredCustomPropertySet> registeredCustomPropertySet = deviceType.getLoadProfileTypeCustomPropertySet(loadProfileType);
        if (registeredCustomPropertySet.isPresent()) {
            info.customPropertySet = new DeviceTypeCustomPropertySetInfo(registeredCustomPropertySet.get());
        }
        return info;
    }
}