/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.rest;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.rest.util.IdWithDisplayValueInfo;
import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.rest.PeriodicalExpressionInfo;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.elster.jupiter.validation.DataValidationOccurrence;
import com.elster.jupiter.validation.DataValidationTask;
import com.elster.jupiter.validation.DataValidationTaskStatus;
import com.elster.jupiter.validation.rest.impl.TranslationKeys;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Optional;

public class DataValidationTaskInfoFactory {

    private final Thesaurus thesaurus;
    private final TimeService timeService;

    @Inject
    public DataValidationTaskInfoFactory(Thesaurus thesaurus, TimeService timeService) {
        this.thesaurus = thesaurus;
        this.timeService = timeService;
    }

    public DataValidationTaskMinimalInfo asMinimalInfo(DataValidationTask dataValidationTask) {
        DataValidationTaskMinimalInfo info = new DataValidationTaskMinimalInfo();
        populate(info, dataValidationTask);
        return info;
    }

    public DataValidationTaskInfo asInfo(DataValidationTask dataValidationTask) {
        DataValidationTaskInfo info = asInfoWithoutLastOccurrence(dataValidationTask);
        dataValidationTask.getLastOccurrence().ifPresent(dataValidationOccurrence -> {
            info.lastValidationOccurence = asInfo(dataValidationOccurrence);
            info.lastValidationOccurence.wasScheduled = dataValidationOccurrence.wasScheduled();
        });
        return info;
    }

    private void populate(DataValidationTaskMinimalInfo info, DataValidationTask dataValidationTask) {
        info.id = dataValidationTask.getId();
        info.name = dataValidationTask.getName();
        info.logLevel = dataValidationTask.getLogLevel();
        info.nextRun = dataValidationTask.getNextExecution();
        ScheduleExpression scheduleExpression = dataValidationTask.getScheduleExpression();
        if (Never.NEVER.equals(dataValidationTask.getScheduleExpression())) {
            info.schedule = null;
        } else {
            if (scheduleExpression instanceof TemporalExpression) {
                info.schedule = new PeriodicalExpressionInfo((TemporalExpression) scheduleExpression);
            } else {
                info.schedule = PeriodicalExpressionInfo.from((PeriodicalScheduleExpression) scheduleExpression);
            }
        }
    }

    private DataValidationTaskInfo asInfoWithoutLastOccurrence(DataValidationTask dataValidationTask) {
        DataValidationTaskInfo info = new DataValidationTaskInfo();
        populate(info, dataValidationTask);
        info.deviceGroup =
                dataValidationTask
                        .getEndDeviceGroup()
                        .map(endDeviceGroup -> new IdWithDisplayValueInfo<>(endDeviceGroup.getId(), endDeviceGroup.getName()))
                        .orElse(null);
        dataValidationTask.getUsagePointGroup().ifPresent(usagePointGroup -> {
            info.usagePointGroup = new IdWithDisplayValueInfo<>(usagePointGroup.getId(), usagePointGroup.getName());
            info.metrologyPurpose =
                    dataValidationTask
                            .getMetrologyPurpose()
                            .map(metrologyPurpose -> new IdWithDisplayValueInfo<>(metrologyPurpose.getId(), metrologyPurpose.getName()))
                            .orElse(null);
        });
        ScheduleExpression scheduleExpression = dataValidationTask.getScheduleExpression();
        if (Never.NEVER.equals(dataValidationTask.getScheduleExpression())) {
            info.recurrence = thesaurus.getFormat(TranslationKeys.NONE).format();
        } else {
            if (scheduleExpression instanceof TemporalExpression) {
                info.recurrence = fromTemporalExpression((TemporalExpression) scheduleExpression);
            } else {
                info.recurrence = fromPeriodicalScheduleExpression((PeriodicalScheduleExpression) scheduleExpression);
            }
        }
        info.lastRun = dataValidationTask.getLastRun().orElse(null);
        info.version = dataValidationTask.getVersion();
        return info;
    }

    private String fromPeriodicalScheduleExpression(PeriodicalScheduleExpression scheduleExpression) {
        return timeService.toLocalizedString(scheduleExpression);
    }

    private String fromTemporalExpression(TemporalExpression scheduleExpression) {
        return this.timeService.toLocalizedString(scheduleExpression);
    }

    public DataValidationTaskHistoryInfo asInfo(DataValidationOccurrence dataValidationOccurrence) {
        return asInfo((History<DataValidationTask>) dataValidationOccurrence.getTask().getHistory(), dataValidationOccurrence);
    }

    public DataValidationTaskHistoryInfo asInfo(History<DataValidationTask> history, DataValidationOccurrence dataValidationOccurrence) {
        DataValidationTaskHistoryInfo info = new DataValidationTaskHistoryInfo();
        info.id = dataValidationOccurrence.getId();
        info.startedOn = dataValidationOccurrence.getStartDate().orElse(null);
        info.finishedOn = dataValidationOccurrence.getEndDate().orElse(null);
        info.duration = calculateDuration(info.startedOn, info.finishedOn);
        info.status = dataValidationOccurrence.getStatusName();
        info.reason = dataValidationOccurrence.getFailureReason();
        info.lastRun = dataValidationOccurrence.getTriggerTime();
        setStatusOnDate(info, dataValidationOccurrence);
        DataValidationTask version = history.getVersionAt(dataValidationOccurrence.getTriggerTime())
                .orElseGet(() -> history.getVersionAt(dataValidationOccurrence.getTask().getCreateTime())
                        .orElseGet(dataValidationOccurrence::getTask));
        info.task = asInfoWithoutLastOccurrence(version);
        Optional<ScheduleExpression> foundSchedule = version.getScheduleExpression(dataValidationOccurrence.getTriggerTime());
        if (!foundSchedule.isPresent() || Never.NEVER.equals(foundSchedule.get())) {
            info.task.schedule = null;
        } else if (foundSchedule.isPresent()) {
            ScheduleExpression scheduleExpression = foundSchedule.get();
            if (scheduleExpression instanceof TemporalExpression) {
                info.task.schedule = new PeriodicalExpressionInfo((TemporalExpression) scheduleExpression);
            } else {
                info.task.schedule = PeriodicalExpressionInfo.from((PeriodicalScheduleExpression) scheduleExpression);
            }
        }
        return info;
    }

    private void setStatusOnDate(DataValidationTaskHistoryInfo info, DataValidationOccurrence dataValidationOccurrence) {
        DataValidationTaskStatus dataExportStatus = dataValidationOccurrence.getStatus();
        String statusTranslation = dataValidationOccurrence.getStatusName();
        if (DataValidationTaskStatus.BUSY.equals(dataExportStatus)) {
            info.statusPrefix = statusTranslation + " " + thesaurus.getString("since", "since");
            info.statusDate = info.startedOn;
        } else if ((DataValidationTaskStatus.FAILED.equals(dataExportStatus)) || (DataValidationTaskStatus.SUCCESS.equals(dataExportStatus))) {
            info.statusPrefix = statusTranslation + " " + thesaurus.getString("on", "on");
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

}
