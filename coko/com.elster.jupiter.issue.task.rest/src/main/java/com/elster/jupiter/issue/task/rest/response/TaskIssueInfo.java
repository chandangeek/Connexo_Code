/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.task.rest.response;

import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.issue.rest.response.issue.IssueInfo;
import com.elster.jupiter.issue.task.TaskIssue;

import java.util.List;

public class TaskIssueInfo<T extends DeviceInfo> extends IssueInfo<T, TaskIssue> {

    public RecurrentTaskInfo recurrentTask;
    public List<TaskOccurrenceInfo> taskOccurrences;

    public TaskIssueInfo(TaskIssue issue, Class<T> deviceInfoClass){
        super(issue, deviceInfoClass);
    }
}
