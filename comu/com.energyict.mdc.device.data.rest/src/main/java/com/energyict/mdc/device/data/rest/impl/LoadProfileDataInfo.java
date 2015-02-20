package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.DataValidationStatus;
import com.energyict.mdc.common.rest.IntervalInfo;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceValidation;
import com.energyict.mdc.device.data.LoadProfileReading;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by bvn on 8/1/14.
 */
public class LoadProfileDataInfo {
    public IntervalInfo interval;
    public Map<Long, String> channelData = new HashMap<>();
    public Map<Long, String> channelCollectedData = new HashMap<>();
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
        channelIntervalInfo.interval = IntervalInfo.from(loadProfileReading.getRange());
        channelIntervalInfo.readingTime = loadProfileReading.getReadingTime();
        channelIntervalInfo.intervalFlags = loadProfileReading
                .getFlags()
                .stream()
                .map(flag -> thesaurus.getString(flag.name(), flag.name()))
                .collect(Collectors.toList());
        if (loadProfileReading.getChannelValues().isEmpty()){
            for (Channel channel : channels) {
                channelIntervalInfo.channelData.put(channel.getId(), null);
            }
            channelIntervalInfo.channelCollectedData = channelIntervalInfo.channelData;
        } else {
            for (Map.Entry<Channel, IntervalReadingRecord> entry : loadProfileReading.getChannelValues().entrySet()) {
                Channel channel = entry.getKey();
                BigDecimal value = getRoundedBigDecimal(entry.getValue().getValue(), channel);
                channelIntervalInfo.channelData.put(channel.getId(), value != null ? value.toString() : "");
                if (channel.getReadingType().isCumulative()){
                    Quantity quantity = entry.getValue().getQuantity(channel.getReadingType());
                    value = getRoundedBigDecimal(quantity != null ?  quantity.getValue() : null, channel);
                    channelIntervalInfo.channelCollectedData.put(channel.getId(), value != null ? value.toString() : "");
                }
            }
        }

        for (Map.Entry<Channel, DataValidationStatus> entry : loadProfileReading.getChannelValidationStates().entrySet()) {
            channelIntervalInfo.channelValidationData.put(entry.getKey().getId(), new ValidationInfo(entry.getValue(), deviceValidation));
        }

        for (Channel channel : channels) {
            if (channelIntervalInfo.channelData.containsKey(channel.getId()) && channelIntervalInfo.channelData.get(channel.getId()) == null
                    && !channelIntervalInfo.channelValidationData.containsKey(channel.getId())) {
                // This means it is a missing value what hasn't been validated( = detected ) yet
                ValidationInfo notValidatedMissing = new ValidationInfo();
                notValidatedMissing.dataValidated = false;
                notValidatedMissing.validationResult = ValidationStatus.NOT_VALIDATED;
                notValidatedMissing.validationRules = Collections.EMPTY_SET;
                channelIntervalInfo.channelValidationData.put(channel.getId(), notValidatedMissing);
            }
        }

        for (Channel channel : loadProfileReading.getChannelValues().keySet()) {
            channelIntervalInfo.validationActive |= deviceValidation.isValidationActive(channel, clock.instant());
        }

        return channelIntervalInfo;
    }

    private static BigDecimal getRoundedBigDecimal(BigDecimal value, Channel channel) {
        return value != null ? value.setScale(channel.getChannelSpec().getNbrOfFractionDigits(), BigDecimal.ROUND_UP) : value;
    }

}