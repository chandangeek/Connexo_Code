package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.LoadProfileReading;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bvn on 8/1/14.
 */
public class LoadProfileDataInfo {
    public Map<String, List<ChannelDataInfo>> channelData = new HashMap<>();


    public static LoadProfileDataInfo from(List<String> channels, List<LoadProfileReading> channelData) {
        LoadProfileDataInfo info = new LoadProfileDataInfo();
        for (String channel : channels) {
            info.channelData.put(channel, new ArrayList<ChannelDataInfo>());
        }
        for (LoadProfileReading loadProfileReading : channelData) {
            for (Map.Entry<Channel, BigDecimal> entry : loadProfileReading.getChannelValues()) {
                ChannelDataInfo channelDataInfo = new ChannelDataInfo();
                channelDataInfo.intervalStart=loadProfileReading.getInterval().getStart().getTime();
                channelDataInfo.intervalEnd=loadProfileReading.getInterval().getEnd().getTime();
                channelDataInfo.value=entry.getValue();
                info.channelData.get(entry.getKey().getChannelSpec().getName()).add(channelDataInfo);
            }
        }

        return info;
    }
}
