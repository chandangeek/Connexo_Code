package com.elster.jupiter.validation.rest;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.rest.PeriodicalExpressionInfo;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.elster.jupiter.validation.DataValidationOccurrence;
import com.elster.jupiter.validation.DataValidationTask;
import com.elster.jupiter.validation.DataValidationTaskStatus;
import com.elster.jupiter.validation.rest.impl.TranslationKeys;

import java.time.Instant;
import java.util.Optional;

public class DataValidationTaskHistoryInfo {

    public Long id;
    public String trigger;
    public Long startedOn;
    public Long finishedOn;
    public Long duration;
    public String status;
    public String reason;
    public Long lastRun;
    public Long statusDate;
    public String statusPrefix;
    public DataValidationTaskInfo task;

    public DataValidationTaskHistoryInfo() {
    }

    public DataValidationTaskHistoryInfo(DataValidationOccurrence dataValidationOccurrence, Thesaurus thesaurus, TimeService timeService) {
        populate((History<DataValidationTask>) dataValidationOccurrence.getTask().getHistory(), dataValidationOccurrence, thesaurus, timeService);
    }

    public DataValidationTaskHistoryInfo(History<? extends DataValidationTask> history, DataValidationOccurrence dataValidationOccurrence, Thesaurus thesaurus, TimeService timeService) {
        populate((History<DataValidationTask>) history, dataValidationOccurrence, thesaurus, timeService);
    }

    private void populate(History<DataValidationTask> history, DataValidationOccurrence dataValidationOccurrence, Thesaurus thesaurus, TimeService timeService) {
        this.id = dataValidationOccurrence.getId();

        this.trigger = thesaurus.getFormat(dataValidationOccurrence.wasScheduled() ? TranslationKeys.SCHEDULED : TranslationKeys.ON_REQUEST).format();
        if (dataValidationOccurrence.wasScheduled()) {
            String scheduledTriggerDescription = this.getScheduledTriggerDescription(dataValidationOccurrence, thesaurus, timeService);
            if (scheduledTriggerDescription != null) {
                this.trigger = this.trigger + " (" + scheduledTriggerDescription + ")";
            }
        }
        this.startedOn = dataValidationOccurrence.getStartDate().map(this::toLong).orElse(null);
        this.finishedOn = dataValidationOccurrence.getEndDate().map(this::toLong).orElse(null);
        this.duration = calculateDuration(startedOn, finishedOn);
        this.status = getName(dataValidationOccurrence.getStatus(), thesaurus);
        this.reason = dataValidationOccurrence.getFailureReason();
        this.lastRun = dataValidationOccurrence.getTriggerTime().toEpochMilli();
        setStatusOnDate(dataValidationOccurrence, thesaurus);
        DataValidationTask version = history.getVersionAt(dataValidationOccurrence.getTriggerTime())
                .orElseGet(() -> history.getVersionAt(dataValidationOccurrence.getTask().getCreateTime())
                        .orElseGet(dataValidationOccurrence::getTask));
        task = new DataValidationTaskInfo();
        task.populate(version);

        Optional<ScheduleExpression> foundSchedule = version.getScheduleExpression(dataValidationOccurrence.getTriggerTime());
        if (!foundSchedule.isPresent() || Never.NEVER.equals(foundSchedule.get())) {
            task.schedule = null;
        } else if (foundSchedule.isPresent()) {
            ScheduleExpression scheduleExpression = foundSchedule.get();
            if (scheduleExpression instanceof TemporalExpression) {
                task.schedule = new PeriodicalExpressionInfo((TemporalExpression) scheduleExpression);
            } else {
                task.schedule = PeriodicalExpressionInfo.from((PeriodicalScheduleExpression) scheduleExpression);
            }
        }
    }

    private void setStatusOnDate(DataValidationOccurrence dataValidationOccurrence, Thesaurus thesaurus) {
        DataValidationTaskStatus dataExportStatus = dataValidationOccurrence.getStatus();
        String statusTranslation =
                thesaurus.getStringBeyondComponent(dataExportStatus.toString(), dataExportStatus.toString());
        if (DataValidationTaskStatus.BUSY.equals(dataExportStatus)) {
            this.statusPrefix = statusTranslation + " " + thesaurus.getString("since", "since");
            this.statusDate = startedOn;
        } else if ((DataValidationTaskStatus.FAILED.equals(dataExportStatus)) || (DataValidationTaskStatus.SUCCESS.equals(dataExportStatus))) {
            this.statusPrefix = statusTranslation + " " + thesaurus.getString("on", "on");
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

    private static String getName(DataValidationTaskStatus status, Thesaurus thesaurus) {
        return thesaurus.getStringBeyondComponent(status.toString(), status.toString());
    }

    private String getScheduledTriggerDescription(DataValidationOccurrence dataValidationOccurrence, Thesaurus thesaurus, TimeService timeService) {
        ScheduleExpression scheduleExpression = dataValidationOccurrence.getTask().getScheduleExpression();
        if (Never.NEVER.equals(scheduleExpression)) {
            return null;
        }
        if (scheduleExpression instanceof PeriodicalScheduleExpression) {
            return fromPeriodicalScheduleExpression((PeriodicalScheduleExpression) scheduleExpression, timeService);
        }
        if (scheduleExpression instanceof TemporalExpression) {
            return fromTemporalExpression((TemporalExpression) scheduleExpression, thesaurus);
        }
        return scheduleExpression.toString();
    }

    private String fromPeriodicalScheduleExpression(PeriodicalScheduleExpression scheduleExpression, TimeService timeService) {
        return timeService.toLocalizedString(scheduleExpression);
    }

    private String fromTemporalExpression(TemporalExpression scheduleExpression, Thesaurus thesaurus) {
        TimeDuration every = scheduleExpression.getEvery();
        int count = every.getCount();
        TimeDuration.TimeUnit unit = every.getTimeUnit();
        String everyTranslation = thesaurus.getString("every", "every");

        String unitTranslation = unit.getDescription();
        if (unit.equals(TimeDuration.TimeUnit.DAYS)) {
            if (count == 1) {
                unitTranslation = thesaurus.getString("day", "day");
            } else {
                unitTranslation = thesaurus.getString("multipleDays", "days");
            }
        }
        else if (unit.equals(TimeDuration.TimeUnit.WEEKS)) {
            if (count == 1) {
                unitTranslation = thesaurus.getString("week", "week");
            } else {
                unitTranslation = thesaurus.getString("multipleWeeks", "weeks");
            }
        }
        else if (unit.equals(TimeDuration.TimeUnit.MONTHS)) {
            if (count == 1) {
                unitTranslation = thesaurus.getString("month", "month");
            } else {
                unitTranslation = thesaurus.getString("multipleMonths", "months");
            }
        }
        else if (unit.equals(TimeDuration.TimeUnit.YEARS)) {
            if (count == 1) {
                unitTranslation = thesaurus.getString("year", "year");
            } else {
                unitTranslation = thesaurus.getString("multipleYears", "years");
            }
        }
        if (count == 1) {
            return everyTranslation + " " + unitTranslation;
        } else {
            return everyTranslation + " " + count + " " + unitTranslation;
        }
    }
}
