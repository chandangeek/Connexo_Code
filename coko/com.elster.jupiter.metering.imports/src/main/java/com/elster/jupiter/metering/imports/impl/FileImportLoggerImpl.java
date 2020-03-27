/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.imports.impl;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.csvimport.FileImportLogger;
import com.elster.jupiter.fileimport.csvimport.FileImportRecord;
import com.elster.jupiter.fileimport.csvimport.exceptions.ImportException;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.logging.Level;

public abstract class FileImportLoggerImpl<T extends FileImportRecord> implements FileImportLogger<T> {

    protected MeteringDataImporterContext context;
    protected FileImportOccurrence fileImportOccurrence;

    public FileImportLoggerImpl(MeteringDataImporterContext context) {
        this.context = context;
    }

    @Override
    public void init(FileImportOccurrence fileImportOccurrence) {
        this.fileImportOccurrence = fileImportOccurrence;
    }

    @Override
    public void warning(MessageSeed message, Object... arguments) {
        fileImportOccurrence.getLogger().info(context.getThesaurus().getFormat(message).format(arguments));
    }

    @Override
    public void warning(TranslationKey message, Object... arguments) {
        fileImportOccurrence.getLogger().info(context.getThesaurus().getFormat(message).format(arguments));
    }

    @Override
    public void importLineFailed(T data, Exception exception) {
        importLineFailed(data.getLineNumber(), exception);
    }

    @Override
    public void importLineFailed(long lineNumber, Exception exception) {
        String message;
        if (exception instanceof ImportException) {
            message = ((ImportException) exception).getLocalizedMessage(this.context.getThesaurus());
            fileImportOccurrence.getLogger().warning(message);
        } else {
            // Always specify line number and mrid
            message = this.context.getThesaurus()
                    .getFormat(TranslationKeys.Labels.IMPORT_DEFAULT_PROCESSOR_ERROR_TEMPLATE)
                    .format(lineNumber, exception.toString());
            fileImportOccurrence.getLogger().log(Level.WARNING, message, exception);
        }
    }

    @Override
    public void importLineFinished(T data) {
        //nothing to do
    }

    @Override
    public void importFailed(Exception exception) {
        String message = exception.toString();
        if (exception instanceof ImportException) {
            message = ((ImportException) exception).getLocalizedMessage(this.context.getThesaurus());
            fileImportOccurrence.getLogger().severe(message);
        } else {
            fileImportOccurrence.getLogger().log(Level.SEVERE, message, exception);
        }
        summarizeFailedImport();
    }

    @Override
    public void importFinished() {
        summarizeSuccessImport();
    }

    protected abstract void summarizeFailedImport();

    protected abstract void summarizeSuccessImport();

}
