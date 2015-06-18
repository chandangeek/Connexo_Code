package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportStatus;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.rest.PeriodicalExpressionInfo;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Optional;

import static com.elster.jupiter.export.rest.impl.MessageSeeds.Labels.ON_REQUEST;
import static com.elster.jupiter.export.rest.impl.MessageSeeds.Labels.SCHEDULED;

public class DataExportTaskHistoryInfo {

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
    public DataExportTaskInfo task;

    public DataExportTaskHistoryInfo() {
    }

    public DataExportTaskHistoryInfo(DataExportOccurrence dataExportOccurrence, Thesaurus thesaurus, TimeService timeService, PropertyUtils propertyUtils) {
        populate(dataExportOccurrence.getTask().getHistory(), dataExportOccurrence, thesaurus, timeService, propertyUtils);
    }

    public DataExportTaskHistoryInfo(History<ExportTask> history, DataExportOccurrence dataExportOccurrence, Thesaurus thesaurus, TimeService timeService, PropertyUtils propertyUtils) {
        populate(history, dataExportOccurrence, thesaurus, timeService, propertyUtils);
    }

    private void populate(History<ExportTask> history, DataExportOccurrence dataExportOccurrence, Thesaurus thesaurus, TimeService timeService, PropertyUtils propertyUtils) {
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
        this.status = getName(dataExportOccurrence.getStatus(), thesaurus);
        this.reason = dataExportOccurrence.getFailureReason();
        this.lastRun = dataExportOccurrence.getTriggerTime().toEpochMilli();
        Range<Instant> interval = dataExportOccurrence.getExportedDataInterval();
        this.exportPeriodFrom = interval.lowerEndpoint().toEpochMilli();
        this.exportPeriodTo = interval.upperEndpoint().toEpochMilli();
        setStatusOnDate(dataExportOccurrence, thesaurus);
        ExportTask version = history.getVersionAt(dataExportOccurrence.getTriggerTime())
                .orElseGet(() -> history.getVersionAt(dataExportOccurrence.getTask().getCreateTime())
                        .orElseGet(dataExportOccurrence::getTask));
        task = new DataExportTaskInfo();
        task.populate(version, thesaurus, timeService, propertyUtils);
        if (version != null) {
            populateForReadingTypeDataExportTask(version, dataExportOccurrence, thesaurus);
        }
        Optional<ScheduleExpression> foundSchedule = version.getScheduleExpression(dataExportOccurrence.getTriggerTime());
        if (!foundSchedule.isPresent() || Never.NEVER.equals(foundSchedule.get())) {
            task.schedule = null;
        } else if (foundSchedule.isPresent()) {
            ScheduleExpression scheduleExpression = foundSchedule.get();
            if (scheduleExpression instanceof TemporalExpression) {
                task.schedule = new PeriodicalExpressionInfo((TemporalExpression) scheduleExpression);
            } else {
                task.schedule = PeriodicalExpressionInfo.from((PeriodicalScheduleExpression) scheduleExpression);
            }
            task.properties = propertyUtils.convertPropertySpecsToPropertyInfos(version.getPropertySpecs(), version.getProperties(dataExportOccurrence.getTriggerTime()));
        }

    }

    private void populateForReadingTypeDataExportTask(ExportTask version, DataExportOccurrence dataExportOccurrence, Thesaurus thesaurus) {
        version.getReadingTypeDataSelector(dataExportOccurrence.getTriggerTime()).ifPresent(readingTypeDataSelector -> {
            task.standardDataSelector = new StandardDataSelectorInfo();
            task.standardDataSelector.populate(readingTypeDataSelector, thesaurus);
            for (ReadingType readingType : readingTypeDataSelector.getReadingTypes(dataExportOccurrence.getTriggerTime())) {
                task.standardDataSelector.readingTypes.add(new ReadingTypeInfo(readingType));
            }
        });
    }

    private void setStatusOnDate(DataExportOccurrence dataExportOccurrence, Thesaurus thesaurus) {
        DataExportStatus dataExportStatus = dataExportOccurrence.getStatus();
        String statusTranslation =
                thesaurus.getStringBeyondComponent(dataExportStatus.toString(), dataExportStatus.toString());
        if (DataExportStatus.BUSY.equals(dataExportStatus)) {
            this.statusPrefix = statusTranslation + " " + thesaurus.getString("since", "since");
            this.statusDate = startedOn;
        } else if ((DataExportStatus.FAILED.equals(dataExportStatus)) || (DataExportStatus.SUCCESS.equals(dataExportStatus))) {
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

    private static String getName(DataExportStatus status, Thesaurus thesaurus) {
        return thesaurus.getStringBeyondComponent(status.toString(), status.toString());
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
