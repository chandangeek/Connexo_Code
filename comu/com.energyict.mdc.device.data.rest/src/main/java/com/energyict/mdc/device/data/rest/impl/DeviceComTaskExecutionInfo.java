/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.energyict.mdc.device.data.rest.BaseComTaskExecutionInfo;
import com.energyict.mdc.device.data.rest.DeviceConnectionTaskInfo.ConnectionStrategyInfo;

import java.time.Instant;

public class DeviceComTaskExecutionInfo extends BaseComTaskExecutionInfo {

    public Long id;
    public IdWithNameInfo comTask;
    public Instant plannedDate;
    public String connectionMethod;
    public ConnectionStrategyInfo connectionStrategy;
    public boolean isOnHold;

}