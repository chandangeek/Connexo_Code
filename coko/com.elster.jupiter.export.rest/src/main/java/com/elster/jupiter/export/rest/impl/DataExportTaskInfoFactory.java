/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportDestination;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataSelectorConfig;
import com.elster.jupiter.export.EventSelectorConfig;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.export.MeterReadingSelectorConfig;
import com.elster.jupiter.export.UsagePointReadingSelectorConfig;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.rest.PeriodicalExpressionInfo;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.util.time.ScheduleExpression;

import org.glassfish.hk2.api.ServiceLocator;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Instant;
import java.util.Arrays;

public class DataExportTaskInfoFactory {

    private final Thesaurus thesaurus;
    private final TimeService timeService;
    private final PropertyValueInfoService propertyValueInfoService;
    private final Provider<DataExportTaskHistoryInfoFactory> dataExportTaskHistoryInfoFactoryProvider;
    private final StandardDataSelectorInfoFactory standardDataSelectorInfoFactory;
    private final ServiceLocator serviceLocator;

    @Inject
    public DataExportTaskInfoFactory(Thesaurus thesaurus, TimeService timeService, PropertyValueInfoService propertyValueInfoService,
                                     Provider<DataExportTaskHistoryInfoFactory> dataExportTaskHistoryInfoFactoryProvider,
                                     StandardDataSelectorInfoFactory standardDataSelectorInfoFactory, ServiceLocator serviceLocator) {
        this.thesaurus = thesaurus;
        this.timeService = timeService;
        this.propertyValueInfoService = propertyValueInfoService;
        this.dataExportTaskHistoryInfoFactoryProvider = dataExportTaskHistoryInfoFactoryProvider;
        this.standardDataSelectorInfoFactory = standardDataSelectorInfoFactory;
        this.serviceLocator = serviceLocator;
    }

    public DataExportTaskInfo asInfo(ExportTask exportTask) {
        DataExportTaskInfo info = asMinimalInfo(exportTask);
        info.lastExportOccurrence = exportTask.getLastOccurrence().map(oc -> dataExportTaskHistoryInfoFactoryProvider.get().asInfo(oc)).orElse(null);
        return info;
    }

    public DataExportTaskInfo asInfoWithMinimalHistory(ExportTask exportTask) {
        DataExportTaskInfo info = asMinimalInfo(exportTask);
        info.lastExportOccurrence = exportTask.getLastOccurrence().map(oc -> dataExportTaskHistoryInfoFactoryProvider.get().asMinimalInfo(oc)).orElse(null);
        return info;
    }

    private DataExportTaskInfo asMinimalInfo(ExportTask exportTask) {
        DataExportTaskInfo info = asInfoWithoutHistory(exportTask);
        if (Never.NEVER.equals(exportTask.getScheduleExpression())) {
            info.schedule = null;
            info.recurrence = thesaurus.getFormat(TranslationKeys.NONE).format();
        } else {
            ScheduleExpression scheduleExpression = exportTask.getScheduleExpression();
            if (scheduleExpression instanceof TemporalExpression) {
                info.schedule = new PeriodicalExpressionInfo((TemporalExpression) scheduleExpression);
                info.recurrence = fromTemporalExpression((TemporalExpression) scheduleExpression);
            } else {
                info.schedule = PeriodicalExpressionInfo.from((PeriodicalScheduleExpression) scheduleExpression);
                info.recurrence = fromPeriodicalScheduleExpression((PeriodicalScheduleExpression) scheduleExpression);
            }
        }
        exportTask.getDestinations().forEach(destination -> info.destinations.add(typeOf(destination).toInfo(serviceLocator, destination)));
        return info;
    }

    public DataExportTaskInfo asInfoWithHistory(ExportTask dataExportTask, DataExportOccurrence dataExportOccurrence) {
        DataExportTaskInfo info = new DataExportTaskInfo();
        Instant versionAt = dataExportOccurrence.getRetryTime().orElse(dataExportOccurrence.getTriggerTime());

        info.id = dataExportTask.getId();
        info.name = dataExportTask.getName();
        info.active = dataExportTask.isActive();
        info.logLevel = dataExportOccurrence.getRetryTime().isPresent() ? dataExportOccurrence.getRecurrentTask().getLogLevel(versionAt) :
                dataExportOccurrence.getRecurrentTask().getLogLevel();
        info.dataProcessor =
                new ProcessorInfo(
                        dataExportTask.getDataFormatterFactory().getName(),
                        dataExportTask.getDataFormatterFactory().getDisplayName(),
                        propertyValueInfoService.getPropertyInfos(
                                dataExportTask.getDataFormatterPropertySpecs(),
                                dataExportTask.getProperties()));
        String selector = dataExportTask.getDataSelectorFactory().getName();
        SelectorType selectorType = SelectorType.forSelector(selector);
        info.dataSelector =
                new SelectorInfo(
                        dataExportTask.getDataSelectorFactory().getName(),
                        dataExportTask.getDataSelectorFactory().getDisplayName(),
                        propertyValueInfoService.getPropertyInfos(
                                dataExportTask.getDataSelectorPropertySpecs(),
                                dataExportTask.getProperties()),
                        selectorType);
        dataExportTask.getStandardDataSelectorConfig().ifPresent(selectorConfig -> selectorConfig.apply(
                new DataSelectorConfig.DataSelectorConfigVisitor() {
                    @Override
                    public void visit(MeterReadingSelectorConfig config) {
                        info.standardDataSelector = standardDataSelectorInfoFactory.asInfo(config);
                    }

                    @Override
                    public void visit(UsagePointReadingSelectorConfig config) {
                        info.standardDataSelector = standardDataSelectorInfoFactory.asInfo(config);
                    }

                    @Override
                    public void visit(EventSelectorConfig config) {
                        info.standardDataSelector = standardDataSelectorInfoFactory.asInfo(config);
                    }
                }
        ));

        Instant nextExecution = dataExportTask.getNextExecution();
        if (nextExecution != null) {
            info.nextRun = nextExecution;
        }
        dataExportTask.getLastRun().ifPresent(lastRun -> info.lastRun = lastRun);
        info.version = dataExportTask.getVersion();
        return info;
    }

    public DataExportTaskInfo asInfoWithoutHistory(ExportTask dataExportTask) {
        DataExportTaskInfo info = new DataExportTaskInfo();
        info.id = dataExportTask.getId();
        info.name = dataExportTask.getName();
        info.logLevel = dataExportTask.getLogLevel();
        info.active = dataExportTask.isActive();
        info.dataProcessor =
                new ProcessorInfo(
                        dataExportTask.getDataFormatterFactory().getName(),
                        dataExportTask.getDataFormatterFactory().getDisplayName(),
                        propertyValueInfoService.getPropertyInfos(
                                dataExportTask.getDataFormatterPropertySpecs(),
                                dataExportTask.getProperties()));
        String selector = dataExportTask.getDataSelectorFactory().getName();
        SelectorType selectorType = SelectorType.forSelector(selector);
        info.dataSelector =
                new SelectorInfo(
                        dataExportTask.getDataSelectorFactory().getName(),
                        dataExportTask.getDataSelectorFactory().getDisplayName(),
                        propertyValueInfoService.getPropertyInfos(
                                dataExportTask.getDataSelectorPropertySpecs(),
                                dataExportTask.getProperties()),
                        selectorType);
        dataExportTask.getStandardDataSelectorConfig().ifPresent(selectorConfig -> selectorConfig.apply(
                new DataSelectorConfig.DataSelectorConfigVisitor() {
                    @Override
                    public void visit(MeterReadingSelectorConfig config) {
                        info.standardDataSelector = standardDataSelectorInfoFactory.asInfo(config);
                    }

                    @Override
                    public void visit(UsagePointReadingSelectorConfig config) {
                        info.standardDataSelector = standardDataSelectorInfoFactory.asInfo(config);
                    }

                    @Override
                    public void visit(EventSelectorConfig config) {
                        info.standardDataSelector = standardDataSelectorInfoFactory.asInfo(config);
                    }
                }
        ));

        Instant nextExecution = dataExportTask.getNextExecution();
        if (nextExecution != null) {
            info.nextRun = nextExecution;
        }

        info.suspendUntilExport = dataExportTask.getSuspendUntil();

        dataExportTask.getLastRun().ifPresent(lastRun -> info.lastRun = lastRun);
        info.version = dataExportTask.getVersion();
        return info;
    }

    private String fromTemporalExpression(TemporalExpression scheduleExpression) {
        return timeService.toLocalizedString(scheduleExpression);
    }

    private String fromPeriodicalScheduleExpression(PeriodicalScheduleExpression scheduleExpression) {
        return timeService.toLocalizedString(scheduleExpression);
    }

    private DestinationType typeOf(DataExportDestination destination) {
        return Arrays.stream(DestinationType.values())
                .filter(type -> type.getDestinationClass(serviceLocator).isInstance(destination))
                .findAny()
                .orElseThrow(IllegalArgumentException::new);
    }
}
