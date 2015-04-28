package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.estimation.EstimationTaskOccurrence;
import com.elster.jupiter.estimation.EstimationTaskStatus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.util.logging.LogEntry;
import com.elster.jupiter.util.logging.LogEntryFinder;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class EstimationTaskOccurrenceImpl implements EstimationTaskOccurrence {

    private Reference<TaskOccurrence> taskOccurrence = ValueReference.absent();
    private Reference<IEstimationTask> estimationTask = ValueReference.absent();
    private EstimationTaskStatus status = EstimationTaskStatus.BUSY;
    private String failureReason;

    private final DataModel dataModel;
    private final Clock clock;

    @Inject
    EstimationTaskOccurrenceImpl(DataModel dataModel, Clock clock) {
        this.dataModel = dataModel;
        this.clock = clock;
    }

    static EstimationTaskOccurrenceImpl from(DataModel model, TaskOccurrence occurrence, IEstimationTask task) {
        return model.getInstance(EstimationTaskOccurrenceImpl.class).init(occurrence, task);
    }

    private EstimationTaskOccurrenceImpl init(TaskOccurrence occurrence, IEstimationTask task) {
        taskOccurrence.set(occurrence);
        estimationTask.set(task);
        return this;

    }

    @Override
    public EstimationTask getTask() {
        return estimationTask.orElseThrow(IllegalStateException::new);
    }

    @Override
    public Optional<Instant> getStartDate() {
        return taskOccurrence.get().getStartDate();
    }

    @Override
    public String getFailureReason() {
        return this.failureReason;
    }

    @Override
    public Optional<Instant> getEndDate() {
        return taskOccurrence.get().getEndDate();
    }

    @Override
    public EstimationTaskStatus getStatus() {
        return status;
    }

    @Override
    public Instant getTriggerTime() {
        return taskOccurrence.get().getTriggerTime();
    }

    @Override
    public boolean wasScheduled() {
        return taskOccurrence.get().wasScheduled();
    }

    @Override
    public Long getId() {
        return taskOccurrence.get().getId();
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
    public void persist() {
        dataModel.persist(this);
    }

    @Override
    public void update() {
        dataModel.update(this);
    }

    @Override
    public TaskOccurrence getTaskOccurrence() {
        return taskOccurrence.get();
    }

    @Override
    public void end(EstimationTaskStatus status, String message) {
        this.status = status;
        this.failureReason = message;
        getTask().updateLastRun(getTriggerTime());
    }

    @Override
    public void end(EstimationTaskStatus status) {
        end(status, null);
    }
}
