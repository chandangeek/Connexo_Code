package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.rest.ReadingTypeInfo;

import java.util.List;

public class UsagePointChannelInfo {
    public Long dataUntil;
    public TimeDurationInfo interval;
    public ReadingTypeInfo readingType;
    public List<UsagePointDeviceChannelInfo> deviceChannels;
}
