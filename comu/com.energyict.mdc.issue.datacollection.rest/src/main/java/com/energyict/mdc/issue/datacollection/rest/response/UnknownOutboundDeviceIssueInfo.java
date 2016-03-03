package com.energyict.mdc.issue.datacollection.rest.response;

import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.energyict.mdc.issue.datacollection.entity.IssueDataCollection;

public class UnknownOutboundDeviceIssueInfo<T extends DeviceInfo> extends DataCollectionIssueInfo<T> {
    public String slaveDeviceId;

    public UnknownOutboundDeviceIssueInfo(IssueDataCollection issue, Class<T> deviceInfoClass){
        super(issue, deviceInfoClass);
    }
}
