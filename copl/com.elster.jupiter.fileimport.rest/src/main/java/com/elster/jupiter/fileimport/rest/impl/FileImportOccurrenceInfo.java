package com.elster.jupiter.fileimport.rest.impl;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Created by Lucian on 6/2/2015.
 */
public class FileImportOccurrenceInfo {
    public Long id;

    public Long historyId;
    public Long importServiceId;
    public String importServiceName;
    public Long startedOn;
    public Long finishedOn;
    public Long duration;
    public String fileName;
    public String status;
    public String summary;

    public FileImportOccurrenceInfo(){

    }
    public FileImportOccurrenceInfo(FileImportOccurrence fileImportOccurrence, Thesaurus thesaurus){
        this.id = this.historyId = fileImportOccurrence.getId();
        this.fileName = fileImportOccurrence.getFileName();
        this.importServiceId = fileImportOccurrence.getImportSchedule().getId();
        this.importServiceName = fileImportOccurrence.getImportSchedule().getName();
        fileImportOccurrence.getStartDate().ifPresent(sd->this.startedOn = sd.toEpochMilli());
        fileImportOccurrence.getEndDate().ifPresent(sd->this.finishedOn = sd.toEpochMilli());
        this.duration = calculateDuration(startedOn, finishedOn);
        this.status = fileImportOccurrence.getStatus().toString();
        //this.summary = fileImportOccurrence.

    }


    private static Long calculateDuration(Long startedOn, Long finishedOn) {
        if (startedOn == null || finishedOn == null) {
            return null;
        }
        return finishedOn - startedOn;
    }

}
