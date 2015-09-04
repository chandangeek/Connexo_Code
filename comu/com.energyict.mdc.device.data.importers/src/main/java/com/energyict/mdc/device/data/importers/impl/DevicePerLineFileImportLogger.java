package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;

public class DevicePerLineFileImportLogger extends FileImportLoggerImpl<FileImportRecord> {

    private int linesWithError = 0;
    private int linesProcessed = 0;
    private int linesWithWarnings = 0;
    private boolean hasWarnings = false;

    public DevicePerLineFileImportLogger(DeviceDataImporterContext context) {
        super(context);
    }

    @Override
    public void warning(MessageSeed message, Object... arguments) {
        super.warning(message, arguments);
        hasWarnings = true;
    }

    @Override
    public void warning(TranslationKey message, Object... arguments) {
        super.warning(message, arguments);
        hasWarnings = true;
    }

    @Override
    public void importLineFinished(FileImportRecord data) {
        super.importLineFinished(data);
        linesProcessed++;
        if (hasWarnings) {
            linesWithWarnings++;
        }
    }

    @Override
    public void importLineFailed(FileImportRecord data, Exception exception) {
        super.importLineFailed(data, exception);
        this.linesWithError++;
    }

    protected void summarizeFailedImport() {
        if (linesWithError != 0 && linesWithWarnings == 0) {
            // Some devices were processed with errors
            fileImportOccurrence.markFailure(this.context.getThesaurus().getFormat(TranslationKeys.IMPORT_RESULT_FAIL_WITH_ERRORS).format(linesProcessed, linesWithError));
        } else if (linesWithError != 0 && linesWithWarnings != 0) {
            // Some devices were processed with errors and warnings
            fileImportOccurrence.markFailure(this.context.getThesaurus().getFormat(TranslationKeys.IMPORT_RESULT_FAIL_WITH_WARN_AND_ERRORS).format(linesProcessed, linesWithWarnings, linesWithError));
        } else if (linesWithError == 0 && linesWithWarnings != 0) {
            // Some devices were processed with warnings
            fileImportOccurrence.markFailure(this.context.getThesaurus().getFormat(TranslationKeys.IMPORT_RESULT_FAIL_WITH_WARN).format(linesProcessed, linesWithWarnings));
        } else if (linesProcessed != 0 && linesWithError == 0 && linesWithWarnings == 0) {
            // Some devices were processed
            fileImportOccurrence.markFailure(this.context.getThesaurus().getFormat(TranslationKeys.IMPORT_RESULT_FAIL).format(linesProcessed));
        } else if (linesProcessed == 0 && linesWithError == 0 && linesWithWarnings == 0) {
            // No devices were processed (Bad column headers)
            fileImportOccurrence.markFailure(this.context.getThesaurus().getFormat(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED).format());
        }
    }

    protected void summarizeSuccessImport() {
        if (linesProcessed == 0 && linesWithError == 0) {
            // No devices were processed (No devices in file)
            fileImportOccurrence.markFailure(this.context.getThesaurus().getFormat(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED).format());
        } else if (linesProcessed != 0 && linesWithError == 0 && linesWithWarnings == 0) {
            // All devices were processed without warnings/errors
            fileImportOccurrence.markSuccess(this.context.getThesaurus().getFormat(TranslationKeys.IMPORT_RESULT_SUCCESS).format(linesProcessed));
        } else if (linesWithError != 0 && linesWithWarnings == 0) {
            // All devices were processed but some of the devices failed
            fileImportOccurrence.markSuccessWithFailures(this.context.getThesaurus().getFormat(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_ERRORS).format(linesProcessed, linesWithError));
        } else if (linesWithError != 0 && linesWithWarnings != 0) {
            // All devices were processed but part of them were processed with warnings and failures
            fileImportOccurrence.markSuccessWithFailures(this.context.getThesaurus()
                    .getFormat(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_WARN_AND_ERRORS)
                    .format(linesProcessed, linesWithWarnings, linesWithError));
        } else if (linesWithError == 0 && linesWithWarnings != 0) {
            // All devices were processed but part of them were processed with warnings
            fileImportOccurrence.markSuccess(this.context.getThesaurus().getFormat(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_WARN).format(linesProcessed, linesWithWarnings));
        } else {
            // Fallback case
            fileImportOccurrence.markFailure(this.context.getThesaurus().getFormat(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED).format());
        }
    }
}
