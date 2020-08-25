/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.task.event;

import com.elster.jupiter.issue.share.UnableToCreateIssueException;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.issue.task.TaskIssue;
import com.elster.jupiter.issue.task.TaskIssueService;
import com.elster.jupiter.issue.task.entity.OpenTaskIssueImpl;
import com.elster.jupiter.issue.task.impl.ModuleConstants;
import com.elster.jupiter.issue.task.impl.i18n.MessageSeeds;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskService;

import com.google.inject.Inject;
import com.google.inject.Injector;

import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.elster.jupiter.issue.task.impl.templates.BasicTaskIssueRuleTemplate.COLON_SEPARATOR;
import static com.elster.jupiter.issue.task.impl.templates.BasicTaskIssueRuleTemplate.LOG_ON_SAME_ISSUE;

public class TaskFailureEvent extends TaskEvent {

    private long taskOccurrenceId;
    private String errorMessage;
    private Instant failureTime;
    protected Long recurrentTaskId;


    @Inject
    public TaskFailureEvent(TaskIssueService taskIssueService, MeteringService meteringService, TaskService taskService, Thesaurus thesaurus, IssueService issueService, Injector injector) {
        super(taskIssueService, meteringService, taskService, thesaurus, issueService, injector);
    }

    @Override
    public void init(Map<?, ?> jsonPayload) {
        try {
            this.taskOccurrenceId = ((Number) jsonPayload.get(ModuleConstants.TASKOCCURRENCE_ID)).longValue();
            this.errorMessage = (String) jsonPayload.get(ModuleConstants.ERROR_MESSAGE);
            this.failureTime = Instant.ofEpochMilli(((Number) jsonPayload.get(ModuleConstants.FAILURE_TIME)).longValue());

        } catch (Exception e) {
            throw new UnableToCreateIssueException(getThesaurus(), MessageSeeds.UNABLE_TO_CREATE_EVENT, jsonPayload.toString());
        }
    }

    @Override
    protected Optional<? extends TaskIssue> filterIssuesByTaskType(List<? extends TaskIssue> issues) {
        return issues.stream().
                filter(this::checkIssuetaskOccurrencesHaveTheSameType).
                filter(issue -> issue.getRelatedTaskOccurrences().get(0).getTaskOccurrence().getRecurrentTask().getId() == recurrentTaskId)
                .max(Comparator.comparing(Issue::getCreateDateTime));
    }

    private boolean checkIssuetaskOccurrencesHaveTheSameType(TaskIssue issue) {
        Set<RecurrentTask> recurrentTasks = issue.getRelatedTaskOccurrences().stream().map(occ -> occ.getTaskOccurrence().getRecurrentTask()).collect(Collectors.toSet());
        return recurrentTasks.size() == 1;
    }


    @Override
    public Optional<EndDevice> getEndDevice() {
        return Optional.empty();
    }

    @Override
    public void apply(Issue issue) {
        if (issue instanceof OpenTaskIssueImpl) {
            OpenTaskIssueImpl taskIssue = (OpenTaskIssueImpl) issue;
            taskIssue.addTaskOccurrence(getTaskOccurrence(), errorMessage, failureTime);

        }
    }


    public boolean logOnSameIssue(int ruleId, String value) {
        setCreationRule(ruleId);
        List<String> values = Arrays.asList(value.split(COLON_SEPARATOR));
        if (values.size() != 2) {
            throw new LocalizedFieldValidationException(MessageSeeds.INVALID_NUMBER_OF_ARGUMENTS,
                    "properties." + LOG_ON_SAME_ISSUE,
                    String.valueOf(2),
                    String.valueOf(values.size()));
        }
        return Integer.parseInt(values.get(0)) == 1;
    }

    public long getRecurrentTaskId() {
        if (recurrentTaskId == null) {
            recurrentTaskId = getTaskOccurrence().getRecurrentTask().getId();
        }
        return recurrentTaskId;
    }

    public TaskOccurrence getTaskOccurrence() {
        return getTaskService().getOccurrence(this.taskOccurrenceId).orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.INVALID_ARGUMENT,
                "taskOccurenceId" + this.taskOccurrenceId));
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Instant getFailureTime() {
        return failureTime;
    }

    @Override
    public String toString() {
        return "TaskFailureEvent{" +
                "taskOccurrenceId=" + taskOccurrenceId +
                ", errorMessage='" + errorMessage + '\'' +
                ", failureTime=" + failureTime +
                ", recurrentTaskId=" + recurrentTaskId +
                '}';
    }
}
