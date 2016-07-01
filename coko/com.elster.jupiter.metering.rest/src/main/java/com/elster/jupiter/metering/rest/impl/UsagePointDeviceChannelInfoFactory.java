package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.rest.util.IdWithNameInfo;

public class UsagePointDeviceChannelInfoFactory {
    UsagePointDeviceChannelInfo from(MeterActivation meterActivation, Channel channel) {
        UsagePointDeviceChannelInfo deviceChannelInfo = new UsagePointDeviceChannelInfo();
        Meter meter = meterActivation.getMeter().get();
        meterActivation.getChannelsContainer().getChannels().contains(channel);

        deviceChannelInfo.from = meterActivation.getStart().toEpochMilli();
        deviceChannelInfo.mRID = meter.getMRID();
        deviceChannelInfo.channel = new IdWithNameInfo(
                meterActivation.getChannelsContainer().getChannels().contains(channel) ? channel.getId() : null,
                channel.getMainReadingType().getFullAliasName());

        return deviceChannelInfo;
    }
}