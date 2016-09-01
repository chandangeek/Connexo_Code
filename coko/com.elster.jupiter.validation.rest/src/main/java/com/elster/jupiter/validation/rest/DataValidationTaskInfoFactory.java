package com.elster.jupiter.validation.rest;

import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.nls.Thesaurus;
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
import com.elster.jupiter.validation.rest.impl.TranslationKeys;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Optional;

public class DataValidationTaskInfoFactory {

    private final Thesaurus thesaurus;
    private final TimeService timeService;
    private final DataValidationTaskHistoryInfoFactory dataValidationTaskHistoryInfoFactory;

    @Inject
    public DataValidationTaskInfoFactory(Thesaurus thesaurus, TimeService timeService, DataValidationTaskHistoryInfoFactory dataValidationTaskHistoryInfoFactory) {
        this.thesaurus = thesaurus;
        this.timeService = timeService;
        this.dataValidationTaskHistoryInfoFactory = dataValidationTaskHistoryInfoFactory;
    }

    public DataValidationTaskInfo asInfo(DataValidationTask dataValidationTask) {
        DataValidationTaskInfo info = new DataValidationTaskInfo();
        info.id = dataValidationTask.getId();
        info.name = dataValidationTask.getName();

        if (dataValidationTask.getEndDeviceGroup().isPresent()) {
            info.deviceGroup = new IdWithDisplayValueInfo<>(dataValidationTask.getEndDeviceGroup().get().getId(), dataValidationTask.getEndDeviceGroup().get().getName());
        }

        if (dataValidationTask.getMetrologyContract().isPresent()) {
            MetrologyContract contract = dataValidationTask.getMetrologyContract().get();
            info.metrologyContract = new IdWithDisplayValueInfo<>(contract.getId(), contract.getMetrologyPurpose().getName());
            info.metrologyConfiguration = new IdWithDisplayValueInfo<>(contract.getMetrologyConfiguration().getId(), contract.getMetrologyConfiguration().getName());
        }

        dataValidationTask.getLastOccurrence().ifPresent(dataValidationOccurrence -> {
            if (dataValidationOccurrence.wasScheduled()) {
                String scheduledTriggerDescription = this.getScheduledTriggerDescription(dataValidationOccurrence, thesaurus, timeService);
                if (scheduledTriggerDescription != null) {
                    info.recurrence = scheduledTriggerDescription;
                }
            } else {
                info.recurrence = thesaurus.getFormat(TranslationKeys.NONE).format();
            }
        });

        if (Never.NEVER.equals(dataValidationTask.getScheduleExpression())) {
            info.schedule = null;
        } else {
            ScheduleExpression scheduleExpression = dataValidationTask.getScheduleExpression();
            if (scheduleExpression instanceof TemporalExpression) {
                info.schedule = new PeriodicalExpressionInfo((TemporalExpression) scheduleExpression);
            } else {
                info.schedule = PeriodicalExpressionInfo.from((PeriodicalScheduleExpression) scheduleExpression);
            }
        }

        Instant nextExecution = dataValidationTask.getNextExecution();
        if (nextExecution != null) {
            info.nextRun = nextExecution.toEpochMilli();
        }

        Optional<Instant> lastRunOptional = dataValidationTask.getLastRun();
        if (lastRunOptional.isPresent()) {
            info.lastRun = lastRunOptional.get().toEpochMilli();
        }
        info.version = dataValidationTask.getVersion();
        return info;
    }

    public DataValidationTaskInfo asInfoWithHistory(DataValidationTask dataValidationTask) {
        DataValidationTaskInfo info = asInfo(dataValidationTask);
        info.lastValidationOccurence = dataValidationTask.getLastOccurrence().map(dataValidationTaskHistoryInfoFactory::asInfo).orElse(null);
        return info;
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
