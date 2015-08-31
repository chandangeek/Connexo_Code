package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.estimation.EstimationTaskOccurrenceFinder;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.orm.JournalEntry;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.RecurrentTaskBuilder;
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
import java.util.Optional;
import java.util.UUID;

import static com.elster.jupiter.util.conditions.Where.where;

@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.DUPLICATE_ESTIMATION_TASK + "}")
public class EstimationTaskImpl implements IEstimationTask {

    private final IEstimationService estimationService;
    private final TaskService taskService;
    private final DataModel dataModel;

    private long id;

    private String name;
    private Reference<RecurrentTask> recurrentTask = ValueReference.absent();
    private Reference<EndDeviceGroup> endDeviceGroup = ValueReference.absent();
    private Reference<RelativePeriod> period = ValueReference.absent();
    private Instant lastRun;

    private transient boolean scheduleImmediately;
    private transient ScheduleExpression scheduleExpression;
    private transient boolean recurrentTaskDirty;
    private transient Instant nextExecution;

    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;

    @Inject
    public EstimationTaskImpl(DataModel dataModel, IEstimationService estimationService, TaskService taskService) {
        this.estimationService = estimationService;
        this.taskService = taskService;
        this.dataModel = dataModel;
    }

    static IEstimationTask from(DataModel dataModel, String name, EndDeviceGroup endDeviceGroup, ScheduleExpression scheduleExpression, Instant nextExecution) {
        return dataModel.getInstance(EstimationTaskImpl.class).init(name, endDeviceGroup, scheduleExpression, nextExecution);
    }

    private EstimationTaskImpl init(String name, EndDeviceGroup endDeviceGroup, ScheduleExpression scheduleExpression, Instant nextExecution) {
        this.name = name;
        this.endDeviceGroup.set(endDeviceGroup);
        this.scheduleExpression = scheduleExpression;
        this.nextExecution = nextExecution;
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
    public EndDeviceGroup getEndDeviceGroup() {
        return endDeviceGroup.get();
    }

    @Override
    public Instant getNextExecution() {
        return recurrentTask.get().getNextExecution();
    }

    @Override
    public void save() {
        if (id == 0) {
            persist();
        } else {
            update();
        }
        recurrentTaskDirty = false;
    }

    private void persist() {
        RecurrentTaskBuilder builder = taskService.newBuilder()
                .setName(UUID.randomUUID().toString())
                .setScheduleExpression(scheduleExpression)
                .setDestination(estimationService.getDestination())
                .setPayLoad(getName());
        if (scheduleImmediately) {
            builder.scheduleImmediately();
        }
        RecurrentTask task = builder.build();
        if (nextExecution != null) {
            task.setNextExecution(nextExecution);
        }
        task.save();
        recurrentTask.set(task);
        Save.CREATE.save(dataModel, this);
    }

    private void update() {
        if (recurrentTaskDirty) {
            recurrentTask.get().save();
        }
        Save.UPDATE.save(dataModel, this);
    }

    @Override
    public void delete() {
        if (id == 0) {
            return;
        }
        dataModel.remove(this);
        if (recurrentTask.isPresent()) {
            recurrentTask.get().delete();
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
    }

    @Override
    public void setEndDeviceGroup(EndDeviceGroup endDeviceGroup) {
        this.endDeviceGroup.set(endDeviceGroup);
    }

    @Override
    public void triggerNow() {
        recurrentTask.get().triggerNow();
    }

    @Override
    public void updateLastRun(Instant triggerTime) {
        lastRun = triggerTime;
        save();
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
        return name;
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
        return recurrentTask.get().getHistory().getVersionAt(at).map(RecurrentTask::getScheduleExpression);
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
        return true; // TODO
    }

    @Override
    public RecurrentTask getRecurrentTask() {
        return recurrentTask.get();
    }
}
