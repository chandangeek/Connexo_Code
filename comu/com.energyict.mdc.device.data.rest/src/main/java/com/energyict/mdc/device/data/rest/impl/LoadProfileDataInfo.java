package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.JsonInstantAdapter;
import com.elster.jupiter.validation.DataValidationStatus;
import com.energyict.mdc.common.rest.IntervalInfo;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceValidation;
import com.energyict.mdc.device.data.LoadProfileReading;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bvn on 8/1/14.
 */
public class LoadProfileDataInfo {
    public IntervalInfo interval;
    public Map<Long, String> channelData = new HashMap<>();
    public Map<Long, ValidationInfo> channelValidationData = new HashMap<>();
    public Instant readingTime;
    public List<String> intervalFlags;
    public boolean validationActive;

    public static List<LoadProfileDataInfo> from(Device device, List<? extends LoadProfileReading> loadProfileReadings, Thesaurus thesaurus, Clock clock, List<Channel> channels) {
        List<LoadProfileDataInfo> channelData = new ArrayList<>();
        DeviceValidation deviceValidation = device.forValidation();
        for (LoadProfileReading loadProfileReading : loadProfileReadings) {
            channelData.add(getLoadProfileDataInfo(loadProfileReading, deviceValidation, thesaurus, clock, channels));
        }
        return channelData;
    }

    private static LoadProfileDataInfo getLoadProfileDataInfo(LoadProfileReading loadProfileReading, DeviceValidation deviceValidation, Thesaurus thesaurus, Clock clock, List<Channel> channels) {
        LoadProfileDataInfo channelIntervalInfo = new LoadProfileDataInfo();
        channelIntervalInfo.interval= IntervalInfo.from(loadProfileReading.getInterval());
        channelIntervalInfo.readingTime=loadProfileReading.getReadingTime();
        channelIntervalInfo.intervalFlags=new ArrayList<>();
        for (ProfileStatus.Flag flag : loadProfileReading.getFlags()) {
            channelIntervalInfo.intervalFlags.add(thesaurus.getString(flag.name(), flag.name()));
        }
        if (loadProfileReading.getChannelValues().isEmpty()){
          for(Channel channel: channels) {
              channelIntervalInfo.channelData.put(channel.getId(), null);
          }
        } else {
            for (Map.Entry<Channel, IntervalReadingRecord> entry : loadProfileReading.getChannelValues().entrySet()) {
                Channel channel = entry.getKey();
                BigDecimal value = entry.getValue().getValue();
                if (value != null) {
                    int nbrOfFractionDigits = channel.getChannelSpec().getNbrOfFractionDigits();
                    value = value.setScale(nbrOfFractionDigits, BigDecimal.ROUND_UP);
                }
                channelIntervalInfo.channelData.put(channel.getId(), value != null ? value.toString() : "");
            }
        }

        for (Map.Entry<Channel, DataValidationStatus> entry : loadProfileReading.getChannelValidationStates().entrySet()) {
            channelIntervalInfo.channelValidationData.put(entry.getKey().getId(), new ValidationInfo(entry.getValue(), deviceValidation));
        }

        for (Channel channel : loadProfileReading.getChannelValues().keySet()) {
            channelIntervalInfo.validationActive |= deviceValidation.isValidationActive(channel, clock.instant());
        }

        return channelIntervalInfo;
    }
}

