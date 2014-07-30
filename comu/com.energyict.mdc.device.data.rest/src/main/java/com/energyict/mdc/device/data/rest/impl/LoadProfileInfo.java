package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.LoadProfile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
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

    public static LoadProfileInfo from(LoadProfile loadProfile) {
        LoadProfileInfo info = new LoadProfileInfo();
        info.id=loadProfile.getId();
        info.name=loadProfile.getLoadProfileSpec().getLoadProfileType().getName();
        info.obisCode=loadProfile.getDeviceObisCode();
        info.interval=new TimeDurationInfo(loadProfile.getInterval());
        info.lastReading=loadProfile.getLastReading();
        info.channels=new ArrayList<>();
        for (Channel channel : loadProfile.getChannels()) {
            info.channels.add(channel.getChannelSpec().getName());
        }
        Collections.sort(info.channels);
        return info;
    }
}
