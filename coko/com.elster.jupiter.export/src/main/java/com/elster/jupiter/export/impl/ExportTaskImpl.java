package com.elster.jupiter.export.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.export.CannotDeleteWhileBusyException;
import com.elster.jupiter.export.DataExportDestination;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportOccurrenceFinder;
import com.elster.jupiter.export.DataExportProperty;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataExportStatus;
import com.elster.jupiter.export.EmailDestination;
import com.elster.jupiter.export.EventDataSelector;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.export.FileDestination;
import com.elster.jupiter.export.FtpDestination;
import com.elster.jupiter.export.FtpsDestination;
import com.elster.jupiter.export.StandardDataSelector;
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
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.DUPLICATE_EXPORT_TASK + "}")
final class ExportTaskImpl implements IExportTask {
    private final TaskService taskService;
    private final DataModel dataModel;
    private final IDataExportService dataExportService;
    private final Thesaurus thesaurus;

    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = Table.NAME_LENGTH, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_MIN_AND_MAX + "}")
    protected String name;
    @NotNull
    @IsExistingFormatter
    private String dataFormatter;
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
    private Reference<IStandardDataSelector> readingTypeDataSelector = Reference.empty();
    @Valid
    private List<IDataExportDestination> destinations = new ArrayList<>();

    @Inject
    ExportTaskImpl(DataModel dataModel, IDataExportService dataExportService, TaskService taskService, Thesaurus thesaurus) {
        this.dataExportService = dataExportService;
        this.dataModel = dataModel;
        this.taskService = taskService;
        this.thesaurus = thesaurus;
    }

    static ExportTaskImpl from(DataModel dataModel, String name, String dataFormatter, String dataSelector, ScheduleExpression scheduleExpression, Instant nextExecution) {
        return dataModel.getInstance(ExportTaskImpl.class).init(name, dataFormatter, dataSelector, scheduleExpression, nextExecution);
    }

    @Override
    public long getId() {
        return id;
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
    public void update() {
        doSave();
    }

    void doSave() {
        // TODO  : separate properties per Factory

        List<PropertySpec> propertiesSpecsForProcessor = dataExportService.getPropertiesSpecsForFormatter(dataFormatter);
        List<PropertySpec> propertiesSpecsForDataSelector = dataExportService.getPropertiesSpecsForDataSelector(dataSelector);
        List<DataExportProperty> processorProperties = new ArrayList<DataExportProperty>();
        List<DataExportProperty> selectorProperties = new ArrayList<DataExportProperty>();
        for (DataExportProperty property : properties) {
            for (PropertySpec processorPropertySpec : propertiesSpecsForProcessor) {
                if (property.instanceOfSpec(processorPropertySpec)) {
                    processorProperties.add(property);
                }
            }
        }
        for (DataExportProperty property : properties) {
            for (PropertySpec selectorPropertySpec : propertiesSpecsForDataSelector) {
                if (property.instanceOfSpec(selectorPropertySpec)) {
                    selectorProperties.add(property);
                }
            }
        }

        dataExportService.getDataFormatterFactory(dataFormatter)
                .ifPresent(dataProcessorFactory -> dataProcessorFactory.validateProperties(processorProperties));
        dataExportService.getDataSelectorFactory(dataSelector)
                .ifPresent(dataSelectorFactory -> dataSelectorFactory.validateProperties(selectorProperties));
        if (id == 0) {
            persist();
        } else {
            doUpdate();
        }
        readingTypeDataSelector.getOptional().ifPresent(StandardDataSelector::save);
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
        destinations.clear();
        readingTypeDataSelector.getOptional().ifPresent(IStandardDataSelector::delete);
        dataModel.mapper(DataExportOccurrence.class).remove(getOccurrences());
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
        return dataFormatter;
    }

    @Override
    public String getDataSelector() {
        return dataSelector;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> allSpecs = new ArrayList<>();
        allSpecs.addAll(getDataProcessorPropertySpecs());
        allSpecs.addAll(getDataSelectorPropertySpecs());
        return allSpecs;
    }

    @Override
    public List<PropertySpec> getDataSelectorPropertySpecs() {
        return dataExportService.getDataSelectorFactory(dataSelector).orElseThrow(() -> new IllegalArgumentException("No such data selector: " + dataSelector)).getPropertySpecs();
    }

    @Override
    public List<PropertySpec> getDataProcessorPropertySpecs() {
        return dataExportService.getDataFormatterFactory(dataFormatter).orElseThrow(() -> new IllegalArgumentException("No such data processor: " + dataFormatter)).getPropertySpecs();
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


    public void removeProperty(PropertySpec propertySpec) {
        Optional<DataExportProperty> dataExportProperty = properties.stream()
                .filter(p -> (p.instanceOfSpec(propertySpec) && p.getName().equals(propertySpec.getName())))
                .findFirst();
        if (dataExportProperty.isPresent()) {
            properties.remove(dataExportProperty.get());
            propertiesDirty = true;
        }
    }


    @Override
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

    private void doUpdate() {
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
                .setPayLoad(getName())
                .scheduleImmediately(scheduleImmediately)
                .setFirstExecution(nextExecution);
        RecurrentTask task = builder.build();
        recurrentTask.set(task);
        if (DataExportService.STANDARD_READINGTYPE_DATA_SELECTOR.equals(dataSelector)) {
            Save.CREATE.validate(dataModel, this, StandardDataSelector.class);
        }
        if (DataExportService.STANDARD_EVENT_DATA_SELECTOR.equals(dataSelector)) {
            Save.CREATE.validate(dataModel, this, EventDataSelector.class);
        }
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
        dataModel.mapper(IExportTask.class).update(this, "lastRun");
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
    public History<ExportTask> getHistory() {
        List<JournalEntry<IExportTask>> journal = dataModel.mapper(IExportTask.class).getJournal(getId());
        return new History<>(journal, this);
    }

    @Override
    public Optional<IStandardDataSelector> getReadingTypeDataSelector() {
        return readingTypeDataSelector.getOptional()
                .filter(selector -> DataExportService.STANDARD_READINGTYPE_DATA_SELECTOR.equals(dataSelector));
    }

    @Override
    public Optional<EventDataSelector> getEventDataSelector() {
        return readingTypeDataSelector.getOptional()
                .map(EventDataSelector.class::cast)
                .filter(selector -> DataExportService.STANDARD_EVENT_DATA_SELECTOR.equals(dataSelector));
    }

    @Override
    public Optional<StandardDataSelector> getReadingTypeDataSelector(Instant at) {
        return getReadingTypeDataSelector().flatMap(selector -> selector.getHistory().getVersionAt(at));
    }

    DataModel getDataModel() {
        return dataModel;
    }

    private ExportTaskImpl init(String name, String dataFormatter, String dataSelector, ScheduleExpression scheduleExpression, Instant nextExecution) {
        setName(name);
        this.dataFormatter = dataFormatter;
        this.dataSelector = dataSelector;
        this.scheduleExpression = scheduleExpression;
        this.nextExecution = nextExecution;
        return this;
    }

    @Override
    public void setDataFormatter(String formatter) {
        this.dataFormatter = formatter;
    }

    @Override
    public void setReadingTypeDataSelector(StandardDataSelectorImpl readingTypeDataSelector) {
        this.readingTypeDataSelector.set(readingTypeDataSelector);
    }

    @Override
    public void setEventDataSelector(StandardDataSelectorImpl eventDataSelector) {
        this.readingTypeDataSelector.set(eventDataSelector);
    }

    @Override
    public FileDestination addFileDestination(String fileLocation, String fileName, String fileExtension) {
        FileDestinationImpl fileDestination = dataModel.getInstance(FileDestinationImpl.class).init(this, fileLocation, fileName, fileExtension);
        destinations.add(fileDestination);
        doSave();
        return fileDestination;
    }

    @Override
    public EmailDestination addEmailDestination(String recipients, String subject, String attachmentName, String attachmentExtension) {
        EmailDestinationImpl emailDestination = EmailDestinationImpl.from(this, dataModel, recipients, subject, attachmentName, attachmentExtension);
        destinations.add(emailDestination);
        doSave();
        return emailDestination;
    }

    @Override
    public FtpDestination addFtpDestination(String server, int port, String user, String password, String fileLocation, String fileName, String fileExtension) {
        FtpDestinationImpl ftpDestination = FtpDestinationImpl.from(this, dataModel, server, port, user, password, fileLocation, fileName, fileExtension);
        destinations.add(ftpDestination);
        doSave();
        return ftpDestination;
    }

    @Override
    public FtpsDestination addFtpsDestination(String server, int port, String user, String password, String fileLocation, String fileName, String fileExtension) {
        FtpsDestinationImpl ftpsDestination = FtpsDestinationImpl.from(this, dataModel, server, port, user, password, fileLocation, fileName, fileExtension);
        destinations.add(ftpsDestination);
        doSave();
        return ftpsDestination;
    }

    @Override
    public void removeDestination(DataExportDestination destination) {
        destinations.remove(destination);
        doSave();
    }

    @Override
    public List<DataExportDestination> getDestinations() {
        return Collections.unmodifiableList(destinations);
    }

    @Override
    public List<DataExportDestination> getDestinations(Instant at) {
        List<JournalEntry<IDataExportDestination>> props = dataModel.mapper(IDataExportDestination.class).at(at).find(ImmutableMap.of("task", this));
        return props.stream()
                .map(JournalEntry::get)
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasDefaultSelector() {
        return readingTypeDataSelector.isPresent();
    }

    @Override
    public Destination getCompositeDestination() {
        return new CompositeDataExportDestination(destinations);
    }

    private class CannotDeleteWhileBusy extends CannotDeleteWhileBusyException {
        CannotDeleteWhileBusy() {
            super(ExportTaskImpl.this.thesaurus, MessageSeeds.CANNOT_DELETE_WHILE_RUNNING, ExportTaskImpl.this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExportTaskImpl that = (ExportTaskImpl) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
