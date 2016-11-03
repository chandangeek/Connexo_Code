package com.elster.jupiter.validation.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.metering.config.MetrologyContract;
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
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.elster.jupiter.validation.CannotDeleteWhileBusyException;
import com.elster.jupiter.validation.DataValidationOccurrence;
import com.elster.jupiter.validation.DataValidationOccurrenceFinder;
import com.elster.jupiter.validation.DataValidationTask;
import com.elster.jupiter.validation.DataValidationTaskStatus;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.elster.jupiter.util.conditions.Where.where;

@HasValidGroup(groups = {Save.Create.class, Save.Update.class})
final class DataValidationTaskImpl implements DataValidationTask {

    @SuppressWarnings("unused") // Managed by ORM
    private long id;

    @NotEmpty(message = "{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
    @Size(max = 80, message = "{" + MessageSeeds.Constants.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    private String name;
    private String application;

    private final TaskService taskService;
    private Instant lastRun;
    @SuppressWarnings("unused") // Managed by ORM
    private long version;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant createTime;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant modTime;
    @SuppressWarnings("unused") // Managed by ORM
    private String userName;
    private final Thesaurus thesaurus;
    private transient Instant nextExecution;

    private Reference<EndDeviceGroup> endDeviceGroup = ValueReference.absent();

    private Reference<UsagePointGroup> usagePointGroup = ValueReference.absent();

    private Reference<RecurrentTask> recurrentTask = ValueReference.absent();

    private final DataModel dataModel;
    private final Provider<DestinationSpec> destinationSpecProvider;

    private transient boolean recurrentTaskDirty;
    private boolean scheduleImmediately;
    private ScheduleExpression scheduleExpression;

    private QualityCodeSystem qualityCodeSystem;

    @Inject
    DataValidationTaskImpl(DataModel dataModel, TaskService taskService, Thesaurus thesaurus, Provider<DestinationSpec> destinationSpecProvider) {
        this.taskService = taskService;
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
        this.destinationSpecProvider = destinationSpecProvider;
    }

    static DataValidationTaskImpl from(DataModel model, String name, Instant nextExecution, QualityCodeSystem qualityCodeSystem) {
        return model.getInstance(DataValidationTaskImpl.class).init(name, nextExecution, qualityCodeSystem);
    }

    DataValidationTaskImpl init(String name, Instant nextExecution, QualityCodeSystem qualityCodeSystem) {
        this.nextExecution = nextExecution;
        this.name = name.trim();
        this.qualityCodeSystem = qualityCodeSystem;
        return this;
    }

    @Override
    public void activate() {

    }

    @Override
    public DataValidationTaskStatus execute(DataValidationOccurrence taskOccurence) {
        return taskOccurence.getStatus();
    }

    @Override
    public void deactivate() {

    }

    @Override
    public boolean canBeDeleted() {
        return !hasUnfinishedOccurrences();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public Instant getNextExecution() {
        return recurrentTask.get().getNextExecution();
    }


    @Override
    public void update() {
        doUpdate();
        recurrentTaskDirty = false;
    }

    void doSave() {
        if (getId() == 0) {
            persist();
        } else {
            doUpdate();
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

        dataModel.mapper(DataValidationOccurrence.class).remove(getOccurrences());
        dataModel.remove(this);
        if (recurrentTask.isPresent()) {
            recurrentTask.get().delete();
        }
    }

    private boolean hasUnfinishedOccurrences() {
        return hasBusyOccurrence() || hasQueuedMessages();
    }

    private boolean hasBusyOccurrence() {
        return getLastOccurrence()
                .map(DataValidationOccurrence::getStatus)
                .orElse(DataValidationTaskStatus.SUCCESS)
                .equals(DataValidationTaskStatus.BUSY);
    }

    private boolean hasQueuedMessages() {
        Optional<? extends TaskOccurrence> lastOccurrence = recurrentTask.get().getLastOccurrence();
        Optional<DataValidationOccurrence> lastDataValidationOccurrence = getLastOccurrence();
        return lastOccurrence.isPresent() &&
                lastDataValidationOccurrence.map(DataValidationOccurrence::getTaskOccurrence)
                        .map(TaskOccurrence::getId)
                        .map(i -> !i.equals(lastOccurrence.get().getId()))
                        .orElse(true);
    }

    @Override
    public String getName() {
        return (recurrentTask.isPresent()) ? recurrentTask.get().getName() : name;
    }

    @Override
    public void setName(String name) {
        this.name = (name != null ? name.trim() : "");
        recurrentTaskDirty = true;
    }

    @Override
    public QualityCodeSystem getQualityCodeSystem() {
        return this.qualityCodeSystem;
    }

    @Override
    public Optional<EndDeviceGroup> getEndDeviceGroup() {
        return endDeviceGroup.getOptional();
    }

    @Override
    public Optional<UsagePointGroup> getUsagePointGroup(){
        return usagePointGroup.getOptional();
    }

    @Override
    public void setEndDeviceGroup(EndDeviceGroup endDeviceGroup) {
        this.endDeviceGroup.set(endDeviceGroup);
    }

    @Override
    public void setUsagePointGroup(UsagePointGroup usagePointGroup){
        this.usagePointGroup.set(usagePointGroup);
    }

    @Override
    public Optional<DataValidationOccurrence> getLastOccurrence() {
        return dataModel.query(DataValidationOccurrence.class, TaskOccurrence.class).select(Operator.EQUAL.compare("dataValidationTask", this), new Order[]{Order.descending("taskocc")},
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
    public List<? extends DataValidationOccurrence> getOccurrences() {
        return dataModel.mapper(DataValidationOccurrence.class).find("dataValidationTask", this);
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
        return recurrentTask.get().getVersionAt(at).map(RecurrentTask::getScheduleExpression);
    }

    @Override
    public void setNextExecution(Instant instant) {
        this.recurrentTask.get().setNextExecution(instant);
        recurrentTaskDirty = true;
    }

    @Override
    public void triggerNow() {
        if (recurrentTask.isPresent()) {
            recurrentTask.get().triggerNow();
        } else {
            persistRecurrentTask();
        }
    }

    private void doUpdate() {
        Save.UPDATE.save(dataModel, this);
        if (recurrentTaskDirty) {
            if (recurrentTask.isPresent()) {
                if (!recurrentTask.get().getName().equals(this.name)) {
                    recurrentTask.get().setName(name);
                }
                recurrentTask.get().save();
            } else {
                persistRecurrentTask();
            }
        }
    }

    private void persist() {
        Save.CREATE.validate(dataModel, this);
        persistRecurrentTask();
        dataModel.persist(this);
    }

    private void persistRecurrentTask() {
        //TODO: 10.3 -> make this dynamic
        String applicationName;
        if (QualityCodeSystem.MDC.equals(qualityCodeSystem)) {
            applicationName = "MultiSense";
        } else if (QualityCodeSystem.MDM.equals(qualityCodeSystem)) {
            applicationName = "Insight";
        } else {
            applicationName = "Admin";
        }
        RecurrentTask task = taskService.newBuilder()
                .setApplication(applicationName)
                .setName(name)
                .setScheduleExpression(scheduleExpression)
                .setDestination(destinationSpecProvider.get())
                .setPayLoad(getName())
                .scheduleImmediately(scheduleImmediately)
                .setFirstExecution(nextExecution).build();
        recurrentTask.set(task);
    }

    private class CannotDeleteWhileBusy extends CannotDeleteWhileBusyException {
        CannotDeleteWhileBusy() {
            super(DataValidationTaskImpl.this.thesaurus, MessageSeeds.CANNOT_DELETE_WHILE_RUNNING, DataValidationTaskImpl.this);
        }
    }

    public long getVersion() {
        return version;
    }

    public Instant getCreateTime() {
        return createTime;
    }

    public Instant getModTime() {
        return modTime;
    }

    public String getUserName() {
        return userName;
    }

    @Override
    public Optional<? extends DataValidationOccurrence> getOccurrence(Long id) {
        return dataModel.mapper(DataValidationOccurrenceImpl.class).getOptional(id).filter(occ -> this.getId() == occ.getTask().getId());
    }

    @Override
    public DataValidationOccurrenceFinder getOccurrencesFinder() {
        Condition condition = where("dataValidationTask").isEqualTo(this);
        Order order = Order.descending("taskocc");
        return new DataValidationOccurrenceFinderImpl(dataModel, condition, order);
    }

    public History<? extends DataValidationTask> getHistory() {
        List<JournalEntry<DataValidationTask>> journal = dataModel.mapper(DataValidationTask.class).getJournal(getId());
        return new History<>(journal, this);
    }

    @Override
    public void updateLastRun(Instant triggerTime) {
        lastRun = triggerTime;
        dataModel.mapper(DataValidationTask.class).update(this, "lastRun");
    }

    void setRecurrentTask(RecurrentTask task) {
        this.recurrentTask.set(task);
    }
}
