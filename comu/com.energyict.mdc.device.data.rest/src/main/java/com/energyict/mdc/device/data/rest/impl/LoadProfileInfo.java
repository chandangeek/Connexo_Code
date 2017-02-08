/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * JSON structure for load profiles
 * Created by bvn on 7/28/14.
 */
public class LoadProfileInfo {

    public static final Comparator<Channel> CHANNEL_COMPARATOR = new ChannelComparator();
    public long id;
    public String name;
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode obisCode;
    public TimeDurationInfo interval; // the interval definition of the load profile
    public Instant lastReading;
    public List<ChannelInfo> channels;
    public long version;
    public VersionInfo<String> parent;

    // optionally filled if requesting details
    public DetailedValidationInfo validationInfo;

    public static LoadProfileInfo from(LoadProfile loadProfile, ChannelInfoFactory channelInfoFactory) {
        LoadProfileInfo info = createLoadProfileInfo(loadProfile);
        List<Channel> channels = loadProfile.getChannels();
        Collections.sort(channels, CHANNEL_COMPARATOR);
        info.channels = channelInfoFactory.from(channels);
        return info;
    }

    private static LoadProfileInfo createLoadProfileInfo(LoadProfile loadProfile) {
        LoadProfileInfo info = new LoadProfileInfo();
        info.id = loadProfile.getId();
        info.name = loadProfile.getLoadProfileSpec().getLoadProfileType().getName();
        info.obisCode = loadProfile.getDeviceObisCode();
        info.interval = new TimeDurationInfo(loadProfile.getInterval());
        info.lastReading = loadProfile.getLastReading().orElse(null);
        info.version = loadProfile.getVersion();
        Device device = loadProfile.getDevice();
        info.parent = new VersionInfo<>(device.getName(), device.getVersion());
        return info;
    }

    public static List<LoadProfileInfo> from(List<LoadProfile> loadProfiles, ChannelInfoFactory channelInfoFactory) {
        List<LoadProfileInfo> loadProfileInfos = new ArrayList<>(loadProfiles.size());
        for (LoadProfile loadProfile : loadProfiles) {
            LoadProfileInfo loadProfileInfo = createLoadProfileInfo(loadProfile);
            List<Channel> channels = loadProfile.getChannels();
            Collections.sort(channels, CHANNEL_COMPARATOR);
            loadProfileInfo.channels = channelInfoFactory.asSimpleInfoFrom(channels);
            loadProfileInfos.add(loadProfileInfo);
        }
        return loadProfileInfos;
    }
}
