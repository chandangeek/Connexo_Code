package com.elster.jupiter.fileimport.rest.impl;


import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.nls.Thesaurus;

import java.util.ArrayList;
import java.util.List;

public class FileImportScheduleHistoryInfo {

    public int total;
    public List<FileImportOccurrenceInfo> data = new ArrayList<>();

    public FileImportScheduleHistoryInfo(){

    }

    public FileImportScheduleHistoryInfo(Iterable<? extends FileImportOccurrence> occurrences, Thesaurus thesaurus){

        addAll(occurrences, thesaurus);
    }

    private void addAll(Iterable<? extends FileImportOccurrence> occurrences, Thesaurus thesaurus) {
        for (FileImportOccurrence each : occurrences) {
            add(each, thesaurus);
        }
    }

    private FileImportOccurrenceInfo add(FileImportOccurrence occurrence, Thesaurus thesaurus) {

        FileImportOccurrenceInfo result = FileImportOccurrenceInfo.of(occurrence, thesaurus);
        data.add(result);
        total++;
        return result;

    }
}
