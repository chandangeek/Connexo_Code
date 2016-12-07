package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.energyict.mdc.device.configuration.rest.DeviceConfigurationIdInfo;

import java.util.List;

public class ComTaskExecutionInfo extends BaseComTaskExecutionInfo {

    public IdWithNameInfo comTask;
    public IdWithNameInfo device;
    public DeviceConfigurationIdInfo deviceConfiguration;
    public IdWithNameInfo deviceType;
    public ConnectionTaskInfo connectionTask;
    public boolean alwaysExecuteOnInbound;
    public long sessionId;

}