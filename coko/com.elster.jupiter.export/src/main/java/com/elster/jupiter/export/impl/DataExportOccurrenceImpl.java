package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportStatus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.util.logging.LogEntry;
import com.elster.jupiter.util.logging.LogEntryFinder;
import com.elster.jupiter.util.time.Interval;
import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

class DataExportOccurrenceImpl implements IDataExportOccurrence {

    private Reference<TaskOccurrence> taskOccurrence = ValueReference.absent();
    private Reference<IExportTask> readingTask = ValueReference.absent();
    private Interval exportedDataInterval = Interval.forever();
    private Interval.EndpointBehavior exportedDataBoundaryType;
    private DataExportStatus status = DataExportStatus.BUSY;
    private String failureReason;

    private final DataModel dataModel;
    private final TaskService taskService;

    private transient Range<Instant> exportedDataRange;

    @Inject
    DataExportOccurrenceImpl(DataModel dataModel, TaskService taskService) {
        this.dataModel = dataModel;
        this.taskService = taskService;
    }

    static DataExportOccurrenceImpl from(DataModel model, TaskOccurrence occurrence, IExportTask task) {
        return model.getInstance(DataExportOccurrenceImpl.class).init(occurrence, task);
    }

    private DataExportOccurrenceImpl init(TaskOccurrence occurrence, IExportTask task) {
        taskOccurrence.set(occurrence);
        readingTask.set(task);
        //TODO ZoneId !!

        task.getReadingTypeDataSelector()
                .map(selector -> selector.getExportPeriod().getOpenClosedInterval(occurrence.getTriggerTime().atZone(ZoneId.systemDefault())))
                .ifPresent(instantRange -> {
                    exportedDataInterval = Interval.of(instantRange);
                    exportedDataBoundaryType = Interval.EndpointBehavior.fromRange(instantRange);
                });
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
    public DataExportStatus getStatus() {
        return status;
    }

    @Override
    public Range<Instant> getExportedDataInterval() {
        if (exportedDataRange == null) {
            exportedDataRange = exportedDataBoundaryType.toRange(exportedDataInterval);
        }
        return exportedDataRange;
    }

    @Override
    public String getFailureReason() {
        return this.failureReason;
    }

    public void persist() {
        dataModel.persist(this);
    }

    @Override
    public void update() {
        dataModel.update(this);
    }

    @Override
    public IExportTask getTask() {
        return readingTask.orElseThrow(IllegalStateException::new);
    }

    @Override
    public void end(DataExportStatus status) {
        end(status, null);
    }

    @Override
    public void end(DataExportStatus status, String message) {
        this.status = status;
        this.failureReason = message;
        getTask().updateLastRun(getTriggerTime());
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

    @Override
    public TaskOccurrence getTaskOccurrence() {
        return taskOccurrence.get();
    }

    @Override
    public int nthSince(Instant since) {
        List<TaskOccurrence> occurrences = taskService.getOccurrences(taskOccurrence.get().getRecurrentTask(), Range.closedOpen(since, getTriggerTime()));
        return occurrences.size() + 1;
    }
}
