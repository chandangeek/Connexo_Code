package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataExportTaskBuilder;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.export.ReadingTypeDataSelector;
import com.elster.jupiter.export.ValidatedDataOption;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.util.time.ScheduleExpression;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

class DataExportTaskBuilderImpl implements DataExportTaskBuilder {

    private final DataModel dataModel;

    private boolean defaultSelector = true;
    private List<PropertyBuilderImpl> properties = new ArrayList<>();
    private ScheduleExpression scheduleExpression;
    private Instant nextExecution;
    private boolean scheduleImmediately;
    private String name;
    private String dataSelector = DataExportService.STANDARD_DATA_SELECTOR;
    private String dataFormatter;
    private RelativePeriod exportPeriod;
    private RelativePeriod updatePeriod;
    private RelativePeriod updateWindow;
    private List<ReadingTypeDefinition> readingTypes = new ArrayList<>();
    private EndDeviceGroup endDeviceGroup;
    private ValidatedDataOption validatedDataOption;
    private boolean exportUpdate;
    private boolean exportContinuousData;
    private boolean exportComplete;

    private interface ReadingTypeDefinition {
        public void addTo(ReadingTypeDataSelector readingTypeDataSelector);
    }

    public class ReadingTypeByMrid implements ReadingTypeDefinition {
        private final String mrid;

        public ReadingTypeByMrid(String mrid) {
            this.mrid = mrid;
        }

        @Override
        public void addTo(ReadingTypeDataSelector dataSelector) {
            dataSelector.addReadingType(mrid);
        }
    }

    public class ReadingTypeHolder implements ReadingTypeDefinition {

        private final ReadingType readingType;

        public ReadingTypeHolder(ReadingType readingType) {
            this.readingType = readingType;
        }

        @Override
        public void addTo(ReadingTypeDataSelector task) {
            task.addReadingType(readingType);
        }
    }


    public DataExportTaskBuilderImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public DataExportTaskBuilderImpl setScheduleExpression(ScheduleExpression scheduleExpression) {
        this.scheduleExpression = scheduleExpression;
        return this;
    }

    @Override
    public DataExportTaskBuilderImpl setNextExecution(Instant nextExecution) {
        this.nextExecution = nextExecution;
        return this;
    }

    @Override
    public DataExportTaskBuilderImpl scheduleImmediately() {
        this.scheduleImmediately = true;
        return this;
    }

    @Override
    public ExportTask build() {
        ExportTaskImpl exportTask = ExportTaskImpl.from(dataModel, name, dataFormatter, dataSelector, scheduleExpression, nextExecution);
        exportTask.setScheduleImmediately(scheduleImmediately);
        if (defaultSelector) {
            ReadingTypeDataSelectorImpl readingTypeDataSelector = ReadingTypeDataSelectorImpl.from(dataModel, exportTask, exportPeriod, endDeviceGroup);
            readingTypeDataSelector.setUpdatePeriod(updatePeriod);
            readingTypeDataSelector.setValidatedDataOption(validatedDataOption);
            readingTypeDataSelector.setExportUpdate(exportUpdate);
            readingTypeDataSelector.setExportContinuousData(exportContinuousData);
            readingTypeDataSelector.setUpdateWindow(updateWindow);
            readingTypeDataSelector.setExportOnlyIfComplete(exportComplete);
            readingTypes.stream().forEach(d -> d.addTo(readingTypeDataSelector));
            exportTask.setReadingTypeDataSelector(readingTypeDataSelector);
        }
        properties.stream().forEach(p -> exportTask.setProperty(p.name, p.value));
        return exportTask;
    }

    @Override
    public DataExportTaskBuilderImpl setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public DataExportTaskBuilderImpl setDataFormatterName(String dataFormatter) {
        this.dataFormatter = dataFormatter;
        return this;
    }

    @Override
    public StandardSelectorBuilderImpl selectingStandard() {
        defaultSelector = true;
        return new StandardSelectorBuilderImpl();
    }

    class StandardSelectorBuilderImpl implements StandardSelectorBuilder {
        @Override
        public StandardSelectorBuilderImpl fromExportPeriod(RelativePeriod relativePeriod) {
            exportPeriod = relativePeriod;
            return this;
        }

        @Override
        public StandardSelectorBuilderImpl fromUpdatePeriod(RelativePeriod relativePeriod) {
            updatePeriod = relativePeriod;
            return this;
        }

        @Override
        public StandardSelectorBuilderImpl fromReadingType(ReadingType readingType) {
            readingTypes.add(new ReadingTypeHolder(readingType));
            return this;
        }

        @Override
        public StandardSelectorBuilderImpl fromReadingType(String readingType) {
            readingTypes.add(new ReadingTypeByMrid(readingType));
            return this;
        }

        @Override
        public StandardSelectorBuilderImpl withValidatedDataOption(ValidatedDataOption option) {
            validatedDataOption = option;
            return this;
        }

        @Override
        public StandardSelectorBuilderImpl fromEndDeviceGroup(EndDeviceGroup group) {
            endDeviceGroup = group;
            return this;
        }

        @Override
        public StandardSelectorBuilderImpl exportUpdate(boolean update) {
            exportUpdate = update;
            return this;
        }

        @Override
        public StandardSelectorBuilderImpl continuousData(boolean continuous) {
            exportContinuousData = continuous;
            return this;
        }

        @Override
        public StandardSelectorBuilderImpl exportComplete(boolean complete) {
            exportComplete = complete;
            return this;
        }

        @Override
        public StandardSelectorBuilderImpl withUpdateWindow(RelativePeriod window) {
            updateWindow = window;
            return this;
        }

        @Override
        public DataExportTaskBuilderImpl endSelection() {
            return DataExportTaskBuilderImpl.this;
        }
    }

    @Override
    public CustomSelectorBuilder selectingCustom(String dataSelector) {
        this.dataSelector = dataSelector;
        defaultSelector = false;
        return new CustomSelectorBuilderImpl();
    }

    private class CustomSelectorBuilderImpl implements CustomSelectorBuilder {
        @Override
        public PropertyBuilder<CustomSelectorBuilder> addProperty(String name) {
            return new PropertyBuilderImpl<>(this, name);
        }

        @Override
        public DataExportTaskBuilder endSelection() {
            return DataExportTaskBuilderImpl.this;
        }
    }

    @Override
    public PropertyBuilder<DataExportTaskBuilder> addProperty(String name) {
        return new PropertyBuilderImpl<>(this, name);
    }

    private class PropertyBuilderImpl<T> implements PropertyBuilder<T> {
        private final String name;
        private final T source;
        private Object value;

        private PropertyBuilderImpl(T source, String name) {
            this.name = name;
            this.source = source;
        }

        @Override
        public T withValue(Object value) {
            this.value = value;
            properties.add(this);
            return source;
        }
    }


}
