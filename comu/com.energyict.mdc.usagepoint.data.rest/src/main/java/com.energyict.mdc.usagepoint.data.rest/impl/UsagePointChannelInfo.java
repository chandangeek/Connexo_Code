package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.energyict.mdc.common.rest.TimeDurationInfo;

import java.util.List;

public class UsagePointChannelInfo {
    public long id;
    public Long dataUntil;
    public TimeDurationInfo interval;
    public ReadingTypeInfo readingType;
    public List<UsagePointDeviceChannelInfo> deviceChannels;
}
