/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.task.rest.response;

import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.issue.rest.response.issue.IssueInfo;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.issue.task.entity.TaskIssue;

import java.time.Instant;

public class TaskIssueInfo<T extends DeviceInfo> extends IssueInfo<T, TaskIssue> {

    public IdWithNameInfo taskOccurrence;

    public TaskIssueInfo(TaskIssue issue, Class<T> deviceInfoClass){
        super(issue, deviceInfoClass);
    }
}
