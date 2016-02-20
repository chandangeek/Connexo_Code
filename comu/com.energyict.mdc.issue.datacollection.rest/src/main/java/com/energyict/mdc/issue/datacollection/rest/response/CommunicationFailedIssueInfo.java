package com.energyict.mdc.issue.datacollection.rest.response;

import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.issue.datacollection.entity.IssueDataCollection;

import java.util.List;

public class CommunicationFailedIssueInfo<T extends DeviceInfo> extends DataCollectionIssueInfo<T>  {
    public Long comTaskId;
    public Long comTaskSessionId;

    public IssueCommunicationTaskInfo communicationTask;

    public CommunicationFailedIssueInfo(IssueDataCollection issue, Class<T> deviceInfoClass){
        super(issue, deviceInfoClass);
    }

}
