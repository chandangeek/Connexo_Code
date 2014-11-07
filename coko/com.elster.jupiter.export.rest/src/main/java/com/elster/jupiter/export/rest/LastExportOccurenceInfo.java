package com.elster.jupiter.export.rest;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportStatus;

import javax.xml.bind.annotation.XmlRootElement;
import java.time.Instant;

/**
 * Copyrights EnergyICT
 * Date: 30/10/2014
 * Time: 14:38
 */
@XmlRootElement
public class LastExportOccurenceInfo {
    public DataExportStatus status;
    public long lastRun;
    public Long startedOn;
    public Long finishedOn;
    public Long duration;

    public LastExportOccurenceInfo(DataExportOccurrence dataExportOccurrence) {

        status = dataExportOccurrence.getStatus();
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
}
