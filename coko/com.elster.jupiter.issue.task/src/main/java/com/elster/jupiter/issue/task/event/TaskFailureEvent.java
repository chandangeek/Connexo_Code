/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.task.event;

import com.elster.jupiter.issue.share.UnableToCreateEventException;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.task.TaskIssueService;
import com.elster.jupiter.issue.task.impl.ModuleConstants;
import com.elster.jupiter.issue.task.impl.i18n.MessageSeeds;
import com.elster.jupiter.issue.task.impl.records.OpenTaskIssueImpl;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.util.conditions.Condition;

import com.google.inject.Inject;
import com.google.inject.Injector;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.elster.jupiter.issue.task.impl.templates.BasicTaskIssueRuleTemplate.COLON_SEPARATOR;
import static com.elster.jupiter.issue.task.impl.templates.BasicTaskIssueRuleTemplate.LOG_ON_SAME_ISSUE;
import static com.elster.jupiter.util.conditions.Where.where;

public class TaskFailureEvent extends TaskEvent {

    private long taskOccurrenceId;
    private String errorMessage;
    private Instant failureTime;
    protected Long recurrentTaskId;


    @Inject
    public TaskFailureEvent(TaskIssueService taskIssueService, MeteringService meteringService, TaskService taskService, Thesaurus thesaurus, Injector injector) {
        super(taskIssueService, meteringService, taskService, thesaurus, injector);
    }

    @Override
    public void init(Map<?, ?> jsonPayload) {
        try {
            this.taskOccurrenceId = ((Number) jsonPayload.get(ModuleConstants.TASKOCCURRENCE_ID)).longValue();
            this.errorMessage = (String) jsonPayload.get(ModuleConstants.ERROR_MESSAGE);
            this.failureTime = Instant.ofEpochMilli(((Number) jsonPayload.get(ModuleConstants.FAILURE_TIME)).longValue());

        } catch (Exception e) {
            throw new UnableToCreateEventException(getThesaurus(), MessageSeeds.UNABLE_TO_CREATE_EVENT, jsonPayload.toString());
        }
    }

    @Override
    protected Condition getConditionForExistingIssue() {
        return where("taskOccurrence.id").isEqualTo(taskOccurrenceId);
    }


    @Override
    public Optional<EndDevice> getEndDevice() {
        return Optional.empty();
    }

    @Override
    public void apply(Issue issue) {
        if (issue instanceof OpenTaskIssueImpl) {
            OpenTaskIssueImpl taskIssue = (OpenTaskIssueImpl) issue;
            taskIssue.setTaskOccurrence(getTaskService().getOccurrence(taskOccurrenceId).orElseThrow(() -> new IllegalArgumentException("Task Occurrence not found")));
            taskIssue.setErrorMessage(errorMessage);
            taskIssue.setFailureTime(failureTime);
        }
    }


    public boolean logOnSameIssue(String value) {
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

    private TaskOccurrence getTaskOccurrence() {
        return getTaskService().getOccurrence(this.taskOccurrenceId).orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.INVALID_ARGUMENT,
                "taskOccurenceId" + this.taskOccurrenceId));
    }

}
