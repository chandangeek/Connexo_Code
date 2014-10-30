package com.elster.jupiter.export.rest;

import com.elster.jupiter.export.DataExportStatus;

/**
 * Copyrights EnergyICT
 * Date: 30/10/2014
 * Time: 14:38
 */
public class LastExportOccurenceInfo {
    public DataExportStatus status;
    public long lastRun;
    public long startedOn;
    public long finishedOn;
    public long duration;

}
