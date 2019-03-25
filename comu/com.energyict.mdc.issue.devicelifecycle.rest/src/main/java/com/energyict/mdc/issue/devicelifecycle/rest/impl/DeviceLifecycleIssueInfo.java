/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.devicelifecycle.rest.impl;

import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.issue.rest.response.issue.IssueInfo;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.energyict.mdc.issue.devicelifecycle.IssueDeviceLifecycle;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class DeviceLifecycleIssueInfo<T extends DeviceInfo> extends IssueInfo<T, IssueDeviceLifecycle> {

    public List<FailedTransitionDataInfo> failedTransitionData = new ArrayList<>();

    public DeviceLifecycleIssueInfo(IssueDeviceLifecycle issue, Class<T> deviceInfoClass) {
        super(issue, deviceInfoClass);
    }

    public static class FailedTransitionDataInfo {

        public List<FailedTransitionInfo> failedTransitions;

    }

    public static class FailedTransitionInfo {

        public IdWithNameInfo lifecycle;

        public IdWithNameInfo transition;

        public IdWithNameInfo from;

        public IdWithNameInfo to;

        public String cause;

        public Instant modTime;

    }
}
