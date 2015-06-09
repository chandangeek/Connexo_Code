package com.elster.jupiter.fileimport;


import com.elster.jupiter.util.logging.LogEntry;

public interface ImportLogEntry extends LogEntry {

    FileImportOccurrence getFileImportOccurrence();

}
