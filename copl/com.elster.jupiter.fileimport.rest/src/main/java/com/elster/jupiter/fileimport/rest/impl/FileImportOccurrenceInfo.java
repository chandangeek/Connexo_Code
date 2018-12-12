/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.rest.impl;

import com.elster.jupiter.fileimport.FileImportOccurrence;

/**
 * Created by Lucian on 6/2/2015.
 */
public class FileImportOccurrenceInfo {
    public Long id;

    public Long occurrenceId;
    public Long importServiceId;
    public String importServiceName;
    public Long triggeredOn;
    public Long startedOn;
    public Long finishedOn;
    public Long duration;
    public String fileName;
    public String status;
    public String summary;
    public boolean deleted;
    public Long now;

    public FileImportOccurrenceInfo(){
    }

    public FileImportOccurrenceInfo(FileImportOccurrence fileImportOccurrence) {
        this();
        this.id = this.occurrenceId = fileImportOccurrence.getId();
        this.fileName = fileImportOccurrence.getFileName();
        this.importServiceId = fileImportOccurrence.getImportSchedule().getId();
        this.importServiceName = fileImportOccurrence.getImportSchedule().getName();
        this.deleted = fileImportOccurrence.getImportSchedule().getObsoleteTime()!=null;
        triggeredOn = fileImportOccurrence.getTriggerDate().toEpochMilli();
        this.now = fileImportOccurrence.getClock().instant().toEpochMilli();
        fileImportOccurrence.getStartDate().ifPresent(sd->this.startedOn = sd.toEpochMilli());
        fileImportOccurrence.getEndDate().ifPresent(sd->this.finishedOn = sd.toEpochMilli());
        this.duration = calculateDuration(startedOn, finishedOn, now);
        this.status = fileImportOccurrence.getStatusName();
        this.summary = fileImportOccurrence.getMessage();
    }

    public static FileImportOccurrenceInfo of(FileImportOccurrence fileImportOccurrence) {
        return new FileImportOccurrenceInfo(fileImportOccurrence);
    }

    private static Long calculateDuration(Long startedOn, Long finishedOn, Long now) {
        if (startedOn == null) {
            return null;
        } else if (finishedOn == null) {
            return now - startedOn;
        }
        return finishedOn - startedOn;
    }

}