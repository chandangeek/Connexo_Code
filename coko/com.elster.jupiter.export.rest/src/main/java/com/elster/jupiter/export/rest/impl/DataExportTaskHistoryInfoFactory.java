/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportDestination;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportRunParameters;
import com.elster.jupiter.export.DataExportStatus;
import com.elster.jupiter.export.DataSelectorConfig;
import com.elster.jupiter.export.DefaultSelectorOccurrence;
import com.elster.jupiter.export.EventSelectorConfig;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.export.MeterReadingSelectorConfig;
import com.elster.jupiter.export.ReadingDataSelectorConfig;
import com.elster.jupiter.export.UsagePointReadingSelectorConfig;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.rest.PeriodicalExpressionInfo;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.util.time.ScheduleExpression;

import com.google.common.collect.Range;
import org.glassfish.hk2.api.ServiceLocator;

import javax.inject.Inject;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

import static com.elster.jupiter.export.rest.impl.TranslationKeys.NONRECURRING;
import static com.elster.jupiter.export.rest.impl.TranslationKeys.ON_REQUEST;
import static com.elster.jupiter.export.rest.impl.TranslationKeys.SCHEDULED;

public class DataExportTaskHistoryInfoFactory {
    private final Thesaurus thesaurus;
    private final TimeService timeService;
    private final PropertyValueInfoService propertyValueInfoService;
    private final DataExportTaskInfoFactory dataExportTaskInfoFactory;
    private final ReadingTypeInfoFactory readingTypeInfoFactory;
    private final StandardDataSelectorInfoFactory standardDataSelectorInfoFactory;
    private final ServiceLocator serviceLocator;

    @Inject
    public DataExportTaskHistoryInfoFactory(Thesaurus thesaurus, TimeService timeService, PropertyValueInfoService propertyValueInfoService,
                                            DataExportTaskInfoFactory dataExportTaskInfoFactory, ReadingTypeInfoFactory readingTypeInfoFactory,
                                            StandardDataSelectorInfoFactory standardDataSelectorInfoFactory, ServiceLocator serviceLocator) {
        this.thesaurus = thesaurus;
        this.timeService = timeService;
        this.propertyValueInfoService = propertyValueInfoService;
        this.dataExportTaskInfoFactory = dataExportTaskInfoFactory;
        this.readingTypeInfoFactory = readingTypeInfoFactory;
        this.standardDataSelectorInfoFactory = standardDataSelectorInfoFactory;
        this.serviceLocator = serviceLocator;
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
        Instant versionAt = dataExportOccurrence.getRetryTime().orElse(dataExportOccurrence.getStartDate().get());
        populateMinimalInfo(info, dataExportOccurrence);
        ExportTask version = history.getVersionAt(versionAt)
                .orElseGet(() -> history.getVersionAt(dataExportOccurrence.getTask().getCreateTime())
                        .orElseGet(dataExportOccurrence::getTask));

        // set task from history
        info.task = dataExportTaskInfoFactory.asInfoWithHistory(version, dataExportOccurrence);
        // set standard data selector configuration from history
        version.getStandardDataSelectorConfig(versionAt)
                .ifPresent(selectorConfig -> populateStandardDataSelectorHistoricalData(info, selectorConfig, dataExportOccurrence));
        // set custom data selector properties from history
        info.task.dataSelector.properties = propertyValueInfoService.getPropertyInfos(
                version.getDataSelectorPropertySpecs(), version.getProperties(versionAt)
        );
        // set data formatter properties from history
        info.task.dataProcessor.properties = propertyValueInfoService.getPropertyInfos(
                version.getDataFormatterPropertySpecs(), version.getProperties(versionAt)
        );
        // set destination from history
        version.getDestinations(versionAt).stream()
                .sorted(Comparator.comparing(DataExportDestination::getCreateTime))
                .forEach(destination -> info.task.destinations.add(typeOf(destination).toInfo(serviceLocator, destination)));
        Optional<ScheduleExpression> foundSchedule = Optional.of(version.getScheduleExpression());
        if (!foundSchedule.isPresent() || Never.NEVER.equals(foundSchedule.get())) {
            info.task.schedule = null;
        } else {
            ScheduleExpression scheduleExpression = foundSchedule.get();
            if (scheduleExpression instanceof TemporalExpression) {
                info.task.schedule = new PeriodicalExpressionInfo((TemporalExpression) scheduleExpression);
            } else {
                info.task.schedule = PeriodicalExpressionInfo.from((PeriodicalScheduleExpression) scheduleExpression);
            }
        }
        if (dataExportOccurrence.wasScheduled() && info.task.schedule == null) {
            info.trigger = NONRECURRING.translate(thesaurus);
        }
        info.summary = dataExportOccurrence.getSummary();
        return info;
    }

    private void populateStandardDataSelectorHistoricalData(DataExportTaskHistoryInfo info, DataSelectorConfig selectorConfig, DataExportOccurrence dataExportOccurrence) {
        selectorConfig.apply(
                new DataSelectorConfig.DataSelectorConfigVisitor() {
                    @Override
                    public void visit(MeterReadingSelectorConfig config) {
                        info.task.standardDataSelector = standardDataSelectorInfoFactory.asInfo(config);
                        setReadingTypes(config);
                        addStrategy(config);
                    }

                    @Override
                    public void visit(UsagePointReadingSelectorConfig config) {
                        info.task.standardDataSelector = standardDataSelectorInfoFactory.asInfo(config);
                        setReadingTypes(config);
                    }

                    @Override
                    public void visit(EventSelectorConfig config) {
                        info.task.standardDataSelector = standardDataSelectorInfoFactory.asInfo(config);
                    }

                    private void setReadingTypes(ReadingDataSelectorConfig config) {
                        info.task.standardDataSelector.readingTypes = new ArrayList<>();
                        Instant at = dataExportOccurrence.getRetryTime().orElse(dataExportOccurrence.getTriggerTime());
                        for (ReadingType readingType : config.getReadingTypes(at)) {
                            info.task.standardDataSelector.readingTypes.add(readingTypeInfoFactory.from(readingType));
                        }
                    }

                    private void addStrategy(ReadingDataSelectorConfig config) {
                        if ((dataExportOccurrence.getAdhocTime().isPresent())
                                && (dataExportOccurrence.getTask().getRunParameters(dataExportOccurrence.getAdhocTime().get()).isPresent())) {
                            DataExportRunParameters runParameters = dataExportOccurrence.getTask().getRunParameters(dataExportOccurrence.getAdhocTime().get()).get();
                            info.updatePeriodFrom = runParameters.getUpdatePeriodStart();
                            info.updatePeriodTo = runParameters.getUpdatePeriodEnd();
                        } else {
                            Optional<RelativePeriod> updatePeriod = config.getStrategy().getUpdatePeriod();
                            if (updatePeriod.isPresent()) {
                                Range<Instant> interval = updatePeriod.get()
                                        .getOpenClosedInterval(ZonedDateTime.ofInstant(dataExportOccurrence.getTriggerTime(), ZoneId.systemDefault()));
                                info.updatePeriodFrom = interval.lowerEndpoint();
                                info.updatePeriodTo = interval.upperEndpoint();
                            }
                        }
                    }
                }
        );
    }

    private DestinationType typeOf(DataExportDestination destination) {
        return Arrays.stream(DestinationType.values())
                .filter(type -> type.getDestinationClass(serviceLocator).isInstance(destination))
                .findAny()
                .orElseThrow(IllegalArgumentException::new);
    }

    private void setStatusOnDate(DataExportTaskHistoryMinimalInfo info, DataExportOccurrence dataExportOccurrence) {
        DataExportStatus dataExportStatus = dataExportOccurrence.getStatus();
        String statusTranslation = dataExportOccurrence.getStatusName();
        if (DataExportStatus.BUSY.equals(dataExportStatus)) {
            info.statusPrefix = thesaurus.getFormat(TranslationKeys.SINCE).format(statusTranslation);
            info.statusDate = info.startedOn;
            info.statusId = 0;
        } else if ((DataExportStatus.FAILED.equals(dataExportStatus)) || (DataExportStatus.SUCCESS.equals(dataExportStatus))) {
            info.statusPrefix = thesaurus.getFormat(TranslationKeys.ON).format(statusTranslation);
            info.statusDate = info.finishedOn;
            info.statusId = 1;
        } else {
            info.statusPrefix = statusTranslation;
            info.statusId = 2;
        }


    }

    private static Long calculateDuration(Instant startedOn, Instant finishedOn) {
        if (startedOn == null) {
            return null;
        } else if (finishedOn == null) {
            return Instant.now().minusMillis(startedOn.toEpochMilli()).toEpochMilli();
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
