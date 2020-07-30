/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.customtask.impl;

import com.elster.jupiter.customtask.CannotDeleteWhileBusyException;
import com.elster.jupiter.customtask.CustomTask;
import com.elster.jupiter.customtask.CustomTaskAction;
import com.elster.jupiter.customtask.CustomTaskFactory;
import com.elster.jupiter.customtask.CustomTaskOccurrence;
import com.elster.jupiter.customtask.CustomTaskOccurrenceFinder;
import com.elster.jupiter.customtask.CustomTaskProperty;
import com.elster.jupiter.customtask.CustomTaskStatus;
import com.elster.jupiter.customtask.PropertiesInfo;
import com.elster.jupiter.domain.util.HasNoBlacklistedCharacters;
import com.elster.jupiter.domain.util.HasNotAllowedChars;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.orm.JournalEntry;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.RecurrentTaskBuilder;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.time.ScheduleExpression;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

@HasValidProperties(groups = {Save.Create.class, Save.Update.class})
@IsExistingTaskType(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.NO_SUCH_TASKTYPE + "}")
final class CustomTaskImpl implements ICustomTask {

    private final TaskService taskService;
    private final DataModel dataModel;
    private final ICustomTaskService customTaskService;
    private final Thesaurus thesaurus;
    @NotNull(message = "{" + MessageSeeds.Keys.NAME_REQUIRED_KEY  + "}")
    @Size(min = 1, max = Table.NAME_LENGTH, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    @HasNoBlacklistedCharacters(balcklistedCharRegEx = HasNotAllowedChars.Constant.SPECIAL_CHARS)
    private String name;
    @NotNull
    private transient String taskType;

    private transient ScheduleExpression scheduleExpression;
    private transient Instant nextExecution;
    @SuppressWarnings("unused") // Managed by ORM
    private long id;
    private Reference<RecurrentTask> recurrentTask = ValueReference.absent();

    private List<CustomTaskProperty> properties = new ArrayList<>();
    private Instant lastRun;
    private transient boolean scheduleImmediately;
    private transient boolean recurrentTaskDirty;
    private transient boolean propertiesDirty;
    @SuppressWarnings("unused") // Managed by ORM
    private long version;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant createTime;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant modTime;

    @SuppressWarnings("unused") // Managed by ORM
    private String userName;
    private transient int logLevel;


    private transient String application;
    private transient List<RecurrentTask> nextRecurrentTasks = new ArrayList<>();

    @Inject
    CustomTaskImpl(DataModel dataModel, ICustomTaskService customTaskService, TaskService taskService, Thesaurus thesaurus) {
        this.customTaskService = customTaskService;
        this.dataModel = dataModel;
        this.taskService = taskService;
        this.thesaurus = thesaurus;
    }

    static CustomTaskImpl from(DataModel dataModel, String name, String taskType, ScheduleExpression scheduleExpression, Instant nextExecution, String application, int logLevel) {
        return dataModel.getInstance(CustomTaskImpl.class).init(name, taskType, scheduleExpression, nextExecution, application, logLevel);
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Map<String, Object> getValues() {
        return properties.stream()
                .collect(Collectors.toMap(CustomTaskProperty::getName, CustomTaskProperty::getValue));
    }

    @Override
    public Map<String, Object> getValues(Instant at) {
        List<JournalEntry<CustomTaskProperty>> props = dataModel.mapper(CustomTaskProperty.class).at(at).find(ImmutableMap.of("task", this));
        return props.stream()
                .map(JournalEntry::get)
                .collect(Collectors.toMap(CustomTaskProperty::getName, CustomTaskProperty::getValue));
    }

    @Override
    public List<CustomTaskProperty> getCustomTaskProperties() {
        return Collections.unmodifiableList(properties);
    }

    @Override
    public Instant getNextExecution() {
        return recurrentTask.get().getNextExecution();
    }

    public List<? extends CustomTaskOccurrence> getOccurrences() {
        return dataModel.mapper(CustomTaskOccurrenceImpl.class).find("customTask", this);
    }

    @Override
    public CustomTaskOccurrenceFinder getOccurrencesFinder() {
        Condition condition = where("customTask").isEqualTo(this);
        Order order = Order.descending("taskocc");
        return new CustomTaskOccurrenceFinderImpl(dataModel, condition, order);
    }

    @Override
    public Optional<ICustomTaskOccurrence> getLastOccurrence() {
        return dataModel.query(ICustomTaskOccurrence.class, TaskOccurrence.class).select(Operator.EQUAL.compare("customTask", this), new Order[]{Order.descending("taskocc")},
                false, new String[]{}, 1, 1).stream().findAny();
    }

    @Override
    public Optional<? extends CustomTaskOccurrence> getOccurrence(Long id) {
        return dataModel.mapper(CustomTaskOccurrenceImpl.class).getOptional(id).filter(occ -> this.getId() == occ.getTask().getId());
    }

    @Override
    public void update() {
        doSave();
    }

    void doSave() {
       if (id == 0) {
            persist();
        } else {
            doUpdate();
        }
        recurrentTaskDirty = false;
        propertiesDirty = false;
    }

    @Override
    public void delete() {
        if (id == 0) {
            return;
        }
        if (!canBeDeleted()) {
            throw new CannotDeleteWhileBusy();
        }
        properties.clear();
        dataModel.mapper(CustomTaskOccurrence.class).remove(getOccurrences());
        dataModel.remove(this);
        if (recurrentTask.isPresent()) {
            recurrentTask.get().delete();
        }
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
                .map(CustomTaskOccurrence::getStatus)
                .orElse(CustomTaskStatus.SUCCESS)
                .equals(CustomTaskStatus.BUSY);
    }

    private boolean hasQueuedMessages() {
        Optional<? extends TaskOccurrence> lastOccurrence = recurrentTask.get().getLastOccurrence();
        Optional<ICustomTaskOccurrence> lastCustomTaskOccurrence = getLastOccurrence();
        return lastOccurrence.isPresent() &&
                lastCustomTaskOccurrence.map(ICustomTaskOccurrence::getTaskOccurrence)
                        .map(TaskOccurrence::getId)
                        .map(i -> !i.equals(lastOccurrence.get().getId()))
                        .orElse(true);
    }

    @Override
    public boolean isActive() {
        return recurrentTask.get().getNextExecution() != null;
    }

    @Override
    public CustomTaskFactory getCustomTaskFactory() {
        return this.customTaskService.getCustomTaskFactory(this.taskType).orElseThrow(() -> new IllegalArgumentException("No such data selector: " + this.taskType));
    }

    @Override
    public List<PropertiesInfo> getPropertySpecs() {
        return getCustomTaskFactory().getProperties();
    }

    @Override
    public List<CustomTaskAction> getActionsForUser(User user, String application) {
        return getCustomTaskFactory().getActionsForUser(user, application);
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
    public void setProperty(String name, Object value) {
        CustomTaskProperty customTaskProperty = properties.stream()
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .orElseGet(() -> {
                    CustomTaskPropertyImpl property = CustomTaskPropertyImpl.from(dataModel, this, name, value);
                    properties.add(property);
                    return property;
                });
        customTaskProperty.setValue(value);
        propertiesDirty = true;
    }

    public void removeProperty(PropertySpec propertySpec) {
        Optional<CustomTaskProperty> customTaskProperty = properties.stream()
                .filter(p -> (p.instanceOfSpec(propertySpec) && p.getName().equals(propertySpec.getName())))
                .findFirst();
        if (customTaskProperty.isPresent()) {
            properties.remove(customTaskProperty.get());
            propertiesDirty = true;
        }
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        return getPropertySpecs().stream()
                .map(PropertiesInfo::getProperties)
                .flatMap(List::stream)
                .filter(p -> name.equals(p.getName()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getDisplayName(String name) {
        return properties.stream()
                .filter(p -> p.getName().equals(name))
                .findAny()
                .map(CustomTaskProperty::getDisplayName)
                .orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public void setScheduleImmediately(boolean scheduleImmediately) {
        this.scheduleImmediately = scheduleImmediately;
    }

    @Override
    public void setNextRecurrentTasks(List<RecurrentTask> nextRecurrentTasks) {
        this.nextRecurrentTasks = nextRecurrentTasks;
    }

    @Override
    public List<RecurrentTask> getNextRecurrentTasks() {
        return recurrentTask.getOptional()
                .map(recurrentTask -> recurrentTask.getNextRecurrentTasks())
                .orElse(Collections.emptyList());
    }

    @Override
    public List<RecurrentTask> getPrevRecurrentTasks() {
        return recurrentTask.getOptional()
                .map(recurrentTask -> recurrentTask.getPrevRecurrentTasks())
                .orElse(Collections.emptyList());
    }

    @Override
    public void triggerNow() {
        recurrentTask.get().triggerNow();
    }

    @Override
    public String getName() {
        return (recurrentTask.isPresent()) ? recurrentTask.get().getName() : name;
    }

    private void doUpdate() {
        Save.UPDATE.validate(dataModel, this);
        if (recurrentTaskDirty) {
            if (!recurrentTask.get().getName().equals(this.name)) {
                recurrentTask.get().setName(name);
            }
            recurrentTask.get().setLogLevel(this.logLevel);
            recurrentTask.get().setNextRecurrentTasks(this.nextRecurrentTasks);
            recurrentTask.get().save();
        }
        if (propertiesDirty) {
            properties.forEach(CustomTaskProperty::save);
        }
        Save.UPDATE.save(dataModel, this);
    }

    private void persist() {
        Save.CREATE.validate(dataModel, this);
        RecurrentTask task = taskService.newBuilder()
                .setApplication(application)
                .setName(name)
                .setScheduleExpression(scheduleExpression)
                .setDestination(customTaskService.getDestination(taskType))
                .setPayLoad(getName())
                .scheduleImmediately(scheduleImmediately)
                .setFirstExecution(nextExecution)
                .setLogLevel(logLevel)
                .setNextRecurrentTasks(nextRecurrentTasks)
                .build();
        recurrentTask.set(task);
        Save.CREATE.save(dataModel, this);
    }

    RecurrentTask getRecurrentTask() {
        return recurrentTask.get();
    }

    @Override
    public Optional<Instant> getLastRun() {
        return Optional.ofNullable(lastRun);
    }

    @Override
    public void updateLastRun(Instant triggerTime) {
        lastRun = triggerTime;
        dataModel.mapper(ICustomTask.class).update(this, "lastRun");
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
    public History<CustomTask> getHistory() {
        List<JournalEntry<ICustomTask>> journal = dataModel.mapper(ICustomTask.class).getJournal(getId());
        return new History<>(journal, this);
    }

    private CustomTaskImpl init(String name, String taskType, ScheduleExpression scheduleExpression, Instant nextExecution, String application, int logLevel) {
        this.name = name;
        this.scheduleExpression = scheduleExpression;
        this.nextExecution = nextExecution;
        this.application = application;
        this.logLevel = logLevel;
        this.taskType = taskType;
        return this;
    }

    private class CannotDeleteWhileBusy extends CannotDeleteWhileBusyException {
        CannotDeleteWhileBusy() {
            super(CustomTaskImpl.this.thesaurus, MessageSeeds.CANNOT_DELETE_WHILE_RUNNING, CustomTaskImpl.this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomTaskImpl that = (CustomTaskImpl) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String getApplication() {
        return (recurrentTask.isPresent()) ? recurrentTask.get().getApplication() : application;
    }

    @Override
    public String getTaskType() {
        return this.taskType;
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
