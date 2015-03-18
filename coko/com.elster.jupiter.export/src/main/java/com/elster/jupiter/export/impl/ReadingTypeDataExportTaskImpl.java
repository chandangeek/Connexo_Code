package com.elster.jupiter.export.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.export.CannotDeleteWhileBusyException;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportOccurrenceFinder;
import com.elster.jupiter.export.DataExportProperty;
import com.elster.jupiter.export.DataExportStatus;
import com.elster.jupiter.export.DataExportStrategy;
import com.elster.jupiter.export.DataProcessorFactory;
import com.elster.jupiter.export.ReadingTypeDataExportTask;
import com.elster.jupiter.export.ValidatedDataOption;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.orm.JournalEntry;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.RecurrentTaskBuilder;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

@UniqueName(groups = {Save.Create.class, Save.Update.class})
class ReadingTypeDataExportTaskImpl implements IReadingTypeDataExportTask {

    private final TaskService taskService;
    private final DataModel dataModel;
    private final IDataExportService dataExportService;
    private final MeteringService meteringService;
    private final Thesaurus thesaurus;

    private long id;
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = Table.NAME_LENGTH, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_NAME_LENGTH + "}")
    private String name;
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    private Reference<RelativePeriod> exportPeriod = ValueReference.absent();
    private Reference<RelativePeriod> updatePeriod = ValueReference.absent();
    @NotNull
    @IsExistingProcessor
    private String dataProcessor;
    private Reference<RecurrentTask> recurrentTask = ValueReference.absent();
    private Reference<EndDeviceGroup> endDeviceGroup = ValueReference.absent();
    private boolean exportUpdate;
    private boolean exportContinuousData;
    private ValidatedDataOption validatedDataOption;
    private List<DataExportProperty> properties = new ArrayList<>();
    @Valid
    @Size(min=1, message = "{" + MessageSeeds.Keys.MUST_SELECT_AT_LEAST_ONE_READING_TYPE + "}")
    private List<ReadingTypeInExportTask> readingTypes = new ArrayList<>();
    private List<ReadingTypeDataExportItemImpl> exportItems = new ArrayList<>();

    private Instant lastRun;

    private transient boolean scheduleImmediately;
    private transient ScheduleExpression scheduleExpression;
    private transient boolean recurrentTaskDirty;
    private transient boolean propertiesDirty;
    private transient Instant nextExecution;

    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;

    @Inject
    ReadingTypeDataExportTaskImpl(DataModel dataModel, TaskService taskService, IDataExportService dataExportService, MeteringService meteringService, Thesaurus thesaurus) {
        this.taskService = taskService;
        this.dataModel = dataModel;
        this.dataExportService = dataExportService;
        this.meteringService = meteringService;
        this.thesaurus = thesaurus;
    }

    static ReadingTypeDataExportTaskImpl from(DataModel dataModel, String name, RelativePeriod exportPeriod, String dataProcessor, ScheduleExpression scheduleExpression, EndDeviceGroup endDeviceGroup, Instant nextExecution) {
        return dataModel.getInstance(ReadingTypeDataExportTaskImpl.class).init(name, exportPeriod, dataProcessor, scheduleExpression, endDeviceGroup, nextExecution);
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void activate() {
        //TODO automatically generated method body, provide implementation.

    }

    @Override
    public void deactivate() {
        //TODO automatically generated method body, provide implementation.

    }

    @Override
    public RelativePeriod getExportPeriod() {
        return exportPeriod.get();
    }

    @Override
    public Optional<RelativePeriod> getUpdatePeriod() {
        return updatePeriod.getOptional();
    }

    /*@Override
    public Optional<Instant> getLastRun() {
        return recurrentTask.get().getLastRun();
    }*/

    @Override
    public EndDeviceGroup getEndDeviceGroup() {
        return endDeviceGroup.get();
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties.stream()
                .collect(Collectors.toMap(DataExportProperty::getName, DataExportProperty::getValue));
    }

    @Override
    public List<DataExportProperty> getDataExportProperties() {
        return Collections.unmodifiableList(properties);
    }

    @Override
    public Instant getNextExecution() {
        return recurrentTask.get().getNextExecution();
    }

    @Override
    public List<? extends DataExportOccurrence> getOccurrences() {
        return dataModel.mapper(DataExportOccurrenceImpl.class).find("readingTask", this);
    }

    @Override
    public DataExportOccurrenceFinder getOccurrencesFinder() {
        Condition condition = where("readingTask").isEqualTo(this);
        Order order = Order.descending("taskocc");
        return new DataExportOccurrenceFinderImpl(dataModel, condition, order);
    }

    @Override
    public Optional<IDataExportOccurrence> getLastOccurrence() {
        return dataModel.query(IDataExportOccurrence.class, TaskOccurrence.class).select(Operator.EQUAL.compare("readingTask", this), new Order[]{Order.descending("taskocc")},
                false, new String[]{}, 1, 1).stream().findAny();
    }

    @Override
    public Optional<? extends DataExportOccurrence> getOccurrence(Long id) {
        return dataModel.mapper(DataExportOccurrenceImpl.class).getOptional(id).filter(occ -> this.getId() == occ.getTask().getId());
    }

    @Override
    public DataExportStrategy getStrategy() {
        return new DataExportStrategyImpl(exportUpdate, exportContinuousData, validatedDataOption);
    }

    @Override
    public Set<ReadingType> getReadingTypes() {
        return readingTypes.stream()
                .map(ReadingTypeInExportTask::getReadingType)
                .collect(Collectors.toSet());
    }

    @Override
    public void save() {
        Optional<DataProcessorFactory> optional = dataExportService.getDataProcessorFactory(dataProcessor);
        if (optional.isPresent()) {
            DataProcessorFactory dataProcessorFactory = optional.get();
            dataProcessorFactory.validateProperties(properties);
        }
        if (id == 0) {
            persist();
        } else {
            update();
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
        readingTypes.clear();
        exportItems.clear();
        dataModel.remove(this);
        dataModel.mapper(DataExportOccurrence.class).remove(getOccurrences());
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
                .map(DataExportOccurrence::getStatus)
                .orElse(DataExportStatus.SUCCESS)
                .equals(DataExportStatus.BUSY);
    }

    private boolean hasQueuedMessages() {
        Optional<? extends TaskOccurrence> lastOccurrence = recurrentTask.get().getLastOccurrence();
        Optional<IDataExportOccurrence> lastDataExportOccurrence = getLastOccurrence();
        return lastOccurrence.isPresent() &&
                lastDataExportOccurrence.map(IDataExportOccurrence::getTaskOccurrence)
                        .map(TaskOccurrence::getId)
                        .map(i -> !i.equals(lastOccurrence.get().getId()))
                        .orElse(true);
    }

    @Override
    public boolean isActive() {
        return recurrentTask.get().getNextExecution() != null;
    }

    @Override
    public String getDataFormatter() {
        return dataProcessor;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return dataExportService.getDataProcessorFactory(dataProcessor).orElseThrow(()->new IllegalArgumentException("No such data processor: "+dataProcessor)).getProperties();
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
    public List<IReadingTypeDataExportItem> getExportItems() {
        return Collections.unmodifiableList(exportItems);
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
    }

    @Override
    public void setExportPeriod(RelativePeriod relativePeriod) {
        this.exportPeriod.set(relativePeriod);
    }


    @Override
    public void setProperty(String name, Object value) {
        DataExportProperty dataExportProperty = properties.stream()
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .orElseGet(() -> {
                    DataExportPropertyImpl property = DataExportPropertyImpl.from(dataModel, this, name, value);
                    properties.add(property);
                    return property;
                });
        dataExportProperty.setValue(value);
        propertiesDirty = true;
    }

    @Override
    public void setEndDeviceGroup(EndDeviceGroup endDeviceGroup) {
        this.endDeviceGroup.set(endDeviceGroup);
    }

    @Override
    public void removeReadingType(ReadingType readingType) {
        this.readingTypes.removeIf(r -> r.getReadingType().equals(readingType));
    }

    @Override
    public void addReadingType(ReadingType readingType) {
        if (getReadingTypes().contains(readingType)) {
            return;
        }
        readingTypes.add(ReadingTypeInExportTask.from(dataModel, this, readingType));

    }

    @Override
    public void addReadingType(String mRID) {
        readingTypes.add(toReadingTypeInExportTask(mRID));
    }

    @Override
    public void setUpdatePeriod(RelativePeriod updatePeriod) {
        this.updatePeriod.set(updatePeriod);
    }

    public PropertySpec getPropertySpec(String name) {
        return getPropertySpecs().stream()
                .filter(p -> name.equals(p.getName()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getDisplayName(String name) {
        return properties.stream()
                .filter(p -> p.getName().equals(name))
                .findAny()
                .map(DataExportProperty::getDisplayName)
                .orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public void setScheduleImmediately(boolean scheduleImmediately) {
        this.scheduleImmediately = scheduleImmediately;
    }

    @Override
    public void setValidatedDataOption(ValidatedDataOption validatedDataOption) {
        this.validatedDataOption = validatedDataOption;
    }

    @Override
    public void setExportContinuousData(boolean exportContinuousData) {
        this.exportContinuousData = exportContinuousData;
    }

    @Override
    public void setExportUpdate(boolean exportUpdate) {
        this.exportUpdate = exportUpdate;
    }

    public IReadingTypeDataExportItem addExportItem(Meter meter, ReadingType readingType) {
        ReadingTypeDataExportItemImpl item = ReadingTypeDataExportItemImpl.from(dataModel, this, meter, readingType);
        exportItems.add(item);
        return item;
    }

    @Override
    public void triggerNow() {
        recurrentTask.get().triggerNow();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Map<String, Object> getProperties(Instant at) {
        List<JournalEntry<DataExportProperty>> props = dataModel.mapper(DataExportProperty.class).at(at).find(ImmutableMap.of("task", this));
        return props.stream()
                .map(JournalEntry::get)
                .collect(Collectors.toMap(DataExportProperty::getName, DataExportProperty::getValue));
    }

    @Override
    public Set<ReadingType> getReadingTypes(Instant at) {
        List<JournalEntry<ReadingTypeInExportTask>> readingTypes = dataModel.mapper(ReadingTypeInExportTask.class).at(at).find(ImmutableMap.of("readingTypeDataExportTask", this));
        return readingTypes.stream()
                .map(JournalEntry::get)
                .map(ReadingTypeInExportTask::getReadingType)
                .collect(Collectors.toSet());
    }

    private void update() {
        if (recurrentTaskDirty) {
            recurrentTask.get().save();
        }
        if (propertiesDirty) {
            properties.forEach(DataExportProperty::save);
        }
        Save.UPDATE.save(dataModel, this);
    }

    private void persist() {
        RecurrentTaskBuilder builder = taskService.newBuilder()
                .setName(getName())
                .setScheduleExpression(scheduleExpression)
                .setDestination(dataExportService.getDestination())
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

    private ReadingTypeDataExportTaskImpl init(String name, RelativePeriod exportPeriod, String dataProcessor, ScheduleExpression scheduleExpression, EndDeviceGroup endDeviceGroup, Instant nextExecution) {
        setName(name);
        this.name = name;
        this.exportPeriod.set(exportPeriod);
        this.dataProcessor = dataProcessor;
        this.scheduleExpression = scheduleExpression;
        this.endDeviceGroup.set(endDeviceGroup);
        this.nextExecution = nextExecution;
        return this;
    }

    private ReadingTypeInExportTask toReadingTypeInExportTask(String mRID) {
        return meteringService.getReadingType(mRID)
                .map(r -> ReadingTypeInExportTask.from(dataModel, this, r))
                .orElseGet(() -> readingTypeInValidationRuleFor(mRID));
    }

    private ReadingTypeInExportTask readingTypeInValidationRuleFor(String mRID) {
        ReadingTypeInExportTask empty = ReadingTypeInExportTask.from(dataModel, this, mRID);
        if (getId() != 0) {
            Save.UPDATE.validate(dataModel, empty);
        }
        return empty;
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
        save();
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

    public History<ReadingTypeDataExportTask> getHistory() {
        List<JournalEntry<IReadingTypeDataExportTask>> journal = dataModel.mapper(IReadingTypeDataExportTask.class).getJournal(getId());
        return new History<>(journal, this);
    }

    private class CannotDeleteWhileBusy extends CannotDeleteWhileBusyException {
        CannotDeleteWhileBusy() {
            super(ReadingTypeDataExportTaskImpl.this.thesaurus, MessageSeeds.CANNOT_DELETE_WHILE_RUNNING, ReadingTypeDataExportTaskImpl.this);
        }
    }

}