package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.csvimport.exceptions.ImportException;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.upl.issue.Warning;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.zip.ZipFile;

public abstract class FileImportZipLoggerImpl implements FileImportZipLogger {
    protected DeviceDataImporterContext context;
    protected FileImportOccurrence fileImportOccurrence;

    public FileImportZipLoggerImpl(DeviceDataImporterContext context) {
        this.context = context;
    }

    @Override
    public void init(FileImportOccurrence fileImportOccurrence) {
        this.fileImportOccurrence = fileImportOccurrence;
    }

    @Override
    public void info(String message) {
        fileImportOccurrence.getLogger().info(message);
    }

    @Override
    public void info(MessageSeeds message, Object... arguments) {
        fileImportOccurrence.getLogger().info(format(message.getDefaultFormat(), arguments));
    }

    @Override
    public void warning(MessageSeed message, Object... arguments) {
        fileImportOccurrence.getLogger().info(format(message.getDefaultFormat(), arguments));
    }

    @Override
    public void warning(Warning warning) {
        fileImportOccurrence.getLogger().info(warning.getDescription());
    }

    @Override
    public void importZipEntryFailed(ZipFile zipFile, FileImportZipEntry data, Exception exception) {
        String message = this.context.getThesaurus()
                .getFormat(TranslationKeys.IMPORT_ZIP_PROCESSOR_ERROR_TEMPLATE)
                .format(zipFile.getName(), data.getDirectory() + "/" + data.getFileName(), exception.getLocalizedMessage());

        fileImportOccurrence.getLogger().warning(message);
    }

    @Override
    public void importZipEntryFinished(ZipFile zipFile, FileImportZipEntry data) {
    }

    @Override
    public void importFailed(Exception exception) {
        String message = exception.getLocalizedMessage();
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

    private String format(String message, Object... args) {
        return new MessageFormat(message).format(args);
    }
}
