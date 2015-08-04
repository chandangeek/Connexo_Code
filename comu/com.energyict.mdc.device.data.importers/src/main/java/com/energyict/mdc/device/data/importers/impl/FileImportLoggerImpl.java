package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.device.data.importers.impl.exceptions.ImportException;

import java.text.MessageFormat;

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
    public void warning(TranslationKey message, Object... arguments) {
        String msg = context.getThesaurus().getString(message.getKey(), message.getDefaultFormat());
        if (arguments != null && arguments.length > 0) {
            msg = MessageFormat.format(msg, arguments);
        }
        fileImportOccurrence.getLogger().info(msg);
    }

    @Override
    public void importLineFailed(T data, Exception exception) {
        String message = null;
        if (exception instanceof ImportException) {
            message = ((ImportException) exception).getLocalizedMessage(this.context.getThesaurus());
        } else {
            // Always specify line number and device mrid
            message = TranslationKeys.IMPORT_DEFAULT_PROCESSOR_ERROR_TEMPLATE.getTranslated(this.context.getThesaurus(),
                    data.getLineNumber(), data.getDeviceMRID(), exception.getLocalizedMessage());
        }
        fileImportOccurrence.getLogger().warning(message);
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
