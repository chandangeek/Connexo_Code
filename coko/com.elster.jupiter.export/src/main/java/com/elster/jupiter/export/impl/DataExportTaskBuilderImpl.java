package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataExportTaskBuilder;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.export.StandardDataSelector;
import com.elster.jupiter.export.ValidatedDataOption;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.util.time.ScheduleExpression;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

class DataExportTaskBuilderImpl implements DataExportTaskBuilder {

    private final DataModel dataModel;

    private enum SelectorType {
        CUSTOM, READINGTYPES, EVENTTYPES, AGGREGATEDDATA
    }

    private SelectorType defaultSelector = SelectorType.CUSTOM;
    private List<PropertyBuilderImpl> properties = new ArrayList<>();
    private ScheduleExpression scheduleExpression;
    private Instant nextExecution;
    private boolean scheduleImmediately;
    private String name;
    private String dataSelector = null;
    private String dataFormatter;
    private RelativePeriod exportPeriod;
    private RelativePeriod updatePeriod;
    private RelativePeriod updateWindow;
    private List<ReadingTypeDefinition> readingTypes = new ArrayList<>();
    private Set<String> eventTypeFilters = new LinkedHashSet<>();
    private EndDeviceGroup endDeviceGroup;
    private UsagePointGroup usagePointGroup;
    private ValidatedDataOption validatedDataOption;
    private boolean exportUpdate;
    private boolean exportContinuousData;
    private boolean exportComplete;
    private String application;

    private interface ReadingTypeDefinition {
        void addTo(StandardDataSelector standardDataSelector);
    }

    private class ReadingTypeByMrid implements ReadingTypeDefinition {
        private final String mrid;

        ReadingTypeByMrid(String mrid) {
            this.mrid = mrid;
        }

        @Override
        public void addTo(StandardDataSelector dataSelector) {
            dataSelector.addReadingType(mrid);
        }
    }

    private class ReadingTypeHolder implements ReadingTypeDefinition {

        private final ReadingType readingType;

        ReadingTypeHolder(ReadingType readingType) {
            this.readingType = readingType;
        }

        @Override
        public void addTo(StandardDataSelector task) {
            task.addReadingType(readingType);
        }
    }

    DataExportTaskBuilderImpl(DataModel dataModel) {
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
    public DataExportTaskBuilderImpl setApplication(String application) {
        this.application = application;
        return this;
    }

    @Override
    public DataExportTaskBuilderImpl scheduleImmediately() {
        this.scheduleImmediately = true;
        return this;
    }

    @Override
    public ExportTask create() {
        ExportTaskImpl exportTask = ExportTaskImpl.from(dataModel, name, dataFormatter, dataSelector, scheduleExpression, nextExecution, application);
        exportTask.setScheduleImmediately(scheduleImmediately);
        switch (defaultSelector) {
            case READINGTYPES: {
                StandardDataSelectorImpl dataSelector = StandardDataSelectorImpl.from(dataModel, exportTask, exportPeriod);
                dataSelector.setEndDeviceGroup(endDeviceGroup);
                dataSelector.setUpdatePeriod(updatePeriod);
                dataSelector.setValidatedDataOption(validatedDataOption);
                dataSelector.setExportUpdate(exportUpdate);
                dataSelector.setExportContinuousData(exportContinuousData);
                dataSelector.setUpdateWindow(updateWindow);
                dataSelector.setExportOnlyIfComplete(exportComplete);
                readingTypes.forEach(readingTypeDefinition -> readingTypeDefinition.addTo(dataSelector));
                exportTask.setReadingTypeDataSelector(dataSelector);
                break;
            }
            case EVENTTYPES: {
                StandardDataSelectorImpl dataSelector = StandardDataSelectorImpl.from(dataModel, exportTask, exportPeriod);
                dataSelector.setEndDeviceGroup(endDeviceGroup);
                dataSelector.setExportContinuousData(exportContinuousData);
                eventTypeFilters.forEach(dataSelector::addEventTypeFilter);
                exportTask.setReadingTypeDataSelector(dataSelector);
                break;
            }
            case AGGREGATEDDATA: {
                StandardDataSelectorImpl dataSelector = StandardDataSelectorImpl.from(dataModel, exportTask, exportPeriod);
                dataSelector.setUsagePointGroup(usagePointGroup);
                dataSelector.setValidatedDataOption(validatedDataOption);
                dataSelector.setExportContinuousData(exportContinuousData);
                dataSelector.setExportOnlyIfComplete(exportComplete);
                readingTypes.forEach(readingTypeDefinition -> readingTypeDefinition.addTo(dataSelector));
                exportTask.setReadingTypeDataSelector(dataSelector);
                break;
            }
            case CUSTOM:
            default:
        }
        properties.forEach(p -> exportTask.setProperty(p.name, p.value));
        exportTask.doSave();
        return exportTask;
    }

    @Override
    public DataExportTaskBuilderImpl setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public DataExportTaskBuilderImpl setDataFormatterFactoryName(String dataFormatter) {
        this.dataFormatter = dataFormatter;
        return this;
    }

    @Override
    public StandardSelectorBuilderImpl selectingReadingTypes() {
        defaultSelector = SelectorType.READINGTYPES;
        dataSelector = DataExportService.STANDARD_READINGTYPE_DATA_SELECTOR;
        return new StandardSelectorBuilderImpl();
    }

    @Override
    public EventSelectorBuilder selectingEventTypes() {
        defaultSelector = SelectorType.EVENTTYPES;
        dataSelector = DataExportService.STANDARD_EVENT_DATA_SELECTOR;
        return new EventSelectorBuilderImpl();
    }

    @Override
    public AggregatedDataSelectorBuilder selectingAggregatedData() {
        defaultSelector = SelectorType.AGGREGATEDDATA;
        dataSelector = DataExportService.STANDARD_AGGREGATED_DATA_SELECTOR;
        return new AggregatedDataSelectorBuilderImpl();
    }

    private class EventSelectorBuilderImpl implements EventSelectorBuilder {
        @Override
        public EventSelectorBuilderImpl fromExportPeriod(RelativePeriod relativePeriod) {
            exportPeriod = relativePeriod;
            return this;
        }

        @Override
        public EventSelectorBuilderImpl fromEndDeviceGroup(EndDeviceGroup group) {
            endDeviceGroup = group;
            return this;
        }

        @Override
        public EventSelectorBuilderImpl fromEventType(String filterCode) {
            eventTypeFilters.add(filterCode);
            return this;
        }

        @Override
        public DataExportTaskBuilderImpl endSelection() {
            return DataExportTaskBuilderImpl.this;
        }
    }

    class StandardSelectorBuilderImpl implements ReadingTypeSelectorBuilder {
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

    class AggregatedDataSelectorBuilderImpl implements AggregatedDataSelectorBuilder {
        @Override
        public AggregatedDataSelectorBuilderImpl fromExportPeriod(RelativePeriod relativePeriod) {
            exportPeriod = relativePeriod;
            return this;
        }

        @Override
        public AggregatedDataSelectorBuilderImpl fromReadingType(ReadingType readingType) {
            readingTypes.add(new ReadingTypeHolder(readingType));
            return this;
        }

        @Override
        public AggregatedDataSelectorBuilderImpl withValidatedDataOption(ValidatedDataOption option) {
            validatedDataOption = option;
            return this;
        }

        @Override
        public AggregatedDataSelectorBuilderImpl continuousData(boolean continuous) {
            exportContinuousData = continuous;
            return this;
        }

        @Override
        public AggregatedDataSelectorBuilderImpl exportComplete(boolean complete) {
            exportComplete = complete;
            return this;
        }

        @Override
        public AggregatedDataSelectorBuilder fromUsagePointGroup(UsagePointGroup group) {
            usagePointGroup = group;
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
        defaultSelector = SelectorType.CUSTOM;
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
