package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportStatus;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.google.common.collect.Range;

import java.time.Instant;

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

    public DataExportTaskHistoryInfo() {
    }

    public DataExportTaskHistoryInfo (DataExportOccurrence dataExportOccurrence, Thesaurus thesaurus) {
        this.id = dataExportOccurrence.getId();

        this.trigger = (dataExportOccurrence.wasScheduled() ? SCHEDULED : ON_REQUEST).translate(thesaurus);
        if (dataExportOccurrence.wasScheduled()) {
            this.trigger = this.trigger + " (" + this.getScheduledTriggerDescription(dataExportOccurrence, thesaurus) + ")";
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

    private String getScheduledTriggerDescription(DataExportOccurrence dataExportOccurrence, Thesaurus thesaurus) {
        ScheduleExpression scheduleExpression = dataExportOccurrence.getTask().getScheduleExpression();
        TimeDuration every = ((TemporalExpression) scheduleExpression).getEvery();
        int count = every.getCount();
        TimeDuration.TimeUnit unit = every.getTimeUnit();
        String everyTranslation = thesaurus.getStringBeyondComponent("every", "every");

        String unitTranslation = unit.getDescription();
        if (unit.equals(TimeDuration.TimeUnit.DAYS)) {
            if (count == 1) {
                unitTranslation = thesaurus.getStringBeyondComponent("day", "day");
            } else {
                unitTranslation = thesaurus.getStringBeyondComponent("multipleDays", "days");
            }
        }
        else if (unit.equals(TimeDuration.TimeUnit.WEEKS)) {
            if (count == 1) {
                unitTranslation = thesaurus.getStringBeyondComponent("week", "week");
            } else {
                unitTranslation = thesaurus.getStringBeyondComponent("multipleWeeks", "weeks");
            }
        }
        else if (unit.equals(TimeDuration.TimeUnit.MONTHS)) {
            if (count == 1) {
                unitTranslation = thesaurus.getStringBeyondComponent("month", "month");
            } else {
                unitTranslation = thesaurus.getStringBeyondComponent("multipleMonths", "months");
            }
        }
        else if (unit.equals(TimeDuration.TimeUnit.YEARS)) {
            if (count == 1) {
                unitTranslation = thesaurus.getStringBeyondComponent("year", "year");
            } else {
                unitTranslation = thesaurus.getStringBeyondComponent("multipleYears", "years");
            }
        }
         if (count == 1) {
             return everyTranslation + " " + unitTranslation;
         } else {
             return everyTranslation + " " + count + " " + unitTranslation;
         }
    }
}
