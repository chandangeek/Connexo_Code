package com.energyict.mdc.issue.datacollection.rest.response;

import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.issue.rest.response.issue.IssueInfo;
import com.energyict.mdc.issue.datacollection.entity.IssueDataCollection;

public class DataCollectionIssueInfo extends IssueInfo<DeviceInfo, IssueDataCollection> {

    public String deviceId;
    public Long connectionTaskId;
    public Long comSessionId;
    
    public DataCollectionIssueInfo(IssueDataCollection issue) {
        super(issue);
    }
}
