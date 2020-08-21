/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.task.event;

import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.issue.task.TaskIssueFilter;
import com.elster.jupiter.issue.task.TaskIssue;
import com.elster.jupiter.issue.task.TaskIssueService;
import com.elster.jupiter.issue.task.impl.event.EventDescription;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.tasks.TaskService;

import com.google.inject.Injector;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public abstract class TaskEvent implements IssueEvent, Cloneable {
    protected static final Logger LOG = Logger.getLogger(TaskEvent.class.getName());

    private final TaskIssueService taskIssueService;
    private final MeteringService meteringService;
    private final TaskService taskService;
    private final Thesaurus thesaurus;
    private final IssueService issueService;

    private Instant timestamp;
    private EventDescription eventDescription;
    private Injector injector;
    private int ruleId;

    public TaskEvent(TaskIssueService taskIssueService, MeteringService meteringService, TaskService taskService, Thesaurus thesaurus, IssueService issueService, Injector injector) {
        this.taskIssueService = taskIssueService;
        this.meteringService = meteringService;
        this.taskService = taskService;
        this.thesaurus = thesaurus;
        this.issueService = issueService;
        this.injector = injector;
    }

    protected TaskIssueService getTaskIssueService() {
        return taskIssueService;
    }

    protected MeteringService getMeteringService() {
        return meteringService;
    }

    protected TaskService getTaskService() {
        return taskService;
    }

    protected Thesaurus getThesaurus() {
        return this.thesaurus;
    }

    protected EventDescription getDescription() {
        if (eventDescription == null) {
            throw new IllegalStateException("You are trying to get event description for event that was not initialized yet");
        }
        return eventDescription;
    }

    public abstract void init(Map<?, ?> jsonPayload);

    protected void setEventDescription(EventDescription eventDescription) {
        this.eventDescription = eventDescription;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public void wrap(Map<?, ?> rawEvent, EventDescription eventDescription) {
        this.eventDescription = eventDescription;

    }

    @Override
    public String getEventType() {
        return eventDescription.getUniqueKey();
    }

    @Override
    public Optional<? extends OpenIssue> findExistingIssue() {
        TaskIssueFilter filter = new TaskIssueFilter();
        Optional<CreationRule> rule = issueService.getIssueCreationService().findCreationRuleById(ruleId);
        if(rule.isPresent()){
            filter.setRule(rule.get());
            new ArrayList<String>(){{
                add(IssueStatus.OPEN);
                add(IssueStatus.IN_PROGRESS);
                add(IssueStatus.SNOOZED);
            }}.forEach(is -> filter.addStatus(issueService.findStatus(is).get()));
            filter.setIssueReason(rule.get().getReason());
            Optional<? extends TaskIssue> foundIssue = filterIssuesByTaskType(taskIssueService.findIssues(filter).find());
            if (foundIssue.isPresent()) {
                return Optional.of((OpenIssue) foundIssue.get());
            }
        }
        return Optional.empty();

    }

    protected abstract Optional<? extends TaskIssue> filterIssuesByTaskType(List<? extends TaskIssue> issues);

    protected Optional<Long> getLong(Map<?, ?> map, String key) {
        Object contents = map.get(key);
        if (contents == null) {
            return Optional.empty();
        }
        return Optional.of(((Number) contents).longValue());
    }

    @Override
    public TaskEvent clone() {
        TaskEvent clone = injector.getInstance(eventDescription.getEventClass());
        clone.eventDescription = eventDescription;
        return clone;
    }

    protected void setCreationRule(int ruleId){
        this.ruleId = ruleId;
    }


    public boolean isResolveEvent() {
        return false;
    }

    @Override
    public String toString() {
        return this.getClass() + "{" +
                "timestamp=" + timestamp +
                ", eventDescription=" + eventDescription +
                ", ruleId=" + ruleId +
                '}';
    }
}