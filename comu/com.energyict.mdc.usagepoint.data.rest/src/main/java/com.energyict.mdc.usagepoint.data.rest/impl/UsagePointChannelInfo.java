/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.time.rest.TimeDurationInfo;;

import java.util.List;

/**
 * Represents usage point channel info
 */
public class UsagePointChannelInfo extends AbstractUsagePointChannelInfo {
    public TimeDurationInfo interval;
    public List<UsagePointDeviceChannelInfo> deviceChannels;
    public String flowUnit;
}
