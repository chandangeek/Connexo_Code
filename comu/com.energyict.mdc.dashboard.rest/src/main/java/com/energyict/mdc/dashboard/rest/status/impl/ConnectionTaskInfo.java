package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.energyict.mdc.device.configuration.rest.DeviceConfigurationIdInfo;
import com.energyict.mdc.device.data.rest.DeviceConnectionTaskInfo;

public class ConnectionTaskInfo extends DeviceConnectionTaskInfo {
    public IdWithNameInfo device;
    public DeviceConfigurationIdInfo deviceConfiguration;
    public IdWithNameInfo deviceType;
}
