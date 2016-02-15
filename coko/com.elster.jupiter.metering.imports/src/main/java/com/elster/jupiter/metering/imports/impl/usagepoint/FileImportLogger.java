package com.elster.jupiter.metering.imports.impl.usagepoint;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;

public interface FileImportLogger<T extends FileImportRecord> {

    void init(FileImportOccurrence fileImportOccurrence);

    void warning(MessageSeed message, Object... arguments);

    void warning(TranslationKey message, Object... arguments);

    void importLineFailed(T data, Exception exception);

    void importLineFinished(T data);

    void importFailed(Exception exception);

    void importFinished();

}
