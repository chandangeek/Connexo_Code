package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportStatus;
import com.elster.jupiter.nls.Thesaurus;
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
}
