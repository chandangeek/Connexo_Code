package com.energyict.mdc.device.data.rest.impl;

import java.util.Date;

import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.device.data.rest.BaseComTaskExecutionInfo;
import com.energyict.mdc.device.data.rest.DeviceConnectionTaskInfo.ConnectionStrategyInfo;

public class DeviceComTaskExecutionInfo extends BaseComTaskExecutionInfo {
    
    public Long id;
    public IdWithNameInfo comTask;
    public Date plannedDate;
    public String connectionMethod;
    public ConnectionStrategyInfo connectionStrategy;
    public boolean isOnHold;

}
