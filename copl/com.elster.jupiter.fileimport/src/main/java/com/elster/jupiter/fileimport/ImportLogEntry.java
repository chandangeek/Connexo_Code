package com.elster.jupiter.fileimport;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.util.logging.LogEntry;

@ProviderType
public interface ImportLogEntry extends LogEntry {

    FileImportOccurrence getFileImportOccurrence();

}
