package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.device.data.rest.SuccessIndicatorInfo;
import java.util.Date;

/**
 * Created by bvn on 10/3/14.
 */
class ComSessionInfo {
    public String connectionMethod;
    public Date startedOn;
    public Date finishedOn;
    public Long durationInSeconds;
    public String direction;
    public String connectionType;
    public IdWithNameInfo comServer;
    public String comPort;
    public String status;
    public SuccessIndicatorInfo result;
    public ComTaskCountInfo comTaskCount;
    public boolean isDefault;
}

class ComTaskCountInfo {
    public long numberOfSuccessfulTasks;
    public long numberOfFailedTasks;
    public long numberOfIncompleteTasks;
}

