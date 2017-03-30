/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.energyict.mdc.device.configuration.rest.DeviceConfigurationIdInfo;

public class ComTaskExecutionInfo extends BaseComTaskExecutionInfo {

    public IdWithNameInfo comTask;
    public IdWithNameInfo device;
    public DeviceConfigurationIdInfo deviceConfiguration;
    public IdWithNameInfo deviceType;
    public ConnectionTaskInfo connectionTask;
    public boolean alwaysExecuteOnInbound;
    public long sessionId;

}