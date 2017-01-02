package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.upl.issue.Warning;

public interface FileImportLogger<T extends FileImportRecord> {

    void init(FileImportOccurrence fileImportOccurrence);

    void warning(MessageSeed message, Object... arguments);

    void warning(TranslationKey message, Object... arguments);

    void warning(Warning warning);

    void importLineFailed(T data, Exception exception);

    void importLineFailed(long lineNumber, Exception exception);

    void importLineFinished(T data);

    void importFailed(Exception exception);

    void importFinished();

}
