package com.energyict.mdc.device.data.importers.impl;

public class DevicePerLineFileImportLogger extends FileImportLoggerImpl<FileImportRecord> {

    protected int linesWithError = 0;
    protected int linesProcessed = 0;

    public DevicePerLineFileImportLogger(DeviceDataImporterContext context) {
        super(context);
    }

    @Override
    public void importLineFinished(FileImportRecord data) {
        super.importLineFinished(data);
        linesProcessed++;
    }

    @Override
    public void importLineFailed(FileImportRecord data, Exception exception) {
        super.importLineFailed(data, exception);
        this.linesWithError++;
    }

    protected void summarizeFailedImport() {
        if (linesWithError != 0 && warnings == 0) {
            // Some devices were processed with errors
            fileImportOccurrence.markFailure(TranslationKeys.IMPORT_RESULT_FAIL_WITH_ERRORS
                    .getTranslated(this.context.getThesaurus(), linesProcessed, linesWithError));
        } else if (linesWithError != 0 && warnings != 0) {
            // Some devices were processed with errors and warnings
            fileImportOccurrence.markFailure(TranslationKeys.IMPORT_RESULT_FAIL_WITH_WARN_AND_ERRORS
                    .getTranslated(this.context.getThesaurus(), linesProcessed, warnings, linesWithError));
        } else if (linesWithError == 0 && warnings != 0) {
            // Some devices were processed with warnings
            fileImportOccurrence.markFailure(TranslationKeys.IMPORT_RESULT_FAIL_WITH_WARN
                    .getTranslated(this.context.getThesaurus(), linesProcessed, warnings));
        } else if (linesProcessed != 0 && linesWithError == 0 && warnings == 0) {
            // Some devices were processed
            fileImportOccurrence.markFailure(TranslationKeys.IMPORT_RESULT_FAIL
                    .getTranslated(this.context.getThesaurus(), linesProcessed));
        } else if (linesProcessed == 0 && linesWithError == 0 && warnings == 0) {
            // No devices were processed (Bad column headers)
            fileImportOccurrence.markFailure(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED
                    .getTranslated(this.context.getThesaurus()));
        }
    }

    protected void summarizeSuccessImport() {
        if (linesProcessed == 0 && linesWithError == 0) {
            // No devices were processed (No devices in file)
            fileImportOccurrence.markFailure(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED
                    .getTranslated(this.context.getThesaurus()));
        } else if (linesProcessed != 0 && linesWithError == 0 && warnings == 0) {
            // All devices were processed without warnings/errors
            fileImportOccurrence.markSuccess(TranslationKeys.IMPORT_RESULT_SUCCESS
                    .getTranslated(this.context.getThesaurus(), linesProcessed));
        } else if (linesWithError != 0 && warnings == 0) {
            // All devices were processed but some of the devices failed
            fileImportOccurrence.markSuccessWithFailures(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_ERRORS
                    .getTranslated(this.context.getThesaurus(), linesProcessed, linesWithError));
        } else if (linesWithError != 0 && warnings != 0) {
            // All devices were processed but part of them were processed with warnings and failures
            fileImportOccurrence.markSuccessWithFailures(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_WARN_AND_ERRORS
                    .getTranslated(this.context.getThesaurus(), linesProcessed, warnings, linesWithError));
        } else if (linesWithError == 0 && warnings != 0) {
            // All devices were processed but part of them were processed with warnings
            fileImportOccurrence.markSuccess(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_WARN
                    .getTranslated(this.context.getThesaurus(), linesProcessed, warnings));
        } else {
            // Fallback case
            fileImportOccurrence.markFailure(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED
                    .getTranslated(this.context.getThesaurus()));
        }
    }
}
