package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportDestination;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportStatus;
import com.elster.jupiter.export.DataExportStrategy;
import com.elster.jupiter.export.DefaultSelectorOccurrence;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.export.StandardDataSelector;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.rest.PeriodicalExpressionInfo;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.util.time.ScheduleExpression;

import javax.inject.Inject;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Optional;

import static com.elster.jupiter.export.rest.impl.TranslationKeys.NONRECURRING;
import static com.elster.jupiter.export.rest.impl.TranslationKeys.ON_REQUEST;
import static com.elster.jupiter.export.rest.impl.TranslationKeys.SCHEDULED;

public class DataExportTaskHistoryInfoFactory {

    private final Thesaurus thesaurus;
    private final TimeService timeService;
    private final PropertyValueInfoService propertyValueInfoService;
    private final DataExportTaskInfoFactory dataExportTaskInfoFactory;

    @Inject
    public DataExportTaskHistoryInfoFactory(Thesaurus thesaurus, TimeService timeService, PropertyValueInfoService propertyValueInfoService, DataExportTaskInfoFactory dataExportTaskInfoFactory) {
        this.thesaurus = thesaurus;
        this.timeService = timeService;
        this.propertyValueInfoService = propertyValueInfoService;
        this.dataExportTaskInfoFactory = dataExportTaskInfoFactory;
    }

    public DataExportTaskHistoryInfo asInfo(DataExportOccurrence dataExportOccurrence) {
        return asInfo(dataExportOccurrence.getTask().getHistory(), dataExportOccurrence);
    }

    public DataExportTaskHistoryMinimalInfo asMinimalInfo(DataExportOccurrence dataExportOccurrence) {
        DataExportTaskHistoryMinimalInfo info = new DataExportTaskHistoryMinimalInfo();
        populateMinimalInfo(info, dataExportOccurrence);
        Optional<ScheduleExpression> foundSchedule = dataExportOccurrence.getTask().getScheduleExpression(dataExportOccurrence.getTriggerTime());
        if (dataExportOccurrence.wasScheduled() && (!foundSchedule.isPresent() || Never.NEVER.equals(foundSchedule.get()))) {
            info.trigger = NONRECURRING.translate(thesaurus);
        }
        return info;
    }

    private void populateMinimalInfo(DataExportTaskHistoryMinimalInfo info, DataExportOccurrence dataExportOccurrence) {
        info.id = dataExportOccurrence.getId();
        info.trigger = (dataExportOccurrence.wasScheduled() ? SCHEDULED : ON_REQUEST).translate(thesaurus);
        if (dataExportOccurrence.wasScheduled()) {
            String scheduledTriggerDescription = this.getScheduledTriggerDescription(dataExportOccurrence);
            if (scheduledTriggerDescription != null) {
                info.trigger = info.trigger + " (" + scheduledTriggerDescription + ")";
            }
        }
        info.startedOn = dataExportOccurrence.getStartDate().orElse(null);
        info.finishedOn = dataExportOccurrence.getEndDate().orElse(null);
        info.duration = calculateDuration(info.startedOn, info.finishedOn);
        info.status = dataExportOccurrence.getStatusName();
        info.reason = dataExportOccurrence.getFailureReason();
        info.lastRun = dataExportOccurrence.getTriggerTime();
        dataExportOccurrence.getDefaultSelectorOccurrence()
                .map(DefaultSelectorOccurrence::getExportedDataInterval)
                .ifPresent(interval -> {
                    info.exportPeriodFrom = interval.lowerEndpoint();
                    info.exportPeriodTo = interval.upperEndpoint();
                });
        setStatusOnDate(info, dataExportOccurrence);
    }

    public DataExportTaskHistoryInfo asInfo(History<ExportTask> history, DataExportOccurrence dataExportOccurrence) {
        DataExportTaskHistoryInfo info = new DataExportTaskHistoryInfo();
        populateMinimalInfo(info, dataExportOccurrence);
        ExportTask version = history.getVersionAt(dataExportOccurrence.getStartDate().get())
                .orElseGet(() -> history.getVersionAt(dataExportOccurrence.getTask().getCreateTime())
                        .orElseGet(dataExportOccurrence::getTask));

        version.getReadingTypeDataSelector(dataExportOccurrence.getStartDate().get())
                .map(StandardDataSelector::getStrategy)
                .flatMap(DataExportStrategy::getUpdatePeriod)
                .map(relativePeriod -> relativePeriod.getOpenClosedInterval(ZonedDateTime.ofInstant(dataExportOccurrence.getTriggerTime(), ZoneId.systemDefault())))
                .ifPresent(interval -> {
                    info.updatePeriodFrom = interval.lowerEndpoint();
                    info.updatePeriodTo = interval.upperEndpoint();
                });

        info.task = dataExportTaskInfoFactory.asInfoWithoutHistory(version);
        populateForReadingTypeDataExportTask(info, version, dataExportOccurrence);
        version.getDestinations(dataExportOccurrence.getStartDate().get()).stream()
                .sorted((d1, d2) -> d1.getCreateTime().compareTo(d2.getCreateTime()))
                .forEach(destination -> info.task.destinations.add(typeOf(destination).toInfo(destination)));
        info.task.dataProcessor.properties = propertyValueInfoService.getPropertyInfos(version.getDataFormatterPropertySpecs(), version.getProperties());
        Optional<ScheduleExpression> foundSchedule = version.getScheduleExpression(dataExportOccurrence.getStartDate().get());
        if (!foundSchedule.isPresent() || Never.NEVER.equals(foundSchedule.get())) {
            info.task.schedule = null;
        } else if (foundSchedule.isPresent()) {
            ScheduleExpression scheduleExpression = foundSchedule.get();
            if (scheduleExpression instanceof TemporalExpression) {
                info.task.schedule = new PeriodicalExpressionInfo((TemporalExpression) scheduleExpression);
            } else {
                info.task.schedule = PeriodicalExpressionInfo.from((PeriodicalScheduleExpression) scheduleExpression);
            }
            info.task.dataSelector.properties = propertyValueInfoService.getPropertyInfos(version.getDataSelectorPropertySpecs(), version.getProperties(dataExportOccurrence.getTriggerTime()));
        }
        if (dataExportOccurrence.wasScheduled() && info.task.schedule == null) {
            info.trigger = NONRECURRING.translate(thesaurus);
        }
        info.summary = dataExportOccurrence.getSummary();
        return info;
    }

    private void populateForReadingTypeDataExportTask(DataExportTaskHistoryInfo info, ExportTask version, DataExportOccurrence dataExportOccurrence) {
        ReadingTypeInfoFactory readingTypeInfoFactory = new ReadingTypeInfoFactory(thesaurus);
        version.getReadingTypeDataSelector(dataExportOccurrence.getStartDate().get()).ifPresent(readingTypeDataSelector -> {
            info.task.standardDataSelector = new StandardDataSelectorInfo();
            info.task.standardDataSelector.populateFrom(readingTypeDataSelector, thesaurus);
            for (ReadingType readingType : readingTypeDataSelector.getReadingTypes(dataExportOccurrence.getStartDate().get())) {
                info.task.standardDataSelector.readingTypes.add(readingTypeInfoFactory.from(readingType));
            }
        });
    }

    private DestinationType typeOf(DataExportDestination destination) {
        return Arrays.stream(DestinationType.values())
                .filter(type -> type.getDestinationClass().isInstance(destination))
                .findAny()
                .orElseThrow(IllegalArgumentException::new);
    }

    private void setStatusOnDate(DataExportTaskHistoryMinimalInfo info, DataExportOccurrence dataExportOccurrence) {
        DataExportStatus dataExportStatus = dataExportOccurrence.getStatus();
        String statusTranslation = dataExportOccurrence.getStatusName();
        if (DataExportStatus.BUSY.equals(dataExportStatus)) {
            info.statusPrefix = thesaurus.getFormat(TranslationKeys.SINCE).format(statusTranslation);
            info.statusDate = info.startedOn;
        } else if ((DataExportStatus.FAILED.equals(dataExportStatus)) || (DataExportStatus.SUCCESS.equals(dataExportStatus))) {
            info.statusPrefix = thesaurus.getFormat(TranslationKeys.ON).format(statusTranslation);
            info.statusDate = info.finishedOn;
        } else {
            info.statusPrefix = statusTranslation;
        }
    }

    private static Long calculateDuration(Instant startedOn, Instant finishedOn) {
        if (startedOn == null || finishedOn == null) {
            return null;
        }
        return finishedOn.toEpochMilli() - startedOn.toEpochMilli();
    }

    private String getScheduledTriggerDescription(DataExportOccurrence dataExportOccurrence) {
        ScheduleExpression scheduleExpression = dataExportOccurrence.getTask().getScheduleExpression();
        if (Never.NEVER.equals(scheduleExpression)) {
            return null;
        }
        if (scheduleExpression instanceof PeriodicalScheduleExpression) {
            return fromPeriodicalScheduleExpression((PeriodicalScheduleExpression) scheduleExpression);
        }
        if (scheduleExpression instanceof TemporalExpression) {
            return fromTemporalExpression((TemporalExpression) scheduleExpression);
        }
        return scheduleExpression.toString();
    }

    private String fromPeriodicalScheduleExpression(PeriodicalScheduleExpression scheduleExpression) {
        return timeService.toLocalizedString(scheduleExpression);
    }

    private String fromTemporalExpression(TemporalExpression scheduleExpression) {
        return timeService.toLocalizedString(scheduleExpression);
    }
}
