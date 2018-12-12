/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.domain.util.VerboseConstraintViolationException;
import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.csvimport.exceptions.ImportException;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.data.importers.impl.attributes.PropertySpecAwareConstraintViolationException;
import com.energyict.mdc.upl.issue.Warning;

import javax.validation.ConstraintViolation;

public abstract class FileImportLoggerImpl<T extends FileImportRecord> implements FileImportLogger<T> {

    protected DeviceDataImporterContext context;
    protected FileImportOccurrence fileImportOccurrence;

    public FileImportLoggerImpl(DeviceDataImporterContext context) {
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
    public void warning(Warning warning) {
        fileImportOccurrence.getLogger().info(warning.getDescription());
    }

    @Override
    public void importLineFailed(T data, Exception exception) {
        String message = "";
        if (exception instanceof ImportException) {
            message = ((ImportException) exception).getLocalizedMessage(this.context.getThesaurus());
        } else if (exception instanceof VerboseConstraintViolationException) {
            VerboseConstraintViolationException constraintViolationException = (VerboseConstraintViolationException) exception;
            for (ConstraintViolation<?> constraintViolation : constraintViolationException.getConstraintViolations()) {
                message = this.context.getThesaurus()
                        .getFormat(TranslationKeys.IMPORT_DEFAULT_PROCESSOR_ERROR_TEMPLATE)
                        .format(data.getLineNumber(), data.getDeviceIdentifier(), constraintViolation.getMessage());
            }
        } else {
            // Always specify line number and device identifier
            message = this.context.getThesaurus()
                    .getFormat(TranslationKeys.IMPORT_DEFAULT_PROCESSOR_ERROR_TEMPLATE)
                    .format(data.getLineNumber(), data.getDeviceIdentifier(), exception.getLocalizedMessage());
        }
        fileImportOccurrence.getLogger().warning(message);
    }

    @Override
    public void importLineFailed(long lineNumber, Exception exception){
        String message;
        if (exception instanceof ImportException) {
            message = ((ImportException) exception).getLocalizedMessage(this.context.getThesaurus());
            fileImportOccurrence.getLogger().warning(message);
        } else if(exception instanceof PropertySpecAwareConstraintViolationException) {
            PropertySpecAwareConstraintViolationException constraintViolationException = (PropertySpecAwareConstraintViolationException) exception;
            for (ConstraintViolation<?> constraintViolation : constraintViolationException.getConstraintViolationException().getConstraintViolations()) {
                if (Checks.is(constraintViolation.getPropertyPath().toString()).emptyOrOnlyWhiteSpace()) {
                    message = this.context.getThesaurus()
                            .getFormat(TranslationKeys.IMPORT_MISSING_MANDATORY_PROCESSOR_ERROR_TEMPLATE)
                            .format(lineNumber, constraintViolationException.getPropertySpec().getName(), constraintViolation.getMessage());
                } else {
                    message = this.context.getThesaurus()
                            .getFormat(TranslationKeys.IMPORT_MISSING_MANDATORY_PROCESSOR_ERROR_PROPERTY_TEMPLATE)
                            .format(lineNumber, constraintViolationException.getPropertySpec().getName(), constraintViolation.getPropertyPath(), constraintViolation.getMessage());
                }
                fileImportOccurrence.getLogger().warning(message);
            }

        } else {
            message = this.context.getThesaurus()
                    .getFormat(TranslationKeys.IMPORT_DEFAULT_PROCESSOR_ERROR_TEMPLATE)
                    .format(lineNumber, exception.getLocalizedMessage());
            fileImportOccurrence.getLogger().warning(message);
        }
    }

    @Override
    public void importLineFinished(T data) {
        //nothing to do
    }

    @Override
    public void importFailed(Exception exception) {
        String message = exception.getLocalizedMessage();
        if (exception instanceof ImportException) {
            message = ((ImportException) exception).getLocalizedMessage(this.context.getThesaurus());
        }
        fileImportOccurrence.getLogger().severe(message);
        summarizeFailedImport();
    }

    @Override
    public void importFinished() {
        summarizeSuccessImport();
    }

    protected abstract void summarizeFailedImport();

    protected abstract void summarizeSuccessImport();

}
