/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.task.event;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.task.TaskIssueService;
import com.elster.jupiter.issue.task.entity.OpenTaskIssue;
import com.elster.jupiter.issue.task.impl.event.EventDescription;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.util.conditions.Condition;

import com.google.inject.Injector;

import java.time.Instant;
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

    private Instant timestamp;
    private EventDescription eventDescription;
    private Optional<? extends OpenIssue> existingIssue;
    private Injector injector;

    public TaskEvent(TaskIssueService taskIssueService, MeteringService meteringService, TaskService taskService, Thesaurus thesaurus, Injector injector) {
        this.taskIssueService = taskIssueService;
        this.meteringService = meteringService;
        this.taskService = taskService;
        this.thesaurus = thesaurus;
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
        if (existingIssue == null) {
            Query<OpenTaskIssue> query = getTaskIssueService().query(OpenTaskIssue.class, TaskOccurrence.class);
            List<OpenTaskIssue> theSameIssues = query.select(getConditionForExistingIssue());
            if (!theSameIssues.isEmpty()) {
                existingIssue = Optional.of(theSameIssues.get(0));
            } else {
                existingIssue = Optional.empty();
            }
        }
        return existingIssue;
    }

    protected abstract Condition getConditionForExistingIssue();

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


    public boolean isResolveEvent() {
        return false;
    }

}