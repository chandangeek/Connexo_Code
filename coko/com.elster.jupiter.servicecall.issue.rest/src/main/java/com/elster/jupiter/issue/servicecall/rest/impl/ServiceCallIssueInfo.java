/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.servicecall.rest.impl;

import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.issue.rest.response.issue.IssueInfo;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.servicecall.issue.ServiceCallIssue;

import java.util.List;

public class ServiceCallIssueInfo<T extends DeviceInfo> extends IssueInfo<T, ServiceCallIssue> {

    public IdWithNameInfo serviceCall;
    public IdWithNameInfo parentServiceCall;
    public IdWithNameInfo onState;
    public List<JournalEntryInfo> journals;

    public ServiceCallIssueInfo(ServiceCallIssue issue, Class<T> deviceInfoClass) {
        super(issue, deviceInfoClass);
    }

}
