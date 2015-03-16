package com.elster.jupiter.validation.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.util.logging.LogEntry;
import com.elster.jupiter.util.logging.LogEntryFinder;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.DataValidationOccurence;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.DataValidationTask;
import com.elster.jupiter.validation.DataValidationTaskStatus;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;


public class DataValidationOccurenceImpl implements DataValidationOccurence {


    private Reference<TaskOccurrence> taskOccurrence = ValueReference.absent();
    private Reference<DataValidationTask> dataValidationTask = ValueReference.absent();
    private Interval dataValidationDataInterval;
    private String failureReason;
    private DataValidationTaskStatus status = DataValidationTaskStatus.BUSY;

    private final DataModel dataModel;
    private final Clock clock;

    @Inject
    DataValidationOccurenceImpl(DataModel dataModel, Clock clock) {
        this.dataModel = dataModel;
        this.clock = clock;
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
}

