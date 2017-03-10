/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.usagepoint.data.rest.impl;

import java.util.List;

/**
 * Represents usage point's register
 */
public class UsagePointRegisterInfo extends AbstractUsagePointChannelInfo {

    /**
     * Date of last data from latest meter activation.
     */
    public Long measurementTime;
    public List<UsagePointDeviceChannelInfo> deviceRegisters;
}
