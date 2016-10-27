package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportDestination;
import com.elster.jupiter.export.EventSelectorConfig;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.export.MeterReadingSelectorConfig;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.rest.PeriodicalExpressionInfo;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.util.time.ScheduleExpression;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Instant;
import java.util.Arrays;

public class DataExportTaskInfoFactory {

    private final Thesaurus thesaurus;
    private final TimeService timeService;
    private final PropertyValueInfoService propertyValueInfoService;
    private final Provider<DataExportTaskHistoryInfoFactory> dataExportTaskHistoryInfoFactoryProvider;

    @Inject
    public DataExportTaskInfoFactory(Thesaurus thesaurus, TimeService timeService, PropertyValueInfoService propertyValueInfoService, Provider<DataExportTaskHistoryInfoFactory> dataExportTaskHistoryInfoFactoryProvider) {
        this.thesaurus = thesaurus;
        this.timeService = timeService;
        this.propertyValueInfoService = propertyValueInfoService;
        this.dataExportTaskHistoryInfoFactoryProvider = dataExportTaskHistoryInfoFactoryProvider;
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
        exportTask.getDestinations().forEach(destination -> info.destinations.add(typeOf(destination).toInfo(destination)));
        return info;
    }

    public DataExportTaskInfo asInfoWithoutHistory(ExportTask dataExportTask) {
        DataExportTaskInfo info = new DataExportTaskInfo();
        info.id = dataExportTask.getId();
        info.name = dataExportTask.getName();
        info.active = dataExportTask.isActive();
        String selector = dataExportTask.getDataSelectorFactory().getName();
        SelectorType selectorType = SelectorType.forSelector(selector);
        if (selectorType.equals(SelectorType.DEFAULT_READINGS)) {
            populateReadingTypeDataExport(info, dataExportTask);
        } else if (selectorType.equals(SelectorType.DEFAULT_EVENTS)) {
            populateEventTypeDataExport(info, dataExportTask);
        }
        info.dataProcessor =
                new ProcessorInfo(
                        dataExportTask.getDataFormatterFactory().getName(),
                        dataExportTask.getDataFormatterFactory().getDisplayName(),
                        propertyValueInfoService.getPropertyInfos(
                                dataExportTask.getDataFormatterPropertySpecs(),
                                dataExportTask.getProperties()));
        info.dataSelector =
                new SelectorInfo(
                        dataExportTask.getDataSelectorFactory().getName(),
                        dataExportTask.getDataSelectorFactory().getDisplayName(),
                        propertyValueInfoService.getPropertyInfos(
                                dataExportTask.getDataSelectorPropertySpecs(),
                                dataExportTask.getProperties()),
                        selectorType);

        Instant nextExecution = dataExportTask.getNextExecution();
        if (nextExecution != null) {
            info.nextRun = nextExecution;
        }
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
                .filter(type -> type.getDestinationClass().isInstance(destination))
                .findAny()
                .orElseThrow(IllegalArgumentException::new);
    }

    private void populateReadingTypeDataExport(DataExportTaskInfo info, ExportTask dataExportTask) {
        dataExportTask.getStandardDataSelectorConfig().map(MeterReadingSelectorConfig.class::cast)
                .ifPresent(meterReadingDataSelectorConfig -> info.standardDataSelector = new StandardDataSelectorInfo(meterReadingDataSelectorConfig, thesaurus));
    }

    private void populateEventTypeDataExport(DataExportTaskInfo info, ExportTask dataExportTask) {
        dataExportTask.getStandardDataSelectorConfig().map(EventSelectorConfig.class::cast)
                .ifPresent(eventDataSelector -> info.standardDataSelector = new StandardDataSelectorInfo(eventDataSelector));
    }
}
