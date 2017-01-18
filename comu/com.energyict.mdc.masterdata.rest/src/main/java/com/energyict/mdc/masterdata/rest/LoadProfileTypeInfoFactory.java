package com.energyict.mdc.masterdata.rest;

import com.energyict.mdc.common.interval.Temporals;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.MeasurementType;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class LoadProfileTypeInfoFactory {

    private final RegisterTypeInfoFactory registerTypeInfoFactory;

    @Inject
    public LoadProfileTypeInfoFactory(RegisterTypeInfoFactory registerTypeInfoFactory) {
        this.registerTypeInfoFactory = registerTypeInfoFactory;
    }

    public List<LoadProfileTypeInfo> from(Iterable<? extends LoadProfileType> loadProfileTypes) {
        List<LoadProfileTypeInfo> loadProfileTypeInfos = new ArrayList<>();
        for (LoadProfileType loadProfileType : loadProfileTypes) {
            loadProfileTypeInfos.add(this.from(loadProfileType, null));
        }
        return loadProfileTypeInfos;
    }

    public LoadProfileTypeInfo from(LoadProfileType loadProfileType, Boolean isInUse) {
        LoadProfileTypeInfo info = new LoadProfileTypeInfo();
        info.id = loadProfileType.getId();
        info.name = loadProfileType.getName();
        info.obisCode = loadProfileType.getObisCode();
        info.timeDuration= Temporals.toTimeDuration(loadProfileType.interval());

        info.registerTypes = new ArrayList<>(loadProfileType.getChannelTypes().size());
        for (MeasurementType measurementType : loadProfileType.getChannelTypes()) {
            info.registerTypes.add(this.registerTypeInfoFactory.asInfo(measurementType, false, true));
        }

        info.isLinkedToActiveDeviceConf = isInUse;
        info.version = loadProfileType.getVersion();

        return info;
    }
}