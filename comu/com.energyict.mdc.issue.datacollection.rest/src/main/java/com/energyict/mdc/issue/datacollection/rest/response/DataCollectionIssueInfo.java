/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.rest.response;

import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.issue.rest.response.issue.IssueInfo;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.energyict.mdc.issue.datacollection.entity.IssueDataCollection;

import java.time.Instant;

public class DataCollectionIssueInfo<T extends DeviceInfo> extends IssueInfo<T, IssueDataCollection> {

    //MDC device mRID
    public String deviceName;
    public IdWithNameInfo deviceState;
    public IdWithNameInfo deviceType;
    public IdWithNameInfo deviceConfiguration;

    public Instant firstConnectionAttempt;
    public Instant lastConnectionAttempt;
    public Long connectionAttemptsNumber;

    public DataCollectionIssueInfo(IssueDataCollection issue, Class<T> deviceInfoClass){
        super(issue, deviceInfoClass);
    }
}
