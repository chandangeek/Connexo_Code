package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.IntervalInfo;
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
public class ChannelDataInfo {
    public IntervalInfo interval;
    public Date readingTime;
    public List<String> intervalFlags;
    public BigDecimal value;

    public static List<ChannelDataInfo> from(List<? extends LoadProfileReading> loadProfileReadings, Thesaurus thesaurus) {
        List<ChannelDataInfo> channelData = new ArrayList<>();
        for (LoadProfileReading loadProfileReading : loadProfileReadings) {
            ChannelDataInfo channelIntervalInfo = new ChannelDataInfo();
            channelIntervalInfo.interval=IntervalInfo.from(loadProfileReading.getInterval());
            channelIntervalInfo.readingTime=loadProfileReading.getReadingTime();
            channelIntervalInfo.intervalFlags=new ArrayList<>();
            for (ProfileStatus.Flag flag : loadProfileReading.getFlags()) {
                channelIntervalInfo.intervalFlags.add(thesaurus.getString(flag.name(), flag.name()));
            }
            for (Map.Entry<Channel, BigDecimal> entry : loadProfileReading.getChannelValues().entrySet()) {
                channelIntervalInfo.value=entry.getValue(); // There can be only one channel (or no channel at all if the channel has no dta for this interval)
            }
            channelData.add(channelIntervalInfo);
        }
        return channelData;
    }
}

