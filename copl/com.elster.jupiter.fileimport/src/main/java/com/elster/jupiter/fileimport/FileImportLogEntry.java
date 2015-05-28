package com.elster.jupiter.fileimport;


import com.elster.jupiter.util.logging.LogEntry;

public interface FileImportLogEntry extends LogEntry {

    FileImportOccurrence getFileImportOccurrence();

}
