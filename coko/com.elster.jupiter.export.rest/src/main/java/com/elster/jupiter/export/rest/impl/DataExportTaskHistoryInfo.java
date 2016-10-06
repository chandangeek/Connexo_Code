package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportDestination;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportStatus;
import com.elster.jupiter.export.DataExportStrategy;
import com.elster.jupiter.export.DefaultSelectorOccurrence;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.export.StandardDataSelector;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.time.rest.PeriodicalExpressionInfo;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.util.time.ScheduleExpression;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Optional;

import static com.elster.jupiter.export.rest.impl.MessageSeeds.Labels.NONRECURRING;
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
    public Long updatePeriodFrom;
    public Long updatePeriodTo;
    public Long statusDate;
    public String statusPrefix;
    public DataExportTaskInfo task;
    public String summary;

    public DataExportTaskHistoryInfo() {
    }

    public DataExportTaskHistoryInfo(DataExportOccurrence dataExportOccurrence, Thesaurus thesaurus, TimeService timeService, PropertyValueInfoService propertyValueInfoService) {
        populate(dataExportOccurrence.getTask().getHistory(), dataExportOccurrence, thesaurus, timeService, propertyValueInfoService);
    }

    public DataExportTaskHistoryInfo(History<ExportTask> history, DataExportOccurrence dataExportOccurrence, Thesaurus thesaurus, TimeService timeService, PropertyValueInfoService propertyValueInfoService) {
        populate(history, dataExportOccurrence, thesaurus, timeService, propertyValueInfoService);
    }

    private void populate(History<ExportTask> history, DataExportOccurrence dataExportOccurrence, Thesaurus thesaurus, TimeService timeService, PropertyValueInfoService propertyValueInfoService) {
        this.id = dataExportOccurrence.getId();
        this.summary = dataExportOccurrence.getSummary();

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
        ExportTask version = history.getVersionAt(dataExportOccurrence.getStartDate().get())
                .orElseGet(() -> history.getVersionAt(dataExportOccurrence.getTask().getCreateTime())
                        .orElseGet(dataExportOccurrence::getTask));

        dataExportOccurrence.getDefaultSelectorOccurrence()
                .map(DefaultSelectorOccurrence::getExportedDataInterval)
                .ifPresent(interval -> {
                    this.exportPeriodFrom = interval.lowerEndpoint().toEpochMilli();
                    this.exportPeriodTo = interval.upperEndpoint().toEpochMilli();
                });
        version.getReadingTypeDataSelector(dataExportOccurrence.getStartDate().get())
                .map(StandardDataSelector::getStrategy)
                .flatMap(DataExportStrategy::getUpdatePeriod)
                .map(relativePeriod -> relativePeriod.getOpenClosedInterval(ZonedDateTime.ofInstant(dataExportOccurrence.getTriggerTime(), ZoneId.systemDefault())))
                .ifPresent(interval -> {
                    this.updatePeriodFrom = interval.lowerEndpoint().toEpochMilli();
                    this.updatePeriodTo = interval.upperEndpoint().toEpochMilli();
                });
        setStatusOnDate(dataExportOccurrence, thesaurus);
        task = new DataExportTaskInfo();
        task.populate(version, thesaurus, timeService, propertyValueInfoService);
        populateForReadingTypeDataExportTask(version, dataExportOccurrence, thesaurus);
        version.getDestinations(dataExportOccurrence.getStartDate().get()).stream()
                .sorted((d1, d2) -> d1.getCreateTime().compareTo(d2.getCreateTime()))
                .forEach(destination -> task.destinations.add(typeOf(destination).toInfo(destination)));
        task.dataProcessor.properties = propertyValueInfoService.getPropertyInfos(version.getDataProcessorPropertySpecs(), version.getProperties());
        Optional<ScheduleExpression> foundSchedule = version.getScheduleExpression(dataExportOccurrence.getStartDate().get());
        if (!foundSchedule.isPresent() || Never.NEVER.equals(foundSchedule.get())) {
            task.schedule = null;
        } else if (foundSchedule.isPresent()) {
            ScheduleExpression scheduleExpression = foundSchedule.get();
            if (scheduleExpression instanceof TemporalExpression) {
                task.schedule = new PeriodicalExpressionInfo((TemporalExpression) scheduleExpression);
            } else {
                task.schedule = PeriodicalExpressionInfo.from((PeriodicalScheduleExpression) scheduleExpression);
            }
            task.dataSelector.properties = propertyValueInfoService.getPropertyInfos(version.getDataSelectorPropertySpecs(), version.getProperties(dataExportOccurrence.getTriggerTime()));
        }

        if (dataExportOccurrence.wasScheduled() && task.schedule==null) {
            this.trigger = NONRECURRING.translate(thesaurus);
        }
    }

    private void populateForReadingTypeDataExportTask(ExportTask version, DataExportOccurrence dataExportOccurrence, Thesaurus thesaurus) {
        ReadingTypeInfoFactory readingTypeInfoFactory = new ReadingTypeInfoFactory(thesaurus);
        version.getReadingTypeDataSelector(dataExportOccurrence.getStartDate().get()).ifPresent(readingTypeDataSelector -> {
            task.standardDataSelector = new StandardDataSelectorInfo();
            task.standardDataSelector.populateFrom(readingTypeDataSelector, thesaurus);
            for (ReadingType readingType : readingTypeDataSelector.getReadingTypes(dataExportOccurrence.getStartDate().get())) {
                task.standardDataSelector.readingTypes.add(readingTypeInfoFactory.from(readingType));
            }
        });
    }

    private DestinationType typeOf(DataExportDestination destination) {
        return Arrays.stream(DestinationType.values())
                .filter(type -> type.getDestinationClass().isInstance(destination))
                .findAny()
                .orElseThrow(IllegalArgumentException::new);
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
        return thesaurus.getStringBeyondComponent(status.getKey(), status.getDefaultFormat());
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
