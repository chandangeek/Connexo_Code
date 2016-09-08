package com.elster.jupiter.validation.rest;

import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.rest.util.IdWithDisplayValueInfo;
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
        if (dataValidationTask.getEndDeviceGroup().isPresent()) {
            info.deviceGroup = new IdWithDisplayValueInfo<>(dataValidationTask.getEndDeviceGroup().get().getId(), dataValidationTask.getEndDeviceGroup().get().getName());
        }
        if (dataValidationTask.getMetrologyContract().isPresent()) {
            MetrologyContract contract = dataValidationTask.getMetrologyContract().get();
            info.metrologyContract = new IdWithDisplayValueInfo<>(contract.getId(), contract.getMetrologyPurpose().getName());
            info.metrologyConfiguration = new IdWithDisplayValueInfo<>(contract.getMetrologyConfiguration().getId(), contract.getMetrologyConfiguration().getName());
        }
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
        TimeDuration every = scheduleExpression.getEvery();
        int count = every.getCount();
        TimeDuration.TimeUnit unit = every.getTimeUnit();
        String everyTranslation = thesaurus.getString("Every", "Every");

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

    public DataValidationTaskHistoryInfo asInfo(DataValidationOccurrence dataValidationOccurrence) {
        return asInfo((History<DataValidationTask>) dataValidationOccurrence.getTask().getHistory(), dataValidationOccurrence);
    }

    public DataValidationTaskHistoryInfo asInfo(History<DataValidationTask> history, DataValidationOccurrence dataValidationOccurrence) {
        DataValidationTaskHistoryInfo info = new DataValidationTaskHistoryInfo();
        info.id = dataValidationOccurrence.getId();
        info.startedOn = dataValidationOccurrence.getStartDate().orElse(null);
        info.finishedOn = dataValidationOccurrence.getEndDate().orElse(null);
        info.duration = calculateDuration(info.startedOn, info.finishedOn);
        info.status = getName(dataValidationOccurrence.getStatus());
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
        String statusTranslation =
                thesaurus.getStringBeyondComponent(dataExportStatus.toString(), dataExportStatus.toString());
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

    private String getName(DataValidationTaskStatus status) {
        return thesaurus.getStringBeyondComponent(status.toString(), status.toString());
    }
}
