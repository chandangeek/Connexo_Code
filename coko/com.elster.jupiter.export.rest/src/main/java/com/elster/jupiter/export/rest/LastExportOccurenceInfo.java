package com.elster.jupiter.export.rest;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportStatus;
import com.elster.jupiter.nls.Thesaurus;

import java.time.Instant;

/**
 * Copyrights EnergyICT
 * Date: 30/10/2014
 * Time: 14:38
 */
public class LastExportOccurenceInfo {
    public String status;
    public long lastRun;
    public Long startedOn;
    public Long finishedOn;
    public Long duration;

    public LastExportOccurenceInfo(DataExportOccurrence dataExportOccurrence, Thesaurus thesaurus) {

        status = getName(dataExportOccurrence.getStatus(), thesaurus);
        lastRun = dataExportOccurrence.getTriggerTime().toEpochMilli();
        startedOn = toLong(dataExportOccurrence.getStartDate());
        finishedOn = dataExportOccurrence.getEndDate().map(this::toLong).orElse(null);
        duration = calculateDuration(startedOn, finishedOn);
    }

    private Long calculateDuration(Long startedOn, Long finishedOn) {
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
