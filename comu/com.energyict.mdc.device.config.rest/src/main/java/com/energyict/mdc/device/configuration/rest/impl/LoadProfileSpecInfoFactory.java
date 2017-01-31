/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.LoadProfileSpec;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class LoadProfileSpecInfoFactory {

    private final ChannelSpecInfoFactory channelSpecInfoFactory;

    @Inject
    public LoadProfileSpecInfoFactory(ChannelSpecInfoFactory channelSpecInfoFactory) {
        this.channelSpecInfoFactory = channelSpecInfoFactory;
    }

    public List<LoadProfileSpecInfo> from(List<LoadProfileSpec> loadProfileSpecs) {
        List<LoadProfileSpecInfo> loadProfileTypeInfos = new ArrayList<>(loadProfileSpecs.size());
        for (LoadProfileSpec loadProfileSpec : loadProfileSpecs) {
            loadProfileTypeInfos.add(from(loadProfileSpec, null));
        }
        return loadProfileTypeInfos;
    }

    public LoadProfileSpecInfo from(LoadProfileSpec loadProfileSpec, List<ChannelSpec> channelSpecs) {
        LoadProfileSpecInfo info = new LoadProfileSpecInfo();
        info.id = loadProfileSpec.getId();
        info.name = loadProfileSpec.getLoadProfileType().getName();
        info.obisCode = loadProfileSpec.getObisCode();
        info.overruledObisCode = loadProfileSpec.getDeviceObisCode();
        info.timeDuration = loadProfileSpec.getInterval();
        info.channels = channelSpecInfoFactory.asInfoList(channelSpecs);
        info.version = loadProfileSpec.getVersion();
        DeviceConfiguration deviceConfiguration = loadProfileSpec.getDeviceConfiguration();
        info.parent = new VersionInfo<>(deviceConfiguration.getId(), deviceConfiguration.getVersion());
        return info;
    }
}