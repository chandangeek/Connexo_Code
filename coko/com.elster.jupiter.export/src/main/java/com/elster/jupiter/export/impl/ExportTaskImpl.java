package com.elster.jupiter.export.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.export.CannotDeleteWhileBusyException;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportOccurrenceFinder;
import com.elster.jupiter.export.DataExportProperty;
import com.elster.jupiter.export.DataExportStatus;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.export.ReadingTypeDataSelector;
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
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

public class ExportTaskImpl implements IExportTask {
    private final TaskService taskService;
    private final DataModel dataModel;
    private final IDataExportService dataExportService;
    private final Thesaurus thesaurus;

    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = Table.NAME_LENGTH, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_NAME_LENGTH + "}")
    protected String name;
    @NotNull
    @IsExistingProcessor
    private String dataProcessor;
    private String dataSelector;
    private transient ScheduleExpression scheduleExpression;
    private transient Instant nextExecution;
    private long id;
    private Reference<RecurrentTask> recurrentTask = ValueReference.absent();
    private List<DataExportProperty> properties = new ArrayList<>();
    private Instant lastRun;
    private transient boolean scheduleImmediately;
    private transient boolean recurrentTaskDirty;
    private transient boolean propertiesDirty;
    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;
    @Valid
    private Reference<IReadingTypeDataSelector> readingTypeDataSelector = Reference.empty();

    @Inject
    ExportTaskImpl(DataModel dataModel, IDataExportService dataExportService, TaskService taskService, Thesaurus thesaurus) {
        this.dataExportService = dataExportService;
        this.dataModel = dataModel;
        this.taskService = taskService;
        this.thesaurus = thesaurus;
    }

    static ExportTaskImpl from(DataModel dataModel, String name, String dataProcessor, String dataSelector, ScheduleExpression scheduleExpression, Instant nextExecution) {
        return dataModel.getInstance(ExportTaskImpl.class).init(name, dataProcessor, dataSelector, scheduleExpression, nextExecution);
    }

    public long getId() {
        return id;
    }

    public void activate() {
        //TODO automatically generated method body, provide implementation.

    }

    public void deactivate() {
        //TODO automatically generated method body, provide implementation.

    }

    public Map<String, Object> getProperties() {
        return properties.stream()
                .collect(Collectors.toMap(DataExportProperty::getName, DataExportProperty::getValue));
    }

    public List<DataExportProperty> getDataExportProperties() {
        return Collections.unmodifiableList(properties);
    }

    public Instant getNextExecution() {
        return recurrentTask.get().getNextExecution();
    }

    public List<? extends DataExportOccurrence> getOccurrences() {
        return dataModel.mapper(DataExportOccurrenceImpl.class).find("readingTask", this);
    }

    public DataExportOccurrenceFinder getOccurrencesFinder() {
        Condition condition = where("readingTask").isEqualTo(this);
        Order order = Order.descending("taskocc");
        return new DataExportOccurrenceFinderImpl(dataModel, condition, order);
    }

    public Optional<IDataExportOccurrence> getLastOccurrence() {
        return dataModel.query(IDataExportOccurrence.class, TaskOccurrence.class).select(Operator.EQUAL.compare("readingTask", this), new Order[]{Order.descending("taskocc")},
                false, new String[]{}, 1, 1).stream().findAny();
    }

    public Optional<? extends DataExportOccurrence> getOccurrence(Long id) {
        return dataModel.mapper(DataExportOccurrenceImpl.class).getOptional(id).filter(occ -> this.getId() == occ.getTask().getId());
    }

    public void save() {
        // TODO  : separate properties per Factory

        List<PropertySpec> propertiesSpecsForProcessor = dataExportService.getPropertiesSpecsForProcessor(dataProcessor);
        List<PropertySpec> propertiesSpecsForDataSelector = dataExportService.getPropertiesSpecsForDataSelector(dataSelector);
        List<DataExportProperty> processorProperties = new ArrayList<DataExportProperty>();
        List<DataExportProperty> selectorProperties = new ArrayList<DataExportProperty>();
        for (DataExportProperty property : properties) {
            for (PropertySpec processorPropertySpec : propertiesSpecsForProcessor)   {
                if (property.instanceOfSpec(processorPropertySpec)) {
                    processorProperties.add(property);
                }
            }
        }
        for (DataExportProperty property : properties) {
            for (PropertySpec selectorPropertySpec : propertiesSpecsForDataSelector)   {
                if (property.instanceOfSpec(selectorPropertySpec)) {
                    selectorProperties.add(property);
                }
            }
        }

        dataExportService.getDataProcessorFactory(dataProcessor)
                .ifPresent(dataProcessorFactory -> dataProcessorFactory.validateProperties(processorProperties));
        dataExportService.getDataSelectorFactory(dataSelector)
                .ifPresent(dataSelectorFactory -> dataSelectorFactory.validateProperties(selectorProperties));
        if (id == 0) {
            persist();
        } else {
            update();
        }
        readingTypeDataSelector.getOptional().ifPresent(ReadingTypeDataSelector::save);
        recurrentTaskDirty = false;
        propertiesDirty = false;
    }

    public void delete() {
        if (id == 0) {
            return;
        }
        if (!canBeDeleted()) {
            throw new CannotDeleteWhileBusy();
        }
        properties.clear();
        clearChildrenForDelete();
        dataModel.mapper(DataExportOccurrence.class).remove(getOccurrences());
        dataModel.remove(this);
        if (recurrentTask.isPresent()) {
            recurrentTask.get().delete();
        }
    }

    void clearChildrenForDelete() {
        readingTypeDataSelector.getOptional().ifPresent(dataSelector -> dataSelector.delete());
    }

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

    public boolean isActive() {
        return recurrentTask.get().getNextExecution() != null;
    }

    public String getDataFormatter() {
        return dataProcessor;
    }

    @Override
    public String getDataSelector() {
        return dataSelector;
    }

    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> allSpecs = new ArrayList<PropertySpec>();
        allSpecs.addAll(getDataProcessorPropertySpecs());
        allSpecs.addAll(getDataSelectorPropertySpecs());
        return allSpecs;
    }

    @Override
    public List getDataSelectorPropertySpecs() {
        return dataExportService.getDataSelectorFactory(dataSelector).orElseThrow(()->new IllegalArgumentException("No such data selector: "+dataSelector)).getPropertySpecs();
    }

    @Override
    public List getDataProcessorPropertySpecs() {
        return dataExportService.getDataProcessorFactory(dataProcessor).orElseThrow(()->new IllegalArgumentException("No such data processor: "+dataProcessor)).getPropertySpecs();
    }

    public ScheduleExpression getScheduleExpression() {
        return recurrentTask.get().getScheduleExpression();
    }

    public Optional<ScheduleExpression> getScheduleExpression(Instant at) {
        return recurrentTask.get().getHistory().getVersionAt(at).map(RecurrentTask::getScheduleExpression);
    }

    public void setNextExecution(Instant instant) {
        this.recurrentTask.get().setNextExecution(instant);
        recurrentTaskDirty = true;
    }

    public void setScheduleExpression(ScheduleExpression scheduleExpression) {
        this.recurrentTask.get().setScheduleExpression(scheduleExpression);
        recurrentTaskDirty = true;
    }

    public void setName(String name) {
        this.name = (name != null ? name.trim() : "");
    }

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

    public PropertySpec getPropertySpec(String name) {
        return getPropertySpecs().stream()
                .filter(p -> name.equals(p.getName()))
                .findFirst()
                .orElse(null);
    }

    public String getDisplayName(String name) {
        return properties.stream()
                .filter(p -> p.getName().equals(name))
                .findAny()
                .map(DataExportProperty::getDisplayName)
                .orElseThrow(IllegalArgumentException::new);
    }

    public void setScheduleImmediately(boolean scheduleImmediately) {
        this.scheduleImmediately = scheduleImmediately;
    }

    public void triggerNow() {
        recurrentTask.get().triggerNow();
    }

    public String getName() {
        return name;
    }

    public Map<String, Object> getProperties(Instant at) {
        List<JournalEntry<DataExportProperty>> props = dataModel.mapper(DataExportProperty.class).at(at).find(ImmutableMap.of("task", this));
        return props.stream()
                .map(JournalEntry::get)
                .collect(Collectors.toMap(DataExportProperty::getName, DataExportProperty::getValue));
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
                .setName(UUID.randomUUID().toString())
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

    RecurrentTask getRecurrentTask() {
        return recurrentTask.get();
    }

    public Optional<Instant> getLastRun() {
        return Optional.ofNullable(lastRun);
    }

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

    public History<ExportTask> getHistory() {
        List<JournalEntry<IExportTask>> journal = dataModel.mapper(IExportTask.class).getJournal(getId());
        return new History<>(journal, this);
    }

    @Override
    public Optional<ReadingTypeDataSelector> getReadingTypeDataSelector() {
        return readingTypeDataSelector.getOptional().map(ReadingTypeDataSelector.class::cast);
    }

    @Override
    public Optional<ReadingTypeDataSelector> getReadingTypeDataSelector(Instant at) {
        return getReadingTypeDataSelector().flatMap(selector -> selector.getHistory().getVersionAt(at));
    }

    DataModel getDataModel() {
        return dataModel;
    }

    private ExportTaskImpl init(String name, String dataProcessor, String dataSelector, ScheduleExpression scheduleExpression, Instant nextExecution) {
        setName(name);
        this.dataProcessor = dataProcessor;
        this.dataSelector = dataSelector;
        this.scheduleExpression = scheduleExpression;
        this.nextExecution = nextExecution;
        return this;
    }

    @Override
    public void setReadingTypeDataSelector(ReadingTypeDataSelectorImpl readingTypeDataSelector) {
        this.readingTypeDataSelector.set(readingTypeDataSelector);

    }

    private class CannotDeleteWhileBusy extends CannotDeleteWhileBusyException {
        CannotDeleteWhileBusy() {
            super(ExportTaskImpl.this.thesaurus, MessageSeeds.CANNOT_DELETE_WHILE_RUNNING, ExportTaskImpl.this);
        }
    }
}
