package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.IntervalInfo;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.LoadProfileReading;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bvn on 8/1/14.
 */
public class LoadProfileDataInfo {
    public IntervalInfo interval;
    public Map<Long, BigDecimal> channelData = new HashMap<>();
    public Date readingTime;
    public List<String> intervalFlags;

    public static List<LoadProfileDataInfo> from(List<LoadProfileReading> loadProfileReadings, Thesaurus thesaurus) {
        List<LoadProfileDataInfo> channelData = new ArrayList<>();
        for (LoadProfileReading loadProfileReading : loadProfileReadings) {
            LoadProfileDataInfo channelIntervalInfo = new LoadProfileDataInfo();
            channelIntervalInfo.interval=IntervalInfo.from(loadProfileReading.getInterval());
            channelIntervalInfo.readingTime=loadProfileReading.getReadingTime();
            channelIntervalInfo.intervalFlags=new ArrayList<>();
            for (ProfileStatus.Flag flag : loadProfileReading.getFlags()) {
                channelIntervalInfo.intervalFlags.add(thesaurus.getString(flag.name(), flag.name()));
            }

            for (Map.Entry<Channel, BigDecimal> entry : loadProfileReading.getChannelValues()) {
                channelIntervalInfo.channelData.put(entry.getKey().getId(), entry.getValue());
            }
            channelData.add(channelIntervalInfo);
        }
        return channelData;
    }
}

