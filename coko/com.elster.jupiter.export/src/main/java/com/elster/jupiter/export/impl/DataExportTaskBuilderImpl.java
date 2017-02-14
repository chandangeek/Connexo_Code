/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataExportTaskBuilder;
import com.elster.jupiter.export.EventSelectorConfig;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.export.MeterReadingSelectorConfig;
import com.elster.jupiter.export.UsagePointReadingSelectorConfig;
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
    private List<ReadingType> readingTypes = new ArrayList<>();
    private Set<String> eventTypeFilters = new LinkedHashSet<>();
    private EndDeviceGroup endDeviceGroup;
    private UsagePointGroup usagePointGroup;
    private ValidatedDataOption validatedDataOption;
    private boolean exportUpdate;
    private boolean exportContinuousData;
    private boolean exportComplete;
    private String application;
    private int logLevel;

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
    public DataExportTaskBuilderImpl setLogLevel(int logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    @Override
    public ExportTask create() {
        ExportTaskImpl exportTask = ExportTaskImpl.from(dataModel, name, dataFormatter, dataSelector, scheduleExpression, nextExecution, application, logLevel);
        exportTask.setScheduleImmediately(scheduleImmediately);
        switch (defaultSelector) {
            case READINGTYPES: {
                MeterReadingSelectorConfig.Updater updater = MeterReadingSelectorConfigImpl.from(dataModel, exportTask, exportPeriod)
                        .startUpdate()
                        .setEndDeviceGroup(endDeviceGroup)
                        .setUpdatePeriod(updatePeriod)
                        .setValidatedDataOption(validatedDataOption)
                        .setExportUpdate(exportUpdate)
                        .setExportContinuousData(exportContinuousData)
                        .setUpdateWindow(updateWindow)
                        .setExportOnlyIfComplete(exportComplete);
                readingTypes.forEach(updater::addReadingType);
                exportTask.setStandardDataSelectorConfig(updater.complete());
                break;
            }
            case EVENTTYPES: {
                EventSelectorConfig.Updater updater = EventSelectorConfigImpl.from(dataModel, exportTask, exportPeriod)
                        .startUpdate()
                        .setEndDeviceGroup(endDeviceGroup)
                        .setExportContinuousData(exportContinuousData);
                eventTypeFilters.forEach(updater::addEventTypeFilter);
                exportTask.setStandardDataSelectorConfig(updater.complete());
                break;
            }
            case AGGREGATEDDATA: {
                UsagePointReadingSelectorConfig.Updater updater = UsagePointReadingSelectorConfigImpl.from(dataModel, exportTask, exportPeriod).startUpdate()
                        .setUsagePointGroup(usagePointGroup)
                        .setValidatedDataOption(validatedDataOption)
                        .setExportContinuousData(exportContinuousData)
                        .setExportOnlyIfComplete(exportComplete);
                readingTypes.forEach(updater::addReadingType);
                exportTask.setStandardDataSelectorConfig(updater.complete());
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
    public MeterReadingSelectorBuilder selectingMeterReadings() {
        defaultSelector = SelectorType.READINGTYPES;
        dataSelector = DataExportService.STANDARD_READINGTYPE_DATA_SELECTOR;
        return new MeterReadingSelectorBuilderImpl();
    }

    @Override
    public EventSelectorBuilder selectingEventTypes() {
        defaultSelector = SelectorType.EVENTTYPES;
        dataSelector = DataExportService.STANDARD_EVENT_DATA_SELECTOR;
        return new EventSelectorBuilderImpl();
    }

    @Override
    public UsagePointReadingSelectorBuilder selectingUsagePointReadings() {
        defaultSelector = SelectorType.AGGREGATEDDATA;
        dataSelector = DataExportService.STANDARD_USAGE_POINT_DATA_SELECTOR;
        return new UsagePointReadingSelectorBuilderImpl();
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

    private class MeterReadingSelectorBuilderImpl implements MeterReadingSelectorBuilder {
        @Override
        public MeterReadingSelectorBuilderImpl fromExportPeriod(RelativePeriod relativePeriod) {
            exportPeriod = relativePeriod;
            return this;
        }

        @Override
        public MeterReadingSelectorBuilderImpl fromUpdatePeriod(RelativePeriod relativePeriod) {
            updatePeriod = relativePeriod;
            return this;
        }

        @Override
        public MeterReadingSelectorBuilderImpl fromReadingType(ReadingType readingType) {
            readingTypes.add(readingType);
            return this;
        }

        @Override
        public MeterReadingSelectorBuilderImpl withValidatedDataOption(ValidatedDataOption option) {
            validatedDataOption = option;
            return this;
        }

        @Override
        public MeterReadingSelectorBuilderImpl fromEndDeviceGroup(EndDeviceGroup group) {
            endDeviceGroup = group;
            return this;
        }

        @Override
        public MeterReadingSelectorBuilderImpl exportUpdate(boolean update) {
            exportUpdate = update;
            return this;
        }

        @Override
        public MeterReadingSelectorBuilderImpl continuousData(boolean continuous) {
            exportContinuousData = continuous;
            return this;
        }

        @Override
        public MeterReadingSelectorBuilderImpl exportComplete(boolean complete) {
            exportComplete = complete;
            return this;
        }

        @Override
        public MeterReadingSelectorBuilderImpl withUpdateWindow(RelativePeriod window) {
            updateWindow = window;
            return this;
        }

        @Override
        public DataExportTaskBuilderImpl endSelection() {
            return DataExportTaskBuilderImpl.this;
        }
    }

    private class UsagePointReadingSelectorBuilderImpl implements UsagePointReadingSelectorBuilder {
        @Override
        public UsagePointReadingSelectorBuilderImpl fromExportPeriod(RelativePeriod relativePeriod) {
            exportPeriod = relativePeriod;
            return this;
        }

        @Override
        public UsagePointReadingSelectorBuilderImpl fromReadingType(ReadingType readingType) {
            readingTypes.add(readingType);
            return this;
        }

        @Override
        public UsagePointReadingSelectorBuilderImpl withValidatedDataOption(ValidatedDataOption option) {
            validatedDataOption = option;
            return this;
        }

        @Override
        public UsagePointReadingSelectorBuilderImpl continuousData(boolean continuous) {
            exportContinuousData = continuous;
            return this;
        }

        @Override
        public UsagePointReadingSelectorBuilderImpl exportComplete(boolean complete) {
            exportComplete = complete;
            return this;
        }

        @Override
        public UsagePointReadingSelectorBuilder fromUsagePointGroup(UsagePointGroup group) {
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
