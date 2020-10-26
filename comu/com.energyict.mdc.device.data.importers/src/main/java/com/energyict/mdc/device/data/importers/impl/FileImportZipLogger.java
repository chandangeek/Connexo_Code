package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.upl.issue.Warning;

import java.util.zip.ZipFile;

public interface FileImportZipLogger {
    void init(FileImportOccurrence fileImportOccurrence);

    void info(String message);

    void warning(MessageSeed message, Object... arguments);

    void warning(Warning warning);

    void importZipEntryFailed(ZipFile zipFile, FileImportZipEntry data, Exception exception);

    void importZipEntryFinished(ZipFile zipFile, FileImportZipEntry data);

    void importFailed(Exception exception);

    void importFinished();

    void info(MessageSeeds message, Object... arguments);
}
