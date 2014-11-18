package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportStatus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.tasks.TaskLogEntryFinder;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.util.logging.LogEntry;
import com.elster.jupiter.util.time.Interval;
import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

class DataExportOccurrenceImpl implements IDataExportOccurrence {

    private Reference<TaskOccurrence> taskOccurrence = ValueReference.absent();
    private Reference<IReadingTypeDataExportTask> readingTask = ValueReference.absent();
    private Instant startDate;
    private Instant endDate;
    private Interval exportedDataInterval;
    private Interval.EndpointBehavior exportedDataBoundaryType;
    private DataExportStatus status = DataExportStatus.BUSY;
    private String failureReason;

    private final DataModel dataModel;
    private final Clock clock;

    private transient Range<Instant> exportedDataRange;

    @Inject
    DataExportOccurrenceImpl(DataModel dataModel, Clock clock) {
        this.dataModel = dataModel;
        this.clock = clock;
    }

    static DataExportOccurrenceImpl from(DataModel model, TaskOccurrence occurrence, IReadingTypeDataExportTask task) {
        return model.getInstance(DataExportOccurrenceImpl.class).init(occurrence, task);
    }

    private DataExportOccurrenceImpl init(TaskOccurrence occurrence, IReadingTypeDataExportTask task) {
        taskOccurrence.set(occurrence);
        readingTask.set(task);
        //TODO ZoneId !!
        Range<ZonedDateTime> interval = task.getExportPeriod().getInterval(occurrence.getTriggerTime().atZone(ZoneId.systemDefault()));
        exportedDataInterval = Interval.of(interval.lowerEndpoint().toInstant(), interval.upperEndpoint().toInstant());
        exportedDataBoundaryType = Interval.EndpointBehavior.fromRange(interval);
        return this;
    }

    @Override
    public Instant getStartDate() {
        return startDate;
    }

    @Override
    public Optional<Instant> getEndDate() {
        return Optional.ofNullable(endDate);
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
    public IReadingTypeDataExportTask getTask() {
        return readingTask.orElseThrow(IllegalStateException::new);
    }

    @Override
    public void start() {
        startDate = clock.instant();
    }

    @Override
    public void end(DataExportStatus status) {
        this.status = status;
        this.endDate = clock.instant();
    }

    @Override
    public void end(DataExportStatus status, String message) {
        this.status = status;
        this.endDate = clock.instant();
        this.failureReason = message;
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
    public TaskLogEntryFinder getLogsFinder() {
        return taskOccurrence.get().getLogsFinder();
    }

    @Override
    public Long getId() {
        return taskOccurrence.get().getId();
    }
}
