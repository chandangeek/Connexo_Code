package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.LoadProfile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

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
    public Date lastReading;
    public List<ChannelInfo> channels;

    public static LoadProfileInfo from(LoadProfile loadProfile) {
        LoadProfileInfo info = createLoadProfileInfo(loadProfile);
        List<Channel> channels = loadProfile.getChannels();
        Collections.sort(channels, CHANNEL_COMPARATOR);
        info.channels=ChannelInfo.from(channels);
        return info;
    }

    private static LoadProfileInfo createLoadProfileInfo(LoadProfile loadProfile) {
        LoadProfileInfo info = new LoadProfileInfo();
        info.id=loadProfile.getId();
        info.name=loadProfile.getLoadProfileSpec().getLoadProfileType().getName();
        info.obisCode=loadProfile.getDeviceObisCode();
        info.interval=new TimeDurationInfo(loadProfile.getInterval());
        info.lastReading=loadProfile.getLastReading();
        return info;
    }

    public static List<LoadProfileInfo> from(List<LoadProfile> loadProfiles) {
        List<LoadProfileInfo> loadProfileInfos = new ArrayList<>(loadProfiles.size());
        for (LoadProfile loadProfile : loadProfiles) {
            LoadProfileInfo loadProfileInfo = createLoadProfileInfo(loadProfile);
            List<Channel> channels = loadProfile.getChannels();
            Collections.sort(channels, CHANNEL_COMPARATOR);
            loadProfileInfo.channels=ChannelInfo.asSimpleInfoFrom(channels);
            loadProfileInfos.add(loadProfileInfo);
        }
        return loadProfileInfos;
    }
}
