package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.rest.PeriodicalExpressionInfo;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.google.common.collect.Range;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import static com.elster.jupiter.estimation.rest.impl.MessageSeeds.Labels.ON_REQUEST;
import static com.elster.jupiter.estimation.rest.impl.MessageSeeds.Labels.SCHEDULED;

public class EstimationTaskHistoryInfo {

    public Long id;
    public String trigger;
    public Long startedOn;
    public Long finishedOn;
    public Long duration;
    public Long lastRun;
    public Long periodFrom;
    public Long periodTo;
    public Long statusDate;
    public String statusPrefix;
    public EstimationTaskInfo task;

    public EstimationTaskHistoryInfo() {
    }

    public EstimationTaskHistoryInfo(History<? extends EstimationTask> history, TaskOccurrence taskOccurrence, Thesaurus thesaurus, TimeService timeService) {
        populate(history, taskOccurrence, thesaurus, timeService);
    }

    private void populate(History<? extends EstimationTask> history, TaskOccurrence taskOccurrence, Thesaurus thesaurus, TimeService timeService) {
        EstimationTask estimationTask = history.getVersionAt(taskOccurrence.getTriggerTime()).orElseThrow(IllegalStateException::new);
        this.id = taskOccurrence.getId();

        this.trigger = (taskOccurrence.wasScheduled() ? SCHEDULED : ON_REQUEST).translate(thesaurus);
        if (taskOccurrence.wasScheduled()) {
            String scheduledTriggerDescription = this.getScheduledTriggerDescription(taskOccurrence, thesaurus, timeService);
            if (scheduledTriggerDescription != null) {
                this.trigger = this.trigger + " (" + scheduledTriggerDescription + ")";
            }
        }
        this.startedOn = taskOccurrence.getStartDate().map(this::toLong).orElse(null);
        this.finishedOn = taskOccurrence.getEndDate().map(this::toLong).orElse(null);
        this.duration = calculateDuration(startedOn, finishedOn);
        this.lastRun = taskOccurrence.getTriggerTime().toEpochMilli();
        estimationTask.getPeriod().ifPresent(relativePeriod -> {
            Range<ZonedDateTime> interval = relativePeriod.getInterval(ZonedDateTime.ofInstant(taskOccurrence.getTriggerTime(), ZoneId.systemDefault()));
            this.periodFrom = interval.lowerEndpoint().toInstant().toEpochMilli();
            this.periodTo = interval.upperEndpoint().toInstant().toEpochMilli();
        });
        task = new EstimationTaskInfo();
        task.doPopulate(estimationTask, thesaurus, timeService);

        Optional<ScheduleExpression> foundSchedule = estimationTask.getScheduleExpression(taskOccurrence.getTriggerTime());
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

    private static Long calculateDuration(Long startedOn, Long finishedOn) {
        if (startedOn == null || finishedOn == null) {
            return null;
        }
        return finishedOn - startedOn;
    }

    private Long toLong(Instant instant) {
        return instant == null ? null : instant.toEpochMilli();
    }

    private String getScheduledTriggerDescription(TaskOccurrence taskOccurrence, Thesaurus thesaurus, TimeService timeService) {
        ScheduleExpression scheduleExpression = taskOccurrence.getRecurrentTask().getScheduleExpression();
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
