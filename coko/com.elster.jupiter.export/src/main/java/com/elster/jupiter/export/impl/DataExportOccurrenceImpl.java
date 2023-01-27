/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportRunParameters;
import com.elster.jupiter.export.DataExportStatus;
import com.elster.jupiter.export.DataSelectorConfig;
import com.elster.jupiter.export.DefaultSelectorOccurrence;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.logging.LogEntry;
import com.elster.jupiter.util.logging.LogEntryFinder;
import com.elster.jupiter.util.time.Interval;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

class DataExportOccurrenceImpl implements IDataExportOccurrence, DefaultSelectorOccurrence {

    private Reference<TaskOccurrence> taskOccurrence = ValueReference.absent();
    private Reference<IExportTask> readingTask = ValueReference.absent();
    private Interval exportedDataInterval;
    private Interval.EndpointBehavior exportedDataBoundaryType;
    private DataExportStatus status = DataExportStatus.BUSY;
    private String failureReason;
    private String summary;

    private final DataModel dataModel;
    private final TaskService taskService;
    private final TransactionService transactionService;
    private final Thesaurus thesaurus;
    private final Clock clock;

    private transient Range<Instant> exportedDataRange;

    @Inject
    DataExportOccurrenceImpl(DataModel dataModel, TaskService taskService, TransactionService transactionService, Thesaurus thesaurus, Clock clock) {
        this.dataModel = dataModel;
        this.taskService = taskService;
        this.transactionService = transactionService;
        this.thesaurus = thesaurus;
        this.clock = clock;
    }

    static DataExportOccurrenceImpl from(DataModel model, TaskOccurrence occurrence, IExportTask task) {
        return model.getInstance(DataExportOccurrenceImpl.class).init(occurrence, task);
    }

    DataExportOccurrenceImpl init(TaskOccurrence occurrence, IExportTask task) {
        taskOccurrence.set(occurrence);
        readingTask.set(task);
        //TODO ZoneId !!
        Instant at = occurrence.getRetryTime().orElse(occurrence.getTriggerTime());

        Optional<DataSelectorConfig> standardDataSelector = occurrence.getRetryTime().isPresent() ? task.getStandardDataSelectorConfig(occurrence.getRetryTime()
                .get()) : task.getStandardDataSelectorConfig();
        if ((occurrence.getAdhocTime().isPresent()) && (task.getRunParameters(occurrence.getAdhocTime().get()).isPresent())) {
            DataExportRunParameters runParameters = task.getRunParameters(occurrence.getAdhocTime().get()).get();
            Range<Instant> instantRange;
            if (standardDataSelector.isPresent() && standardDataSelector.get().isExportContinuousData()) {
                instantRange = standardDataSelector.get().getExportPeriod().getOpenClosedInterval(at.atZone(ZoneId.systemDefault()));
                if (instantRange.hasUpperBound() && runParameters.getExportPeriodEnd().isAfter(instantRange.upperEndpoint())) {
                    instantRange = Range.openClosed(instantRange.lowerEndpoint(), runParameters.getExportPeriodEnd());
                }
            } else {
                instantRange = Range.openClosed(runParameters.getExportPeriodStart(), runParameters.getExportPeriodEnd());
            }
            exportedDataInterval = Interval.of(instantRange);
            exportedDataBoundaryType = Interval.EndpointBehavior.fromRange(instantRange);
        } else if ((occurrence.getRetryTime().isPresent()) && (task.getRunParameters(occurrence.getRetryTime().get()).isPresent())) {
            DataExportRunParameters runParameters = task.getRunParameters(occurrence.getRetryTime().get()).get();
            Range<Instant> instantRange = Range.openClosed(runParameters.getExportPeriodStart(), runParameters.getExportPeriodEnd());
            exportedDataInterval = Interval.of(instantRange);
            exportedDataBoundaryType = Interval.EndpointBehavior.fromRange(instantRange);
        } else {
            if (standardDataSelector.isPresent()) {
                Range instantRange = standardDataSelector.get().getExportPeriod().getOpenClosedInterval(at.atZone(ZoneId.systemDefault()));
                exportedDataInterval = Interval.of(instantRange);
                exportedDataBoundaryType = Interval.EndpointBehavior.fromRange(instantRange);
            } else {
                exportedDataInterval = Interval.of(occurrence.getRecurrentTask().getLastRun().orElse(Instant.EPOCH), occurrence.getTriggerTime());
                exportedDataBoundaryType = Interval.EndpointBehavior.OPEN_CLOSED;
            }
        }
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
    public String getStatusName() {
        return this.thesaurus.getFormat(this.getStatus()).format();
    }

    @Override
    public Optional<DefaultSelectorOccurrence> getDefaultSelectorOccurrence() {
        if (!getTask().hasDefaultSelector()) {
            return Optional.empty();
        }
        return Optional.of(this);
    }

    @Override
    public Range<Instant> getExportedDataInterval() {
        if (exportedDataRange == null) {
            exportedDataRange = exportedDataBoundaryType.toRange(exportedDataInterval);
        }
        return exportedDataRange;
    }

    @Override
    public String getSummary() {
        return this.summary;
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
    public void summarize(String summaryMessage) {
        try (TransactionContext context = transactionService.getContext()) {
            this.summary = summaryMessage;
            update();
            context.commit();
        }
    }

    @Override
    public Instant getTriggerTime() {
        return taskOccurrence.get().getTriggerTime();
    }

    @Override
    public Optional<Instant> getRetryTime() {
        return taskOccurrence.get().getRetryTime();
    }

    @Override
    public Optional<Instant> getAdhocTime() {
        return taskOccurrence.get().getAdhocTime();
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
        Instant triggerTime = getTriggerTime();
        if (triggerTime.isBefore(since)) {
            triggerTime = Instant.now(clock);
        }
        List<TaskOccurrence> occurrences = taskService.getOccurrences(taskOccurrence.get().getRecurrentTask(), Range.closedOpen(since, triggerTime));
        return occurrences.size() + 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DataExportOccurrenceImpl)) {
            return false;
        }
        DataExportOccurrenceImpl that = (DataExportOccurrenceImpl) o;
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

    @Override
    public void updateExportedDataRange(Range<Instant> instantRange) {
        exportedDataRange = instantRange;
        exportedDataInterval = Interval.of(instantRange);
        exportedDataBoundaryType = Interval.EndpointBehavior.fromRange(instantRange);
    }

    @Override
    public void setToFailed() {
        if (getStatus().equals(DataExportStatus.BUSY)) {
            this.getTaskOccurrence().setToFailed();
            status = DataExportStatus.FAILED;
            failureReason = thesaurus.getSimpleFormat(MessageSeeds.OCCURRENCE_HAS_BEEN_SET_TO_FAILED).format();
            dataModel.mapper(DataExportOccurrenceImpl.class).update(this, "status", "failureReason");

            Logger logger = Logger.getAnonymousLogger();
            logger.addHandler(getTaskOccurrence().createTaskLogHandler(getRecurrentTask()).asHandler());
            logger.info(failureReason);
        }
    }
}
