/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.energyict.mdc.device.configuration.rest.DeviceConfigurationIdInfo;
import com.energyict.mdc.device.data.rest.DeviceConnectionTaskInfo;

public class ConnectionTaskInfo extends DeviceConnectionTaskInfo {
    public IdWithNameInfo device;
    public IdWithNameInfo deviceType;
    public DeviceConfigurationIdInfo deviceConfiguration;
    public ConnectionFunctionInfo connectionFunctionInfo;
}
