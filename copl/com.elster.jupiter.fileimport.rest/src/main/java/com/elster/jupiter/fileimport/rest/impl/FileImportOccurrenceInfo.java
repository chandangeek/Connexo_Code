package com.elster.jupiter.fileimport.rest.impl;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.Status;
import com.elster.jupiter.nls.Thesaurus;

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

    public FileImportOccurrenceInfo(){

    }
    public FileImportOccurrenceInfo(FileImportOccurrence fileImportOccurrence, Thesaurus thesaurus){
        this.id = this.occurrenceId = fileImportOccurrence.getId();
        this.fileName = fileImportOccurrence.getFileName();
        this.importServiceId = fileImportOccurrence.getImportSchedule().getId();
        this.importServiceName = fileImportOccurrence.getImportSchedule().getName();
        this.deleted = fileImportOccurrence.getImportSchedule().getObsoleteTime()!=null;
        triggeredOn = fileImportOccurrence.getTriggerDate().toEpochMilli();
        fileImportOccurrence.getStartDate().ifPresent(sd->this.startedOn = sd.toEpochMilli());
        fileImportOccurrence.getEndDate().ifPresent(sd->this.finishedOn = sd.toEpochMilli());
        this.duration = calculateDuration(startedOn, finishedOn);
        this.status = getStatusDescription(fileImportOccurrence.getStatus(), thesaurus);
        this.summary = fileImportOccurrence.getMessage();
    }

    public static FileImportOccurrenceInfo of(FileImportOccurrence fileImportOccurrence, Thesaurus thesaurus) {
        return new FileImportOccurrenceInfo(fileImportOccurrence, thesaurus);
    }

    private String getStatusDescription(Status status,Thesaurus thesaurus ){
        return thesaurus.getStringBeyondComponent(status.toString(), status.toString());
    }


    private static Long calculateDuration(Long startedOn, Long finishedOn) {
        if (startedOn == null || finishedOn == null) {
            return null;
        }
        return finishedOn - startedOn;
    }

}
