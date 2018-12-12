/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.customtask.rest.impl;

import com.elster.jupiter.customtask.CustomTask;
import com.elster.jupiter.customtask.CustomTaskOccurrence;
import com.elster.jupiter.customtask.CustomTaskStatus;
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
import java.util.Optional;

import static com.elster.jupiter.customtask.rest.impl.TranslationKeys.NONRECURRING;
import static com.elster.jupiter.customtask.rest.impl.TranslationKeys.ON_REQUEST;
import static com.elster.jupiter.customtask.rest.impl.TranslationKeys.SCHEDULED;


public class CustomTaskHistoryInfoFactory {

    private final Thesaurus thesaurus;
    private final TimeService timeService;
    private final PropertyValueInfoService propertyValueInfoService;
    private final CustomTaskInfoFactory customTaskInfoFactory;

    @Inject
    public CustomTaskHistoryInfoFactory(Thesaurus thesaurus, TimeService timeService, PropertyValueInfoService propertyValueInfoService,
                                        CustomTaskInfoFactory customTaskInfoFactory) {
        this.thesaurus = thesaurus;
        this.timeService = timeService;
        this.propertyValueInfoService = propertyValueInfoService;
        this.customTaskInfoFactory = customTaskInfoFactory;
    }

    public CustomTaskHistoryInfo asInfo(CustomTaskOccurrence customTaskOccurrence) {
        return asInfo(customTaskOccurrence.getTask().getHistory(), customTaskOccurrence);
    }

    public CustomTaskHistoryMinimalInfo asMinimalInfo(CustomTaskOccurrence customTaskOccurrence) {
        CustomTaskHistoryMinimalInfo info = new CustomTaskHistoryMinimalInfo();
        populateMinimalInfo(info, customTaskOccurrence);
        Optional<ScheduleExpression> foundSchedule = customTaskOccurrence.getTask().getScheduleExpression(customTaskOccurrence.getTriggerTime());
        if (customTaskOccurrence.wasScheduled() && (!foundSchedule.isPresent() || Never.NEVER.equals(foundSchedule.get()))) {
            info.trigger = NONRECURRING.translate(thesaurus);
        }
        return info;
    }

    private void populateMinimalInfo(CustomTaskHistoryMinimalInfo info, CustomTaskOccurrence customTaskOccurrence) {
        info.id = customTaskOccurrence.getId();
        info.trigger = (customTaskOccurrence.wasScheduled() ? SCHEDULED : ON_REQUEST).translate(thesaurus);
        if (customTaskOccurrence.wasScheduled()) {
            String scheduledTriggerDescription = this.getScheduledTriggerDescription(customTaskOccurrence);
            if (scheduledTriggerDescription != null) {
                info.trigger = info.trigger + " (" + scheduledTriggerDescription + ")";
            }
        }
        info.startedOn = customTaskOccurrence.getStartDate().orElse(null);
        info.finishedOn = customTaskOccurrence.getEndDate().orElse(null);
        info.duration = calculateDuration(info.startedOn, info.finishedOn);
        info.statusType = customTaskOccurrence.getStatus().toString();
        info.status = customTaskOccurrence.getStatusName();
        info.reason = customTaskOccurrence.getFailureReason();
        info.lastRun = customTaskOccurrence.getTriggerTime();
        info.wasScheduled = customTaskOccurrence.wasScheduled();
        setStatusOnDate(info, customTaskOccurrence);
    }

    public CustomTaskHistoryInfo asInfo(History<CustomTask> history, CustomTaskOccurrence customTaskOccurrence) {
        CustomTaskHistoryInfo info = new CustomTaskHistoryInfo();
        Instant versionAt = customTaskOccurrence.getStartDate().get();
        populateMinimalInfo(info, customTaskOccurrence);
        CustomTask version = history.getVersionAt(versionAt)
                .orElseGet(() -> history.getVersionAt(customTaskOccurrence.getTask().getCreateTime())
                        .orElseGet(customTaskOccurrence::getTask));

        info.task = customTaskInfoFactory.asInfoWithHistory(version, customTaskOccurrence);
        Optional<ScheduleExpression> foundSchedule = version.getScheduleExpression(versionAt);
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
        if (customTaskOccurrence.wasScheduled() && info.task.schedule == null) {
            info.trigger = NONRECURRING.translate(thesaurus);
        }
        info.summary = customTaskOccurrence.getSummary();
        return info;
    }

    private void setStatusOnDate(CustomTaskHistoryMinimalInfo info, CustomTaskOccurrence customTaskOccurrence) {
        CustomTaskStatus customTaskStatus = customTaskOccurrence.getStatus();
        String statusTranslation = customTaskOccurrence.getStatusName();
        if (CustomTaskStatus.BUSY.equals(customTaskStatus)) {
            info.statusPrefix = thesaurus.getFormat(TranslationKeys.SINCE).format(statusTranslation);
            info.statusDate = info.startedOn;
        } else if ((CustomTaskStatus.FAILED.equals(customTaskStatus)) || (CustomTaskStatus.SUCCESS.equals(customTaskStatus))) {
            info.statusPrefix = thesaurus.getFormat(TranslationKeys.ON).format(statusTranslation);
            info.statusDate = info.finishedOn;
        } else {
            info.statusPrefix = statusTranslation;
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

    private String getScheduledTriggerDescription(CustomTaskOccurrence customTaskOccurrence) {
        ScheduleExpression scheduleExpression = customTaskOccurrence.getTask().getScheduleExpression();
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
