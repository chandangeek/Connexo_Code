/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.customtask.impl;

import com.elster.jupiter.customtask.CustomTaskStatus;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.logging.LogEntryFinder;
import com.elster.jupiter.util.time.Interval;
import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

class CustomTaskOccurrenceImpl implements ICustomTaskOccurrence/*, DefaultSelectorOccurrence*/ {

    private Reference<TaskOccurrence> taskOccurrence = ValueReference.absent();
    private Reference<ICustomTask> customTask = ValueReference.absent();
    private CustomTaskStatus status = CustomTaskStatus.BUSY;
    private String failureReason;
    private String occurrenceDetails;
    private String summary;

    private final DataModel dataModel;
    private final TaskService taskService;
    private final TransactionService transactionService;
    private final Thesaurus thesaurus;
    private final Clock clock;

    @Inject
    CustomTaskOccurrenceImpl(DataModel dataModel, TaskService taskService, TransactionService transactionService, Thesaurus thesaurus, Clock clock) {
        this.dataModel = dataModel;
        this.taskService = taskService;
        this.transactionService = transactionService;
        this.thesaurus = thesaurus;
        this.clock = clock;
    }

    static CustomTaskOccurrenceImpl from(DataModel model, TaskOccurrence occurrence, ICustomTask task) {
        return model.getInstance(CustomTaskOccurrenceImpl.class).init(occurrence, task);
    }

    CustomTaskOccurrenceImpl init(TaskOccurrence occurrence, ICustomTask task) {
        taskOccurrence.set(occurrence);
        customTask.set(task);
        return this;
    }

    @Override
    public Optional<Instant> getStartDate() {
        return taskOccurrence.get().getStartDate();
    }

    @Override
    public Optional<Instant> getEndDate() {
        return taskOccurrence.get().getEndDate();
    }

    @Override
    public CustomTaskStatus getStatus() {
        return status;
    }

    @Override
    public String getStatusName() {
        return this.thesaurus.getFormat(this.getStatus()).format();
    }

    @Override
    public String getSummary() {
        return this.summary;
    }

    @Override
    public String getFailureReason() {
        return this.failureReason;
    }

    @Override
    public String getOccurrenceDetails() {
        return this.occurrenceDetails;
    }


    public void persist() {
        dataModel.persist(this);
    }

    @Override
    public void update() {
        dataModel.update(this);
    }

    @Override
    public ICustomTask getTask() {
        return customTask.orElseThrow(IllegalStateException::new);
    }

    @Override
    public void summarize(String summaryMessage) {
        try (TransactionContext context = transactionService.getContext()) {
            this.summary = summaryMessage;
            update();
            context.commit();
        }
    }
    @Override
    public void end(CustomTaskStatus status) {
        end(status, null, null);
    }

    @Override
    public void end(CustomTaskStatus status, String message)  {
        end(status, message, null);
    }

    @Override
    public void end(CustomTaskStatus status, String message, String details) {
        this.status = status;
        this.failureReason = message;
        this.occurrenceDetails = details;
        getTask().updateLastRun(getTriggerTime());
    }


    @Override
    public Instant getTriggerTime() {
        return taskOccurrence.get().getTriggerTime();
    }

    @Override
    public LogEntryFinder getLogsFinder() {
        return taskOccurrence.get().getLogsFinder();
    }

    @Override
    public Long getId() {
        return taskOccurrence.get().getId();
    }

    @Override
    public boolean wasScheduled() {
        return taskOccurrence.get().wasScheduled();
    }

    @Override
    public TaskOccurrence getTaskOccurrence() {
        return taskOccurrence.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CustomTaskOccurrenceImpl)) {
            return false;
        }
        CustomTaskOccurrenceImpl that = (CustomTaskOccurrenceImpl) o;
        return Objects.equals(taskOccurrence, that.taskOccurrence);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskOccurrence);
    }

    @Override
    public RecurrentTask getRecurrentTask() {
        return this.getTaskOccurrence().getRecurrentTask();
    }
}
