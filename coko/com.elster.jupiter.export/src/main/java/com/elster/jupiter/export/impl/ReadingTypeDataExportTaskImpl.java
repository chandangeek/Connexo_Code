package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportProperty;
import com.elster.jupiter.export.DataExportStrategy;
import com.elster.jupiter.export.DataProcessor;
import com.elster.jupiter.export.NoSuchDataProcessorException;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.export.ValidatedDataOption;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.RecurrentTaskBuilder;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

class ReadingTypeDataExportTaskImpl implements IReadingTypeDataExportTask {

    private final TaskService taskService;
    private final DataModel dataModel;
    private final IDataExportService dataExportService;
    private final DataExportStrategyImpl dataExportStrategy = new DataExportStrategyImpl();

    private long id;
    private String name;
    private Reference<RelativePeriod> exportPeriod = ValueReference.absent();
    private Reference<RelativePeriod> updatePeriod = ValueReference.absent();
    private String dataProcessor;
    private Reference<RecurrentTask> recurrentTask = ValueReference.absent();
    private Reference<EndDeviceGroup> endDeviceGroup = ValueReference.absent();
    private List<DataExportProperty> properties = new ArrayList<>();
    private boolean exportUpdate;
    private boolean exportContinuousData;
    private ValidatedDataOption validatedDataOption;
    private List<ReadingTypeInExportTask> readingTypes = new ArrayList<>();
    private List<ReadingTypeDataExportItemImpl> exportItems = new ArrayList<>();

    private transient boolean scheduleImmediately;
    private transient ScheduleExpression scheduleExpression;

    @Inject
    ReadingTypeDataExportTaskImpl(DataModel dataModel, TaskService taskService, IDataExportService dataExportService) {
        this.taskService = taskService;
        this.dataModel = dataModel;
        this.dataExportService = dataExportService;
    }

    static ReadingTypeDataExportTaskImpl from(DataModel dataModel, String name, RelativePeriod exportPeriod, String dataProcessor, ScheduleExpression scheduleExpression, EndDeviceGroup endDeviceGroup) {
        return dataModel.getInstance(ReadingTypeDataExportTaskImpl.class).init(name, exportPeriod, dataProcessor, scheduleExpression, endDeviceGroup);
    }

    private ReadingTypeDataExportTaskImpl init(String name, RelativePeriod exportPeriod, String dataProcessor, ScheduleExpression scheduleExpression, EndDeviceGroup endDeviceGroup) {
        this.name = name;
        this.exportPeriod.set(exportPeriod);
        this.dataProcessor = dataProcessor;
        this.scheduleExpression = scheduleExpression;
        this.endDeviceGroup.set(endDeviceGroup);

        return this;
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
    public void execute(DataExportOccurrence occurrence, Logger logger) {
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

    @Override
    public Optional<Instant> getLastRun() {
        return recurrentTask.get().getLastRun();
    }

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
    public Instant getNextExecution() {
        return recurrentTask.get().getNextExecution();
    }

    @Override
    public List<? extends DataExportOccurrence> getOccurrences(Range<Instant> interval) {
        return dataModel.mapper(DataExportOccurrenceImpl.class).find("readingTask", this);
    }

    RecurrentTask getRecurrentTask() {
        return recurrentTask.get();
    }

    @Override
    public DataExportStrategy getStrategy() {
        return dataExportStrategy;
    }

    @Override
    public Set<ReadingType> getReadingTypes() {
        return readingTypes.stream()
                .map(ReadingTypeInExportTask::getReadingType)
                .collect(Collectors.toSet());
    }

    @Override
    public void save() {
        if (id == 0) {
            RecurrentTaskBuilder builder = taskService.newBuilder()
                    .setName(getName())
                    .setScheduleExpression(scheduleExpression)
                    .setDestination(dataExportService.getDestination())
                    .setPayLoad(getName());
            if (scheduleImmediately) {
                builder.scheduleImmediately();
            }
            RecurrentTask task = builder.build();
            task.save();
            recurrentTask.set(task);
            dataModel.persist(this);
        } else {
            dataModel.update(this);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    public PropertySpec<?> getPropertySpec(String name) {
        return getTemplateDataProcessor(getDataFormatter()).getPropertySpecs().stream()
                .filter(p -> name.equals(p.getName()))
                .findFirst()
                .orElse(null);
    }

    private DataProcessor getTemplateDataProcessor(String name) {
        return dataExportService.getDataProcessorFactory(name)
                .orElseThrow(NoSuchDataProcessorException::new)
                .createTemplateDataFormatter();
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
    public void addReadingType(ReadingType readingType) {
        if (getReadingTypes().contains(readingType)) {
            return;
        }
        readingTypes.add(ReadingTypeInExportTask.from(dataModel, this, readingType));

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
    }

    @Override
    public void setScheduleImmediately(boolean scheduleImmediately) {
        this.scheduleImmediately = scheduleImmediately;
    }

    @Override
    public void setUpdatePeriod(RelativePeriod updatePeriod) {
        this.updatePeriod.set(updatePeriod);
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

    @Override
    public List<ReadingTypeDataExportItem> getExportItems() {
        return Collections.unmodifiableList(exportItems);
    }

    public void addExportItem(Meter meter, String readingTypeMRId) {
        ReadingTypeDataExportItemImpl item = ReadingTypeDataExportItemImpl.from(dataModel, this, meter, readingTypeMRId);
        exportItems.add(item);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReadingTypeDataExportTaskImpl that = (ReadingTypeDataExportTaskImpl) o;

        if (id != that.id) return false;
        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + name.hashCode();
        return result;
    }

    private class DataExportStrategyImpl implements DataExportStrategy {
        @Override
        public boolean isExportUpdate() {
            return exportUpdate;
        }

        @Override
        public boolean isExportContinuousData() {
            return exportContinuousData;
        }

        @Override
        public ValidatedDataOption getValidatedDataOption() {
            return validatedDataOption;
        }
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
    public List<PropertySpec<?>> getPropertySpecs() {
        return getTemplateDataProcessor(getDataFormatter()).getPropertySpecs();
    }
}
