package com.elster.jupiter.export.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportProperty;
import com.elster.jupiter.export.DataExportStrategy;
import com.elster.jupiter.export.ValidatedDataOption;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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
    private final MeteringService meteringService;

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
    private List<DataExportProperty> properties = new ArrayList<>();
    private boolean exportUpdate;
    private boolean exportContinuousData;
    private ValidatedDataOption validatedDataOption;
    private List<ReadingTypeInExportTask> readingTypes = new ArrayList<>();

    private transient boolean scheduleImmediately;
    private transient ScheduleExpression scheduleExpression;

    @Inject
    ReadingTypeDataExportTaskImpl(DataModel dataModel, TaskService taskService, IDataExportService dataExportService, MeteringService meteringService) {
        this.taskService = taskService;
        this.dataModel = dataModel;
        this.dataExportService = dataExportService;
        this.meteringService = meteringService;
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
        return Collections.emptyList(); // TODO
    }

    @Override
    public Optional<? extends DataExportOccurrence> getLastOccurence() {
// TODO comment in when TABLESPECS HAVE BEEN DEFINED FOR  DataExportOccurrence
//       return dataModel.query(DataExportOccurrence.class).select(Operator.EQUAL.compare("taskOccurrence", this), new Order[] {Order.descending("startDate")},
//                false, new String[]{}, 1, 1). stream().findAny();
        return Optional.empty();
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
            Save.CREATE.save(dataModel, this);
        } else {
            Save.UPDATE.save(dataModel, this);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    public PropertySpec<?> getPropertySpec(String name) {
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
        return dataExportService.getDataProcessorFactory(dataProcessor).orElseThrow(IllegalArgumentException::new).getProperties();
    }

    @Override
    public ScheduleExpression getScheduleExpression() {
        return recurrentTask.get().getScheduleExpression();
    }
}
