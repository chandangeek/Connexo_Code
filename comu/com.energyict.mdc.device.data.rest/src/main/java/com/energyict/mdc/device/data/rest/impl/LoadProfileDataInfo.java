package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.ProcessStatus;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.LoadProfileReading;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by bvn on 8/1/14.
 */
public class LoadProfileDataInfo {

    public static List<ChannelIntervalInfo> from(List<LoadProfileReading> loadProfileReadings, Thesaurus thesaurus) {
        List<ChannelIntervalInfo> channelData = new ArrayList<>();
        for (LoadProfileReading loadProfileReading : loadProfileReadings) {
            ChannelIntervalInfo channelIntervalInfo = new ChannelIntervalInfo();
            channelIntervalInfo.intervalStart=loadProfileReading.getInterval().getStart();
            channelIntervalInfo.intervalEnd=loadProfileReading.getInterval().getEnd();
            channelIntervalInfo.readingTime=loadProfileReading.getReadingTime();
            channelIntervalInfo.intervalFlags=new ArrayList<>();
            for (ProcessStatus.Flag flag : loadProfileReading.getFlags()) {
                channelIntervalInfo.intervalFlags.add(thesaurus.getString(flag.name(), flag.name()));
            }

            for (Map.Entry<Channel, BigDecimal> entry : loadProfileReading.getChannelValues()) {
                channelIntervalInfo.channelData.add(new ChannelDataInfo(entry.getKey().getChannelSpec().getName(), entry.getValue()));
            }
            channelData.add(channelIntervalInfo);
        }
        return channelData;
    }
}

class ChannelIntervalInfo {
    public Date intervalStart;
    public Date intervalEnd;
    public List<ChannelDataInfo> channelData = new ArrayList<>();
    public Date readingTime;
    public List<String> intervalFlags;
}

class ChannelDataInfo {
    public String name;
    public BigDecimal value;

    ChannelDataInfo() {
    }

    public ChannelDataInfo(String name, BigDecimal value) {
        this.name = name;
        this.value = value;
    }
}

