package com.energyict.mdc.issue.datacollection.rest.response;

import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.issue.rest.response.issue.IssueInfo;
import com.energyict.mdc.issue.datacollection.entity.IssueDataCollection;

public class DataCollectionIssueInfo<T extends DeviceInfo> extends IssueInfo<T, IssueDataCollection> {

    //MDC device mRID
    public String deviceMRID;
    
    //for view connection log
    public Long connectionTaskId;
    public Long comSessionId;
    
    //for view communication log
    public Long comTaskId;
    public Long comTaskSessionId;

    public DataCollectionIssueInfo(IssueDataCollection issue, Class<T> deviceInfoClass){
        super(issue, deviceInfoClass);
    }
}
