package com.energyict.mdc.issue.datacollection.rest.response;

import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.issue.datacollection.entity.IssueDataCollection;

import java.util.List;

public class ConnectionFailedIssueInfo<T extends DeviceInfo> extends DataCollectionIssueInfo<T> {

    public Long comSessionId;
    public Long connectionTaskId;

    public IssueConnectionTaskInfo connectionTask;

    public ConnectionFailedIssueInfo(IssueDataCollection issue, Class<T> deviceInfoClass){
        super(issue, deviceInfoClass);
    }
}
