package com.elster.jupiter.export.rest;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportStatus;
import com.elster.jupiter.nls.Thesaurus;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class DataExportTaskHistoryInfo {
    public Long id;
    public Long startedOn;
    public Long finishedOn;
    public Long duration;
    public String status;
    public String reason;
    public Long exportPeriodFrom;
    public Long exportPeriodTo;

    public DataExportTaskHistoryInfo() {
    }

    public DataExportTaskHistoryInfo (DataExportOccurrence dataExportOccurrence, Thesaurus thesaurus) {
        this.id = dataExportOccurrence.getTaskOccurenceId();
        this.startedOn = dataExportOccurrence.getStartDate().toEpochMilli();
        this.finishedOn = dataExportOccurrence.getEndDate().isPresent() ? dataExportOccurrence.getEndDate().get().toEpochMilli() : null;
        this.duration = calculateDuration(startedOn, finishedOn);
        this.status = getName(dataExportOccurrence.getStatus(), thesaurus);
        this.reason = dataExportOccurrence.getFailureReason();
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

    private static String getName(DataExportStatus status, Thesaurus thesaurus) {
        return thesaurus.getStringBeyondComponent(status.toString(), status.toString());
    }
}
