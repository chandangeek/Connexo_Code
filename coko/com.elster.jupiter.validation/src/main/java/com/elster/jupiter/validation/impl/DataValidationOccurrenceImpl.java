/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.util.logging.LogEntry;
import com.elster.jupiter.util.logging.LogEntryFinder;
import com.elster.jupiter.validation.DataValidationOccurrence;
import com.elster.jupiter.validation.DataValidationTask;
import com.elster.jupiter.validation.DataValidationTaskStatus;

import javax.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

class DataValidationOccurrenceImpl implements DataValidationOccurrence {

    private Reference<TaskOccurrence> taskOccurrence = ValueReference.absent();
    private Reference<DataValidationTask> dataValidationTask = ValueReference.absent();
    private String failureReason;
    private DataValidationTaskStatus status = DataValidationTaskStatus.BUSY;

    private final DataModel dataModel;
    private final Thesaurus thesaurus;

    @Inject
    DataValidationOccurrenceImpl(DataModel dataModel, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
    }

    static DataValidationOccurrenceImpl from(DataModel model, TaskOccurrence occurrence, DataValidationTask task) {
        return model.getInstance(DataValidationOccurrenceImpl.class).init(occurrence, task);
    }

    private DataValidationOccurrenceImpl init(TaskOccurrence occurrence, DataValidationTask task){
        taskOccurrence.set(occurrence);
        dataValidationTask.set(task);
        return this;

    }
    @Override
    public String getFailureReason() {
        return this.failureReason;
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
    public DataValidationTaskStatus getStatus() {
        return status;
    }

    @Override
    public String getStatusName() {
        return this.thesaurus.getFormat(this.getStatus()).format();
    }

    @Override
    public DataValidationTask getTask() {
        return dataValidationTask.orElseThrow(IllegalStateException::new);
    }

    @Override
    public Instant getTriggerTime() {
        return taskOccurrence.get().getTriggerTime();
    }

    @Override
    public List<? extends LogEntry> getLogs() {
        return taskOccurrence.get().getLogs();
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

    void persist() {
        dataModel.persist(this);
    }

    void update() {
        dataModel.update(this);
    }

    @Override
    public TaskOccurrence getTaskOccurrence() {
        return taskOccurrence.get();
    }

    public void end(DataValidationTaskStatus status, String message) {
        this.status = status;
        this.failureReason = message;
        DataValidationTask dataValidationTask = dataModel.mapper(DataValidationTask.class).lock(this.dataValidationTask.get().getId());
        dataValidationTask.updateLastRun(getTriggerTime());
        this.dataValidationTask.set(dataValidationTask);
        this.update();
    }

    @Override
    public void end(DataValidationTaskStatus status) {
        end(status, null);
    }

}