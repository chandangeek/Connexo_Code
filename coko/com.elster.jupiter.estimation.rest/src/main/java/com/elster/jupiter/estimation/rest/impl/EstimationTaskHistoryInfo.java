package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.estimation.EstimationTaskOccurrence;
import com.elster.jupiter.estimation.EstimationTaskStatus;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.rest.PeriodicalExpressionInfo;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.util.time.ScheduleExpression;

import java.time.Instant;
import java.util.Optional;

public class EstimationTaskHistoryInfo {

    public Long id;
    public Long startedOn;
    public Long finishedOn;
    public Long duration;
    public String status;
    public String reason;
    public Long lastRun;
    public Long statusDate;
    public String statusPrefix;
    public EstimationTaskInfo task;

    public EstimationTaskHistoryInfo() {
    }

    public EstimationTaskHistoryInfo(EstimationTaskOccurrence estimationTaskOccurrence, Thesaurus thesaurus) {
        populate((History<EstimationTask>) estimationTaskOccurrence.getTask().getHistory(), estimationTaskOccurrence, thesaurus);
    }

    public EstimationTaskHistoryInfo(History<? extends EstimationTask> history, EstimationTaskOccurrence estimationTaskOccurrence, Thesaurus thesaurus) {
        populate((History<EstimationTask>) history, estimationTaskOccurrence, thesaurus);
    }

    private void populate(History<EstimationTask> history, EstimationTaskOccurrence estimationTaskOccurrence, Thesaurus thesaurus) {
        this.id = estimationTaskOccurrence.getId();
        this.startedOn = estimationTaskOccurrence.getStartDate().map(this::toLong).orElse(null);
        this.finishedOn = estimationTaskOccurrence.getEndDate().map(this::toLong).orElse(null);
        this.duration = calculateDuration(startedOn, finishedOn);
        this.status = getName(estimationTaskOccurrence.getStatus(), thesaurus);
        this.reason = estimationTaskOccurrence.getFailureReason();
        this.lastRun = estimationTaskOccurrence.getTriggerTime().toEpochMilli();
        setStatusOnDate(estimationTaskOccurrence, thesaurus);
        EstimationTask version = history.getVersionAt(estimationTaskOccurrence.getTriggerTime())
                .orElseGet(() -> history.getVersionAt(estimationTaskOccurrence.getTask().getCreateTime())
                        .orElseGet(estimationTaskOccurrence::getTask));
        task = new EstimationTaskInfo();
        task.populate(version, thesaurus);

        Optional<ScheduleExpression> foundSchedule = version.getScheduleExpression(estimationTaskOccurrence.getTriggerTime());
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

    private void setStatusOnDate(EstimationTaskOccurrence estimationTaskOccurrence, Thesaurus thesaurus) {
        EstimationTaskStatus estimationTaskStatus = estimationTaskOccurrence.getStatus();
        String statusTranslation = thesaurus.getStringBeyondComponent(estimationTaskStatus.toString(), estimationTaskStatus.toString());
        if (EstimationTaskStatus.BUSY.equals(estimationTaskStatus)) {
            this.statusPrefix = statusTranslation + " " + thesaurus.getString("since", "since");
            this.statusDate = startedOn;
        } else if ((EstimationTaskStatus.FAILED.equals(estimationTaskStatus)) || (EstimationTaskStatus.SUCCESS.equals(estimationTaskStatus))) {
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

    private static String getName(EstimationTaskStatus status, Thesaurus thesaurus) {
        return thesaurus.getStringBeyondComponent(status.toString(), status.toString());
    }

    private String getScheduledTriggerDescription(EstimationTaskOccurrence estimationTaskOccurrence, Thesaurus thesaurus, TimeService timeService) {
        ScheduleExpression scheduleExpression = estimationTaskOccurrence.getTask().getScheduleExpression();
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
        } else if (unit.equals(TimeDuration.TimeUnit.WEEKS)) {
            if (count == 1) {
                unitTranslation = thesaurus.getString("week", "week");
            } else {
                unitTranslation = thesaurus.getString("multipleWeeks", "weeks");
            }
        } else if (unit.equals(TimeDuration.TimeUnit.MONTHS)) {
            if (count == 1) {
                unitTranslation = thesaurus.getString("month", "month");
            } else {
                unitTranslation = thesaurus.getString("multipleMonths", "months");
            }
        } else if (unit.equals(TimeDuration.TimeUnit.YEARS)) {
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
