/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.rest.response;

import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.energyict.mdc.issue.datacollection.entity.IssueDataCollection;

public class CommunicationFailedIssueInfo<T extends DeviceInfo> extends DataCollectionIssueInfo<T>  {
    public Long comTaskId;
    public Long comTaskSessionId;

    public CommunicationTaskIssueInfo communicationTask;

    public CommunicationFailedIssueInfo(IssueDataCollection issue, Class<T> deviceInfoClass){
        super(issue, deviceInfoClass);
    }

}
