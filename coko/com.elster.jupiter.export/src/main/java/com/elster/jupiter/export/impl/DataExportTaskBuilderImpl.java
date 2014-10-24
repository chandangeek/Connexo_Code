package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportTaskBuilder;
import com.elster.jupiter.export.ReadingTypeDataExportTask;
import com.elster.jupiter.export.ValidatedDataOption;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.util.time.ScheduleExpression;

import java.util.ArrayList;
import java.util.List;

class DataExportTaskBuilderImpl implements DataExportTaskBuilder {

    private final DataModel dataModel;

    private List<PropertyBuilderImpl> properties = new ArrayList<>();
    private ScheduleExpression scheduleExpression;
    private boolean scheduleImmediately;
    private String name;
    private String dataProcessor;
    private RelativePeriod exportPeriod;
    private RelativePeriod updatePeriod;
    private List<ReadingType> readingTypes = new ArrayList<>();
    private EndDeviceGroup endDeviceGroup;
    private ValidatedDataOption validatedDataOption;
    private boolean exportUpdate;
    private boolean exportContinuousData;

    public DataExportTaskBuilderImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public DataExportTaskBuilder setScheduleExpression(ScheduleExpression scheduleExpression) {
        this.scheduleExpression = scheduleExpression;
        return this;
    }

    @Override
    public DataExportTaskBuilder scheduleImmediately() {
        this.scheduleImmediately = true;
        return this;
    }

    @Override
    public DataExportTaskBuilder exportUpdate() {
        this.exportUpdate = true;
        return this;
    }

    @Override
    public DataExportTaskBuilder exportContinuousData() {
        this.exportContinuousData = true;
        return this;
    }

    @Override
    public ReadingTypeDataExportTask build() {
        ReadingTypeDataExportTaskImpl exportTask = ReadingTypeDataExportTaskImpl.from(dataModel, name, exportPeriod, dataProcessor, scheduleExpression, endDeviceGroup);
        exportTask.setScheduleImmediately(scheduleImmediately);
        exportTask.setUpdatePeriod(updatePeriod);
        exportTask.setValidatedDataOption(validatedDataOption);
        exportTask.setExportUpdate(exportUpdate);
        exportTask.setExportContinuousData(exportContinuousData);
        readingTypes.stream().forEach(exportTask::addReadingType);
        properties.stream().forEach(p -> exportTask.setProperty(p.name, p.value));
        return exportTask;
    }

    @Override
    public DataExportTaskBuilder setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public DataExportTaskBuilder setDataFormatter(String dataProcessor) {
        this.dataProcessor = dataProcessor;
        return this;
    }

    @Override
    public DataExportTaskBuilder setExportPeriod(RelativePeriod exportPeriod) {
        this.exportPeriod = exportPeriod;
        return this;
    }

    @Override
    public DataExportTaskBuilder setUpdatePeriod(RelativePeriod updatePeriod) {
        this.updatePeriod = updatePeriod;
        return this;
    }

    @Override
    public DataExportTaskBuilder addReadingType(ReadingType readingType) {
        this.readingTypes.add(readingType);
        return this;
    }

    @Override
    public DataExportTaskBuilder setValidatedDataOption(ValidatedDataOption validatedDataOption) {
        this.validatedDataOption = validatedDataOption;
        return this;
    }

    @Override
    public DataExportTaskBuilder setEndDeviceGroup(EndDeviceGroup endDeviceGroup) {
        this.endDeviceGroup = endDeviceGroup;
        return this;
    }

    @Override
    public PropertyBuilder addProperty(String name) {
        return new PropertyBuilderImpl(name);
    }

    private class PropertyBuilderImpl implements PropertyBuilder {
        private final String name;
        private Object value;

        private PropertyBuilderImpl(String name) {
            this.name = name;
        }

        @Override
        public DataExportTaskBuilder withValue(Object value) {
            this.value = value;
            properties.add(this);
            return DataExportTaskBuilderImpl.this;
        }
    }
}
