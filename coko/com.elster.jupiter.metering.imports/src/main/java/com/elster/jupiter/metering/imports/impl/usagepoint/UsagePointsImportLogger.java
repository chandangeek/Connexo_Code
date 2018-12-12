/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.imports.impl.usagepoint;

import com.elster.jupiter.metering.imports.impl.FileImportLoggerImpl;
import com.elster.jupiter.fileimport.csvimport.FileImportRecord;
import com.elster.jupiter.metering.imports.impl.MeteringDataImporterContext;
import com.elster.jupiter.metering.imports.impl.TranslationKeys;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;


public class UsagePointsImportLogger extends FileImportLoggerImpl<FileImportRecord> {

    private int linesWithError = 0;
    private int linesProcessed = 0;
    private int linesWithWarnings = 0;
    private boolean hasWarnings = false;

    public UsagePointsImportLogger(MeteringDataImporterContext context) {
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
        super.importLineFailed(data.getLineNumber(), exception);
        this.linesWithError++;
    }

    @Override
    public void importLineFailed(long lineNumber, Exception exception) {
        super.importLineFailed(lineNumber, exception);
        this.linesWithError++;
    }

    protected void summarizeFailedImport() {
        if (linesWithError != 0 && linesWithWarnings == 0) {
            // Some data were processed with errors
            fileImportOccurrence.markFailure(this.context.getThesaurus()
                    .getFormat(TranslationKeys.Labels.IMPORT_RESULT_FAIL_WITH_ERRORS)
                    .format(linesProcessed, linesWithError));
        } else if (linesWithError != 0 && linesWithWarnings != 0) {
            // Some data were processed with errors and warnings
            fileImportOccurrence.markFailure(this.context.getThesaurus()
                    .getFormat(TranslationKeys.Labels.IMPORT_RESULT_FAIL_WITH_WARN_AND_ERRORS)
                    .format(linesProcessed, linesWithWarnings, linesWithError));
        } else if (linesWithError == 0 && linesWithWarnings != 0) {
            // Some data were processed with warnings
            fileImportOccurrence.markFailure(this.context.getThesaurus()
                    .getFormat(TranslationKeys.Labels.IMPORT_RESULT_FAIL_WITH_WARN)
                    .format(linesProcessed, linesWithWarnings));
        } else if (linesProcessed != 0 && linesWithError == 0 && linesWithWarnings == 0) {
            // Some data were processed
            fileImportOccurrence.markFailure(this.context.getThesaurus()
                    .getFormat(TranslationKeys.Labels.IMPORT_RESULT_FAIL)
                    .format(linesProcessed));
        } else if (linesProcessed == 0 && linesWithError == 0 && linesWithWarnings == 0) {
            // No data were processed (Bad column headers)
            fileImportOccurrence.markFailure(this.context.getThesaurus()
                    .getFormat(TranslationKeys.Labels.IMPORT_RESULT_NO_USAGEPOINTS_WERE_PROCESSED)
                    .format());
        }
    }

    protected void summarizeSuccessImport() {
        if (linesProcessed == 0) {
            // No data were processed (No data in file)
            fileImportOccurrence.markFailure(this.context.getThesaurus()
                    .getFormat(TranslationKeys.Labels.IMPORT_RESULT_NO_USAGEPOINTS_WERE_PROCESSED)
                    .format());
        } else if (linesProcessed != 0 && linesWithError == 0 && linesWithWarnings == 0) {
            // All data were processed without warnings/errors
            fileImportOccurrence.markSuccess(this.context.getThesaurus()
                    .getFormat(TranslationKeys.Labels.IMPORT_RESULT_SUCCESS)
                    .format(linesProcessed));
        } else if (linesWithError != 0 && linesWithWarnings == 0) {
            // All data were processed but some of the data failed
            fileImportOccurrence.markSuccessWithFailures(this.context.getThesaurus()
                    .getFormat(TranslationKeys.Labels.IMPORT_RESULT_SUCCESS_WITH_ERRORS)
                    .format(linesProcessed, linesWithError));
        } else if (linesWithError != 0 && linesWithWarnings != 0) {
            // All data were processed but part of them were processed with warnings and failures
            fileImportOccurrence.markSuccessWithFailures(this.context.getThesaurus()
                    .getFormat(TranslationKeys.Labels.IMPORT_RESULT_SUCCESS_WITH_WARN_AND_ERRORS)
                    .format(linesProcessed, linesWithWarnings, linesWithError));
        } else if (linesWithError == 0 && linesWithWarnings != 0) {
            // All data were processed but part of them were processed with warnings
            fileImportOccurrence.markSuccess(this.context.getThesaurus()
                    .getFormat(TranslationKeys.Labels.IMPORT_RESULT_SUCCESS_WITH_WARN)
                    .format(linesProcessed, linesWithWarnings));
        } else {
            // Fallback case
            fileImportOccurrence.markFailure(this.context.getThesaurus()
                    .getFormat(TranslationKeys.Labels.IMPORT_RESULT_NO_USAGEPOINTS_WERE_PROCESSED)
                    .format());
        }
    }
}
