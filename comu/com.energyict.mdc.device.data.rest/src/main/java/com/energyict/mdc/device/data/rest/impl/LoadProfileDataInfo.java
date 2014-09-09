package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationEvaluator;
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
    public Map<Long, ValidationInfo> channelValidationData = new HashMap<>();
    public Date readingTime;
    public List<String> intervalFlags;
    public boolean validationActive;

    public static List<LoadProfileDataInfo> from(List<? extends LoadProfileReading> loadProfileReadings, Thesaurus thesaurus, Clock clock, ValidationEvaluator evaluator) {
        List<LoadProfileDataInfo> channelData = new ArrayList<>();
        for (LoadProfileReading loadProfileReading : loadProfileReadings) {
            channelData.add(getLoadProfileDataInfo(loadProfileReading, thesaurus, clock, evaluator));
        }
        return channelData;
    }

    private static LoadProfileDataInfo getLoadProfileDataInfo(LoadProfileReading loadProfileReading, Thesaurus thesaurus, Clock clock, ValidationEvaluator evaluator) {
        LoadProfileDataInfo channelIntervalInfo = new LoadProfileDataInfo();
        channelIntervalInfo.interval= IntervalInfo.from(loadProfileReading.getInterval());
        channelIntervalInfo.readingTime=loadProfileReading.getReadingTime();
        channelIntervalInfo.intervalFlags=new ArrayList<>();
        for (ProfileStatus.Flag flag : loadProfileReading.getFlags()) {
            channelIntervalInfo.intervalFlags.add(thesaurus.getString(flag.name(), flag.name()));
        }

        for (Map.Entry<Channel, BigDecimal> entry : loadProfileReading.getChannelValues().entrySet()) {
            channelIntervalInfo.channelData.put(entry.getKey().getId(), entry.getValue());
        }

        for (Map.Entry<Channel, DataValidationStatus> entry : loadProfileReading.getChannelValidationStates().entrySet()) {
            channelIntervalInfo.channelValidationData.put(entry.getKey().getId(), new ValidationInfo(entry.getValue(), evaluator));
        }

        for (Channel channel : loadProfileReading.getChannelValues().keySet()) {
            channelIntervalInfo.validationActive |= channel.getDevice().forValidation().isValidationActive(channel, clock.now());
        }

        return channelIntervalInfo;
    }
}

