/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.servicecall.rest.impl;

import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.issue.rest.response.issue.IssueInfo;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.servicecall.issue.IssueServiceCall;

public class ServiceCallIssueInfo<T extends DeviceInfo> extends IssueInfo<T, IssueServiceCall> {

    public IdWithNameInfo serviceCall;
    public IdWithNameInfo onState;

    public ServiceCallIssueInfo(IssueServiceCall issue, Class<T> deviceInfoClass) {
        super(issue, deviceInfoClass);
    }

}
