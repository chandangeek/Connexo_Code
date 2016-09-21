package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.topology.DataLoggerChannelUsage;

import java.util.List;
import java.util.Optional;

/**
 * Copyrights EnergyICT
 * Date: 11.07.16
 * Time: 15:28
 */
public class ChannelHistoryInfo {
    public Long startDate;
    public Long endDate;
    public String deviceName;
    public Long channelId;

    public static ChannelHistoryInfo from(DataLoggerChannelUsage dataLoggerChannelUsage) {
        ChannelHistoryInfo channelHistoryInfo = new ChannelHistoryInfo();
        List<? extends ReadingType> slaveChannelReadingTypes = dataLoggerChannelUsage.getSlaveChannel().getReadingTypes();
        Optional<Channel> slaveChannel = dataLoggerChannelUsage.getDataLoggerReference()
                .getOrigin()
                .getChannels()
                .stream()
                .filter((c) -> slaveChannelReadingTypes.contains(c.getReadingType()))
                .findFirst();
        slaveChannel.ifPresent(channel -> {
            channelHistoryInfo.channelId = channel.getId();
            channelHistoryInfo.deviceName = dataLoggerChannelUsage.getDataLoggerReference().getOrigin().getName();
            if (dataLoggerChannelUsage.getRange().hasLowerBound()) {
                channelHistoryInfo.startDate = dataLoggerChannelUsage.getRange().lowerEndpoint().toEpochMilli();
            }
            if (dataLoggerChannelUsage.getRange().hasUpperBound()) {
                channelHistoryInfo.endDate = dataLoggerChannelUsage.getRange().upperEndpoint().toEpochMilli();
            }
        });
        return channelHistoryInfo;
    }
}
