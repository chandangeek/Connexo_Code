/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.energyict.mdc.device.configuration.rest.DeviceConfigurationIdInfo;
import com.energyict.mdc.device.data.rest.SuccessIndicatorInfo;

import java.time.Instant;

/**
 * Created by bvn on 10/3/14.
 */
class ComSessionInfo {
    public long id;
    public IdWithNameInfo connectionMethod;
    public Instant startedOn;
    public Instant finishedOn;
    public Long durationInSeconds;
    public String direction;
    public String connectionType;
    public IdWithNameInfo comServer;
    public String comPort;
    public String status;
    public SuccessIndicatorInfo result;
    public ComTaskCountInfo comTaskCount;
    public boolean isDefault;
    public IdWithNameInfo device;
    public IdWithNameInfo deviceType;
    public DeviceConfigurationIdInfo deviceConfiguration;
}