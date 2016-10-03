package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportStatus;
import com.elster.jupiter.export.DefaultSelectorOccurrence;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.util.time.ScheduleExpression;

import java.time.Instant;
import java.util.Optional;

import static com.elster.jupiter.export.rest.impl.MessageSeeds.Labels.NONRECURRING;
import static com.elster.jupiter.export.rest.impl.MessageSeeds.Labels.ON_REQUEST;
import static com.elster.jupiter.export.rest.impl.MessageSeeds.Labels.SCHEDULED;

public class DataTaskHistoryWithoutEmbeddedTaskInfo {
    public Long id;
    public String trigger;
    public Long startedOn;
    public Long finishedOn;
    public Long duration;
    public String status;
    public String reason;
    public Long lastRun;
    public Long exportPeriodFrom;
    public Long exportPeriodTo;
    public Long statusDate;
    public String statusPrefix;

    public DataTaskHistoryWithoutEmbeddedTaskInfo() {
    }

    public DataTaskHistoryWithoutEmbeddedTaskInfo(DataExportOccurrence dataExportOccurrence, Thesaurus thesaurus, TimeService timeService, PropertyValueInfoService propertyValueInfoService) {
        this();
        populate(dataExportOccurrence, thesaurus, timeService, propertyValueInfoService);
    }

    private void populate(DataExportOccurrence dataExportOccurrence, Thesaurus thesaurus, TimeService timeService, PropertyValueInfoService propertyValueInfoService) {
        this.id = dataExportOccurrence.getId();

        this.trigger = (dataExportOccurrence.wasScheduled() ? SCHEDULED : ON_REQUEST).translate(thesaurus);
        if (dataExportOccurrence.wasScheduled()) {
            String scheduledTriggerDescription = this.getScheduledTriggerDescription(dataExportOccurrence, thesaurus, timeService);
            if (scheduledTriggerDescription != null) {
                this.trigger = this.trigger + " (" + scheduledTriggerDescription + ")";
            }
        }
        this.startedOn = dataExportOccurrence.getStartDate().map(this::toLong).orElse(null);
        this.finishedOn = dataExportOccurrence.getEndDate().map(this::toLong).orElse(null);
        this.duration = calculateDuration(startedOn, finishedOn);
        this.status = dataExportOccurrence.getStatusName();
        this.reason = dataExportOccurrence.getFailureReason();
        this.lastRun = dataExportOccurrence.getTriggerTime().toEpochMilli();
        dataExportOccurrence.getDefaultSelectorOccurrence()
                .map(DefaultSelectorOccurrence::getExportedDataInterval)
                .ifPresent(interval -> {
                    this.exportPeriodFrom = interval.lowerEndpoint().toEpochMilli();
                    this.exportPeriodTo = interval.upperEndpoint().toEpochMilli();
                });
        setStatusOnDate(dataExportOccurrence, thesaurus);
        Optional<ScheduleExpression> foundSchedule =  dataExportOccurrence.getTask().getScheduleExpression(dataExportOccurrence.getTriggerTime());
        if (dataExportOccurrence.wasScheduled() && (!foundSchedule.isPresent() || Never.NEVER.equals(foundSchedule.get()))) {
            this.trigger = NONRECURRING.translate(thesaurus);
        }
    }

    private void setStatusOnDate(DataExportOccurrence dataExportOccurrence, Thesaurus thesaurus) {
        DataExportStatus dataExportStatus = dataExportOccurrence.getStatus();
        String statusTranslation = dataExportOccurrence.getStatusName();
        if (DataExportStatus.BUSY.equals(dataExportStatus)) {
            this.statusPrefix = thesaurus.getFormat(TranslationKeys.SINCE).format(statusTranslation);
            this.statusDate = startedOn;
        } else if ((DataExportStatus.FAILED.equals(dataExportStatus)) || (DataExportStatus.SUCCESS.equals(dataExportStatus))) {
            this.statusPrefix = thesaurus.getFormat(TranslationKeys.ON).format(statusTranslation);
            this.statusDate = finishedOn;
        } else {
            this.statusPrefix = statusTranslation;
        }
    }

    private static Long calculateDuration(Long startedOn, Long finishedOn) {
        if (startedOn == null || finishedOn == null) {
            return null;
        }
        return finishedOn - startedOn;
    }

    private Long toLong(Instant instant) {
        return instant == null ? null : instant.toEpochMilli();
    }

    private String getScheduledTriggerDescription(DataExportOccurrence dataExportOccurrence, Thesaurus thesaurus, TimeService timeService) {
        ScheduleExpression scheduleExpression = dataExportOccurrence.getTask().getScheduleExpression();
        if (Never.NEVER.equals(scheduleExpression)) {
            return null;
        }
        if (scheduleExpression instanceof PeriodicalScheduleExpression) {
            return fromPeriodicalScheduleExpression((PeriodicalScheduleExpression) scheduleExpression, timeService);
        }
        if (scheduleExpression instanceof TemporalExpression) {
            return fromTemporalExpression((TemporalExpression) scheduleExpression, timeService);
        }
        return scheduleExpression.toString();
    }

    private String fromPeriodicalScheduleExpression(PeriodicalScheduleExpression scheduleExpression, TimeService timeService) {
        return timeService.toLocalizedString(scheduleExpression);
    }

    private String fromTemporalExpression(TemporalExpression scheduleExpression, TimeService timeService) {
        return timeService.toLocalizedString(scheduleExpression);
    }

}