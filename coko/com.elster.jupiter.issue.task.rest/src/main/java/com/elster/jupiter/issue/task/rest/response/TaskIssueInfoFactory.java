/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.task.rest.response;

import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.issue.rest.response.device.DeviceShortInfo;
import com.elster.jupiter.issue.task.entity.TaskIssue;
import com.elster.jupiter.issue.task.rest.ModuleConstants;
import com.elster.jupiter.rest.util.InfoFactory;
import com.elster.jupiter.rest.util.PropertyDescriptionInfo;

import org.osgi.service.component.annotations.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component(name = "task.issue.info.factory", service = {InfoFactory.class}, immediate = true)
public class TaskIssueInfoFactory implements InfoFactory<TaskIssue> {


    public TaskIssueInfoFactory() {
    }


    public TaskIssueInfo<?> asInfo(TaskIssue issue, Class<? extends DeviceInfo> deviceInfoClass) {
        TaskIssueInfo<?> info = new TaskIssueInfo<>(issue, deviceInfoClass);

        switch (issue.getReason().getKey()) {
            case ModuleConstants.REASON_TASK_FAILED:
                //TODO: do something
                break;
        }
        return info;
    }


    public List<TaskIssueInfo<?>> asInfos(List<? extends TaskIssue> issues) {
        return issues.stream().map(issue -> this.asInfo(issue, DeviceShortInfo.class)).collect(Collectors.toList());
    }


    @Override
    public Object from(TaskIssue taskIssue) {
        return asInfo(taskIssue, DeviceInfo.class);
    }

    @Override
    public List<PropertyDescriptionInfo> modelStructure() {
        return new ArrayList<>();
    }

    @Override
    public Class<TaskIssue> getDomainClass() {
        return TaskIssue.class;
    }
}