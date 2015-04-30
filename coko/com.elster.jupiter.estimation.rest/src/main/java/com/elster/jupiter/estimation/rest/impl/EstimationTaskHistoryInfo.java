package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskStatus;

import java.time.Instant;

public class EstimationTaskHistoryInfo {

    public Long id;
    public Long startedOn;
    public Long finishedOn;
    public Long duration;
    public String status;
    public Long lastRun;
    public Long statusDate;
    public String statusPrefix;
    public EstimationTaskInfo task;

    public EstimationTaskHistoryInfo() {
    }

    public EstimationTaskHistoryInfo(EstimationTask estimationTask, TaskOccurrence taskOccurrence, Thesaurus thesaurus) {
        populate(estimationTask, taskOccurrence, thesaurus);
    }

    private void populate(EstimationTask estimationTask, TaskOccurrence taskOccurrence, Thesaurus thesaurus) {
        this.id = taskOccurrence.getId();
        this.startedOn = taskOccurrence.getStartDate().map(this::toLong).orElse(null);
        this.finishedOn = taskOccurrence.getEndDate().map(this::toLong).orElse(null);
        this.duration = calculateDuration(startedOn, finishedOn);
        this.status = getName(taskOccurrence.getStatus(), thesaurus);
        this.lastRun = taskOccurrence.getTriggerTime().toEpochMilli();
        setStatusOnDate(taskOccurrence, thesaurus);
        History<EstimationTask> estTaskHistory = estimationTask.getHistory();
        EstimationTask version = estTaskHistory.getVersionAt(taskOccurrence.getTriggerTime())
                .orElseGet(() -> estTaskHistory.getVersionAt(taskOccurrence.getRecurrentTask().getCreateTime())
                        .orElse(estimationTask));
        task = new EstimationTaskInfo();
        task.populate(version, thesaurus);

//        Optional<ScheduleExpression> foundSchedule = version.getScheduleExpression(taskOccurrence.getTriggerTime());
//        if (!foundSchedule.isPresent() || Never.NEVER.equals(foundSchedule.get())) {
//            task.schedule = null;
//        } else if (foundSchedule.isPresent()) {
//            ScheduleExpression scheduleExpression = foundSchedule.get();
//            if (scheduleExpression instanceof TemporalExpression) {
//                task.schedule = new PeriodicalExpressionInfo((TemporalExpression) scheduleExpression);
//            } else {
//                task.schedule = PeriodicalExpressionInfo.from((PeriodicalScheduleExpression) scheduleExpression);
//            }
//        }
    }

    private static String getName(TaskStatus status, Thesaurus thesaurus) {
        return thesaurus.getStringBeyondComponent(status.toString(), status.toString());
    }

    private Long toLong(Instant instant) {
        return instant == null ? null : instant.toEpochMilli();
    }

    private static Long calculateDuration(Long startedOn, Long finishedOn) {
        if (startedOn == null || finishedOn == null) {
            return null;
        }
        return finishedOn - startedOn;
    }

    private void setStatusOnDate(TaskOccurrence taskOccurrence, Thesaurus thesaurus) {
        TaskStatus taskStatus = taskOccurrence.getStatus();
        String statusTranslation = thesaurus.getStringBeyondComponent(taskStatus.toString(), taskStatus.toString());
        if (TaskStatus.BUSY.equals(taskStatus)) {
            this.statusPrefix = statusTranslation + " " + thesaurus.getString("since", "since");
            this.statusDate = startedOn;
        } else if ((TaskStatus.FAILED.equals(taskStatus)) || (TaskStatus.SUCCESS.equals(taskStatus))) {
            this.statusPrefix = statusTranslation + " " + thesaurus.getString("on", "on");
            this.statusDate = finishedOn;
        } else {
            this.statusPrefix = statusTranslation;
        }
    }

//    private String getScheduledTriggerDescription(TaskOccurrence estimationTaskOccurrence, Thesaurus thesaurus, TimeService timeService) {
//        ScheduleExpression scheduleExpression = estimationTaskOccurrence.getTask().getScheduleExpression();
//        if (Never.NEVER.equals(scheduleExpression)) {
//            return null;
//        }
//        if (scheduleExpression instanceof PeriodicalScheduleExpression) {
//            return fromPeriodicalScheduleExpression((PeriodicalScheduleExpression) scheduleExpression, timeService);
//        }
//        if (scheduleExpression instanceof TemporalExpression) {
//            return fromTemporalExpression((TemporalExpression) scheduleExpression, thesaurus);
//        }
//        return scheduleExpression.toString();
//    }

//    private String fromPeriodicalScheduleExpression(PeriodicalScheduleExpression scheduleExpression, TimeService timeService) {
//        return timeService.toLocalizedString(scheduleExpression);
//    }
//
//    private String fromTemporalExpression(TemporalExpression scheduleExpression, Thesaurus thesaurus) {
//        TimeDuration every = scheduleExpression.getEvery();
//        int count = every.getCount();
//        TimeDuration.TimeUnit unit = every.getTimeUnit();
//        String everyTranslation = thesaurus.getString("every", "every");
//
//        String unitTranslation = unit.getDescription();
//        if (unit.equals(TimeDuration.TimeUnit.DAYS)) {
//            if (count == 1) {
//                unitTranslation = thesaurus.getString("day", "day");
//            } else {
//                unitTranslation = thesaurus.getString("multipleDays", "days");
//            }
//        } else if (unit.equals(TimeDuration.TimeUnit.WEEKS)) {
//            if (count == 1) {
//                unitTranslation = thesaurus.getString("week", "week");
//            } else {
//                unitTranslation = thesaurus.getString("multipleWeeks", "weeks");
//            }
//        } else if (unit.equals(TimeDuration.TimeUnit.MONTHS)) {
//            if (count == 1) {
//                unitTranslation = thesaurus.getString("month", "month");
//            } else {
//                unitTranslation = thesaurus.getString("multipleMonths", "months");
//            }
//        } else if (unit.equals(TimeDuration.TimeUnit.YEARS)) {
//            if (count == 1) {
//                unitTranslation = thesaurus.getString("year", "year");
//            } else {
//                unitTranslation = thesaurus.getString("multipleYears", "years");
//            }
//        }
//        if (count == 1) {
//            return everyTranslation + " " + unitTranslation;
//        } else {
//            return everyTranslation + " " + count + " " + unitTranslation;
//        }
//    }
}
