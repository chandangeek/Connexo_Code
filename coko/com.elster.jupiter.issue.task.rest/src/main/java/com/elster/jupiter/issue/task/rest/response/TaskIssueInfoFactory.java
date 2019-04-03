/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.task.rest.response;

import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.issue.rest.response.device.DeviceShortInfo;
import com.elster.jupiter.issue.task.TaskIssueService;
import com.elster.jupiter.issue.task.entity.TaskIssue;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.InfoFactory;
import com.elster.jupiter.rest.util.PropertyDescriptionInfo;
import com.elster.jupiter.tasks.TaskOccurrence;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.issue.task.rest.TaskIssueApplication.TASK_ISSUE_REST_COMPONENT;

@Component(name = "task.issue.info.factory", service = {InfoFactory.class}, immediate = true)
public class TaskIssueInfoFactory implements InfoFactory<TaskIssue> {

    private volatile Thesaurus thesaurus;
    private volatile NlsService nlsService;

    public TaskIssueInfoFactory() {
    }

    @Inject
    public TaskIssueInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        Thesaurus domainThesaurus = nlsService.getThesaurus(TaskIssueService.COMPONENT_NAME, Layer.DOMAIN);
        Thesaurus restThesaurus = nlsService.getThesaurus(TASK_ISSUE_REST_COMPONENT, Layer.REST);
        this.thesaurus = domainThesaurus.join(restThesaurus);
    }

    public TaskIssueInfo<?> asInfo(TaskIssue issue, Class<? extends DeviceInfo> deviceInfoClass) {
        TaskIssueInfo<?> info = new TaskIssueInfo<>(issue, deviceInfoClass);
        addTaskInfo(info, issue);
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


    private void addTaskInfo(TaskIssueInfo<?> info, TaskIssue issue) {
        Optional<TaskOccurrence> taskOccurence = issue.getTaskOccurrence();
        taskOccurence.ifPresent(taskOccurrence -> info.taskOccurrence = new TaskOccurrenceInfo(thesaurus, taskOccurrence));
        info.taskOccurrence.errorMessage = issue.getErrorMessage();
        info.taskOccurrence.failureTime = issue.getFailureTime();
    }

}