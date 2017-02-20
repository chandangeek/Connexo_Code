/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.estimation.CannotDeleteWhileBusyException;
import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.estimation.EstimationTaskOccurrenceFinder;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.orm.JournalEntry;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.tasks.TaskStatus;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.time.ScheduleExpression;

import javax.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;

import static com.elster.jupiter.util.conditions.Where.where;

@HasValidGroup
final class EstimationTaskImpl implements IEstimationTask {

    private final IEstimationService estimationService;
    private final TaskService taskService;
    private final DataModel dataModel;
    private final Thesaurus thesaurus;

    private long id;

    private String name;
    private Reference<RecurrentTask> recurrentTask = ValueReference.absent();
    private Reference<EndDeviceGroup> endDeviceGroup = ValueReference.absent();
    private Reference<UsagePointGroup> usagePointGroup = ValueReference.absent();
    private Reference<MetrologyPurpose> metrologyPurpose = ValueReference.absent();
    private Reference<RelativePeriod> period = ValueReference.absent();
    private Instant lastRun;

    private transient boolean scheduleImmediately;
    private transient ScheduleExpression scheduleExpression;
    private transient boolean recurrentTaskDirty;
    private transient Instant nextExecution;
    private transient int logLevel;

    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;

    public QualityCodeSystem qualityCodeSystem;

    @Inject
    public EstimationTaskImpl(DataModel dataModel, IEstimationService estimationService, TaskService taskService, Thesaurus thesaurus) {
        this.estimationService = estimationService;
        this.taskService = taskService;
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
        this.logLevel = Level.WARNING.intValue();
    }

    static IEstimationTask from(DataModel dataModel, String name, EndDeviceGroup endDeviceGroup, UsagePointGroup usagePointGroup, ScheduleExpression scheduleExpression, Instant nextExecution, QualityCodeSystem qualityCodeSystem, int logLevel) {
        return dataModel.getInstance(EstimationTaskImpl.class).init(name, endDeviceGroup, usagePointGroup, scheduleExpression, nextExecution, qualityCodeSystem, logLevel);
    }

    private EstimationTaskImpl init(String name, EndDeviceGroup endDeviceGroup, UsagePointGroup usagePointGroup, ScheduleExpression scheduleExpression, Instant nextExecution, QualityCodeSystem qualityCodeSystem, int logLevel) {
        this.name = name;
        this.logLevel = logLevel;
        this.endDeviceGroup.set(endDeviceGroup);
        this.usagePointGroup.set(usagePointGroup);
        this.scheduleExpression = scheduleExpression;
        this.nextExecution = nextExecution;
        this.qualityCodeSystem = qualityCodeSystem;
        return this;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Optional<Instant> getLastRun() {
        return Optional.ofNullable(lastRun);
    }

    @Override
    public Optional<EndDeviceGroup> getEndDeviceGroup() {
        return endDeviceGroup.getOptional();
    }

    @Override
    public Optional<UsagePointGroup> getUsagePointGroup() {
        return usagePointGroup.getOptional();
    }

    @Override
    public Optional<MetrologyPurpose> getMetrologyPurpose() {
        return metrologyPurpose.getOptional();
    }

    @Override
    public Instant getNextExecution() {
        return recurrentTask.get().getNextExecution();
    }

    @Override
    public QualityCodeSystem getQualityCodeSystem() {
        return qualityCodeSystem;
    }

    @Override
    public void doSave() {
        if (id == 0) {
            persist();
        } else {
            update();
        }
        recurrentTaskDirty = false;
    }


    private void persist() {
        //TODO: 10.3 -> make this dynamic
        String applicationName;
        if(qualityCodeSystem.equals(QualityCodeSystem.MDC)) {
            applicationName = "MultiSense";
        } else if (qualityCodeSystem.equals(QualityCodeSystem.MDM)) {
            applicationName = "Insight";
        } else {
            applicationName = "Admin";
        }
        RecurrentTask task = taskService.newBuilder()
                .setApplication(applicationName)
                .setName(name)
                .setScheduleExpression(scheduleExpression)
                .setDestination(estimationService.getDestination())
                .setPayLoad(getName())
                .scheduleImmediately(scheduleImmediately)
                .setFirstExecution(nextExecution)
                .setLogLevel(logLevel)
                .build();
        recurrentTask.set(task);
        Save.CREATE.save(dataModel, this);
    }

    public void update() {
        if (recurrentTaskDirty) {
            if (!recurrentTask.get().getName().equals(this.name)) {
                recurrentTask.get().setName(name);
            }
            recurrentTask.get().setLogLevel(this.logLevel);
            recurrentTask.get().save();
        }
        Save.UPDATE.save(dataModel, this);
    }

    @Override
    public void delete() {
        if (id == 0) {
            return;
        }
        if (!canBeDeleted()) {
            throw new CannotDeleteWhileBusy();
        }
        dataModel.remove(this);
        if (recurrentTask.isPresent()) {
            recurrentTask.get().delete();
        }
    }

    private class CannotDeleteWhileBusy extends CannotDeleteWhileBusyException {
        CannotDeleteWhileBusy() {
            super(EstimationTaskImpl.this.thesaurus, MessageSeeds.CANNOT_DELETE_WHILE_RUNNING, EstimationTaskImpl.this);
        }
    }

    @Override
    public boolean isActive() {
        return recurrentTask.get().getNextExecution() != null;
    }

    @Override
    public ScheduleExpression getScheduleExpression() {
        return recurrentTask.get().getScheduleExpression();
    }

    @Override
    public void setNextExecution(Instant instant) {
        recurrentTask.get().setNextExecution(instant);
    }

    @Override
    public void setScheduleExpression(ScheduleExpression scheduleExpression) {
        this.recurrentTask.get().setScheduleExpression(scheduleExpression);
        recurrentTaskDirty = true;
    }

    @Override
    public void setName(String name) {
        this.name = (name != null ? name.trim() : "");
        recurrentTaskDirty = true;
    }

    @Override
    public void setEndDeviceGroup(EndDeviceGroup endDeviceGroup) {
        this.endDeviceGroup.set(endDeviceGroup);
    }

    @Override
    public void setUsagePointGroup(UsagePointGroup usagePointGroup) {
        this.usagePointGroup.set(usagePointGroup);
    }

    @Override
    public void setMetrologyPurpose(MetrologyPurpose metrologyPurpose) {
        this.metrologyPurpose.set(metrologyPurpose);
    }

    @Override
    public void triggerNow() {
        recurrentTask.get().triggerNow();
        Optional<TaskOccurrence> lastOccurence = recurrentTask.get().getLastOccurrence();
        if(lastOccurence.isPresent() && lastOccurence.get().getStatus().equals(TaskStatus.NOT_EXECUTED_YET)){
            lastOccurence.get().start();
        }
    }

    @Override
    public void updateLastRun(Instant triggerTime) {
        lastRun = triggerTime;
        dataModel.mapper(IEstimationTask.class).update(this, "lastRun");
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public Instant getCreateTime() {
        return createTime;
    }

    @Override
    public Instant getModTime() {
        return modTime;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public String getName() {
        return (recurrentTask.isPresent()) ? recurrentTask.get().getName() : name;
    }

    @Override
    public void setScheduleImmediately(boolean scheduleImmediately) {
        this.scheduleImmediately = scheduleImmediately;
    }

    @Override
    public Optional<RelativePeriod> getPeriod() {
        return period.getOptional();
    }

    @Override
    public void setPeriod(RelativePeriod relativePeriod) {
        this.period.set(relativePeriod);
    }

    @Override
    public Optional<ScheduleExpression> getScheduleExpression(Instant at) {
        return recurrentTask.get().getVersionAt(at).map(RecurrentTask::getScheduleExpression);
    }

    @Override
    public History<EstimationTask> getHistory() {
        List<JournalEntry<IEstimationTask>> journal = dataModel.mapper(IEstimationTask.class).getJournal(getId());
        return new History<>(journal, this);
    }

    @Override
    public EstimationTaskOccurrenceFinder getOccurrencesFinder() {
        Condition condition = where("recurrentTask").isEqualTo(getRecurrentTask()).and(where("status").isNotEqual(TaskStatus.NOT_EXECUTED_YET));
        return new EstimationTaskOccurrenceFinderImpl(taskService, condition, Order.descending("triggerTime"));
    }

    @Override
    public Optional<TaskOccurrence> getOccurrence(Long id) {
        return taskService.getTaskOccurrenceQueryExecutor().getOptional(id);
    }

    @Override
    public Optional<TaskOccurrence> getLastOccurrence() {
        return taskService.getTaskOccurrenceQueryExecutor()
                .select(where("recurrentTask").isEqualTo(getRecurrentTask()),
                        new Order[]{Order.descending("startDate")}, false, null, 0, 1)
                .stream()
                .findFirst();
    }

    @Override
    public boolean canBeDeleted() {
        return !hasUnfinishedOccurrences();
    }

    private boolean hasUnfinishedOccurrences() {
        return hasBusyOccurrence() || hasQueuedMessages();
    }

    private boolean hasBusyOccurrence() {
        return getLastOccurrence()
                .map(TaskOccurrence::getStatus)
                .orElse(TaskStatus.SUCCESS)
                .equals(TaskStatus.BUSY);
    }

    private boolean hasQueuedMessages() {
        Optional<? extends TaskOccurrence> lastOccurrence = recurrentTask.get().getLastOccurrence();
        Optional<TaskOccurrence> lastDataEstimationOccurrence = getLastOccurrence();
        return lastOccurrence.isPresent() &&
                lastDataEstimationOccurrence
                        .map(TaskOccurrence::getId)
                        .map(i -> !i.equals(lastOccurrence.get().getId()))
                        .orElse(true);
    }

    @Override
    public RecurrentTask getRecurrentTask() {
        return recurrentTask.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EstimationTaskImpl that = (EstimationTaskImpl) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public int getLogLevel() {
        return recurrentTask.isPresent() ? getRecurrentTask().getLogLevel() : logLevel;
    }

    public void setLogLevel(int newLevel) {
        this.logLevel = newLevel;
        if (recurrentTask.isPresent()) {
            recurrentTaskDirty = true;
        }
    }
}
