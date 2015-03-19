package com.elster.jupiter.validation.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.RecurrentTaskBuilder;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.validation.*;
import com.elster.jupiter.util.time.ScheduleExpression;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.DUPLICATE_VALIDATION_TASK + "}")
public final class DataValidationTaskImpl implements DataValidationTask {

    private long id;

    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + com.elster.jupiter.validation.MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
    @Size(min = 1, max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + com.elster.jupiter.validation.MessageSeeds.Constants.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    private String name;

    private final TaskService taskService;
    private Instant lastRun;
    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;
    private final Thesaurus thesaurus;
    private transient Instant nextExecution;
    private ValidationService dataValidationService;

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.DEVICE_GROUP_REQUIRED_PROPERTY_MISSING_KEY + "}")
    private Reference<EndDeviceGroup> endDeviceGroup = ValueReference.absent();

    private Reference<RecurrentTask> recurrentTask = ValueReference.absent();

    private final DataModel dataModel;

    private transient boolean recurrentTaskDirty;
    private boolean scheduleImmediately;
    private ScheduleExpression scheduleExpression;

    @Inject
    DataValidationTaskImpl(DataModel dataModel, TaskService taskService,ValidationService dataValidationService,Thesaurus thesaurus)
    {
        this.taskService = taskService;
        this.dataModel = dataModel;
        this.dataValidationService = dataValidationService;
        this.thesaurus = thesaurus;
    }

    static DataValidationTaskImpl from(DataModel model,String name, Instant nextExecution,ValidationService dataValidationService ) {
        return model.getInstance(DataValidationTaskImpl.class).init(name, nextExecution,dataValidationService);
    }

    private DataValidationTaskImpl init(String name, Instant nextExecution,ValidationService dataValidationService) {
        this.nextExecution = nextExecution;
        this.name = name;
        this.dataValidationService = dataValidationService;
        return this;
    }

    @Override
    public void activate() {

    }

    @Override
    public DataValidationStatus execute(DataValidationOccurence taskOccurence) {
        return null;
    }

    @Override
    public void deactivate() {

    }

    @Override
    public boolean canBeDeleted() {
        return !hasUnfinishedOccurrences();
    }

    @Override
    public Instant getNextExecution() {
        return recurrentTask.get().getNextExecution();
    }

    @Override
    public void save() {
        if (getId() == 0) {
            persist();
        } else {
            update();
        }
        recurrentTaskDirty = false;
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
        dataModel.mapper(DataValidationOccurence.class).remove(getOccurrences());
        if (recurrentTask.isPresent()) {
            recurrentTask.get().delete();
        }
    }



    private boolean hasUnfinishedOccurrences() {
        return hasBusyOccurrence() || hasQueuedMessages();
    }

    private boolean hasBusyOccurrence() {
        return getLastOccurrence()
                .map(DataValidationOccurence::getStatus)
                .orElse(DataValidationTaskStatus.SUCCESS)
                .equals(DataValidationTaskStatus.BUSY);
    }

    private boolean hasQueuedMessages() {
        Optional<? extends TaskOccurrence> lastOccurrence = recurrentTask.get().getLastOccurrence();
        Optional<DataValidationOccurence> lastDataValidationOccurrence = getLastOccurrence();
        return lastOccurrence.isPresent() &&
                lastDataValidationOccurrence.map(DataValidationOccurence::getTaskOccurrence)
                        .map(TaskOccurrence::getId)
                        .map(i -> !i.equals(lastOccurrence.get().getId()))
                        .orElse(true);
    }


    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public EndDeviceGroup getEndDeviceGroup() {
        return endDeviceGroup.get();
    }

    @Override
    public void setEndDeviceGroup(EndDeviceGroup endDeviceGroup) {
        this.endDeviceGroup.set(endDeviceGroup);
    }

    @Override
    public Optional<DataValidationOccurence> getLastOccurrence() {
        return dataModel.query(DataValidationOccurence.class, TaskOccurrence.class).select(Operator.EQUAL.compare("dataValidationTask", this), new Order[]{Order.descending("taskocc")},
                false, new String[]{}, 1, 1).stream().findAny();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        return getId() == ((DataValidationTaskImpl) o).getId();
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public List<? extends DataValidationOccurence> getOccurrences() {
        return dataModel.mapper(DataValidationOccurenceImpl.class).find("dataValidationTask", this);
    }

    @Override
    public void setScheduleImmediately(boolean scheduleImmediately) {
        this.scheduleImmediately = scheduleImmediately;
    }

    @Override
    public void setScheduleExpression(ScheduleExpression scheduleExpression) {
        this.scheduleExpression = scheduleExpression;
        recurrentTaskDirty = true;
        if (recurrentTask.isPresent()) {
            this.recurrentTask.get().setScheduleExpression(scheduleExpression);
        }
    }

    @Override
    public Optional<Instant> getLastRun() {
        return Optional.ofNullable(lastRun);
    }

    @Override
    public ScheduleExpression getScheduleExpression() {
        return recurrentTask.get().getScheduleExpression();
    }

    @Override
    public Optional<ScheduleExpression> getScheduleExpression(Instant at) {
        return recurrentTask.get().getHistory().getVersionAt(at).map(RecurrentTask::getScheduleExpression);
    }

    @Override
    public void setNextExecution(Instant instant) {
        this.recurrentTask.get().setNextExecution(instant);
        recurrentTaskDirty = true;
    }

    @Override
    public void triggerNow() {
        if(recurrentTask.isPresent()) {
            recurrentTask.get().triggerNow();
        }
        else{
            persistRecurrentTask();
        }
    }

    private void update() {
        if (recurrentTaskDirty) {
            if(recurrentTask.isPresent()) {
                recurrentTask.get().save();
            }
            else{
                persistRecurrentTask();
            }
        }
        Save.UPDATE.save(dataModel, this);
    }

    private void persist() {
        persistRecurrentTask();
        Save.CREATE.save(dataModel, this);
    }

    private void persistRecurrentTask() {
        RecurrentTaskBuilder builder = taskService.newBuilder()
                .setName(getName())
                .setDestination(dataValidationService.getDestination())
                .setScheduleExpression(scheduleExpression)
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
    }

    private class CannotDeleteWhileBusy extends CannotDeleteWhileBusyException {
        CannotDeleteWhileBusy() {
            super(DataValidationTaskImpl.this.thesaurus, MessageSeeds.CANNOT_DELETE_WHILE_RUNNING, DataValidationTaskImpl.this);
        }
    }

}
