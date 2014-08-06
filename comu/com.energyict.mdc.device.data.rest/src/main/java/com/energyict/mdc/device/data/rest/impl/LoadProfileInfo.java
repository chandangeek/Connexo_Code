package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LoadProfileReading;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * JSON structure for load profiles
 * Created by bvn on 7/28/14.
 */
public class LoadProfileInfo {
    public long id;
    public String name;
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode obisCode;
    public TimeDurationInfo interval; // the interval definition of the load profile
    public Date lastReading;
    public List<String> channels;
    public List<ChannelIntervalInfo> loadProfileData;

    public static LoadProfileInfo from(LoadProfile loadProfile) {
        LoadProfileInfo info = new LoadProfileInfo();
        info.id=loadProfile.getId();
        info.name=loadProfile.getLoadProfileSpec().getLoadProfileType().getName();
        info.obisCode=loadProfile.getDeviceObisCode();
        info.interval=new TimeDurationInfo(loadProfile.getInterval());
        info.lastReading=loadProfile.getLastReading();
        info.channels=new ArrayList<>();
        for (Channel channel : loadProfile.getChannels()) {
            info.channels.add(channel.getChannelSpec().getName()); // channel name is the id
        }
        Collections.sort(info.channels);
        return info;
    }

}
