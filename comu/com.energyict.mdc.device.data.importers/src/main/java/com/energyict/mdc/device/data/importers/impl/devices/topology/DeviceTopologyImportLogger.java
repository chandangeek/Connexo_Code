/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.devices.topology;

import com.energyict.mdc.device.data.importers.impl.AbstractPerLineFileImportLogger;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.TranslationKeys;

public class DeviceTopologyImportLogger extends AbstractPerLineFileImportLogger {

    public DeviceTopologyImportLogger(DeviceDataImporterContext context) {
        super(context);
    }

    @Override
    protected void summarizeFailedImport() {
        if (linesProcessed == 0 && linesWithError == 0) {
            // No lines were processed (Bad column headers)
            fileImportOccurrence.markFailure(this.context.getThesaurus().getFormat(TranslationKeys.TOPOLOGY_IMPORT_RESULT_INCORRECT_HEADER).format());
        } else if (linesProcessed == 0 && linesWithError != 0) {
            // All lines were processed with errors
            fileImportOccurrence.markFailure(this.context.getThesaurus().getFormat(TranslationKeys.TOPOLOGY_IMPORT_RESULT_INCOMPLETE_ALL_ERRORS).format(linesProcessed, linesWithError));
        } else if (linesProcessed != 0 && linesWithError != 0) {
            // Some lines were processed with errors
            fileImportOccurrence.markFailure(this.context.getThesaurus()
                    .getFormat(TranslationKeys.TOPOLOGY_IMPORT_RESULT_INCOMPLETE_SOME_ERRORS)
                    .format(linesProcessed, linesWithError));
        } else if (linesProcessed != 0 && linesWithError == 0) {
            // Some lines were processed successfully
            fileImportOccurrence.markFailure(this.context.getThesaurus().getFormat(TranslationKeys.TOPOLOGY_IMPORT_RESULT_INCOMPLETE).format(linesProcessed));
        }
    }

    @Override
    protected void summarizeSuccessImport() {
        if (linesProcessed == 0 && linesWithError == 0) {
            // No lines were processed  (No devices in file)
            fileImportOccurrence.markFailure(this.context.getThesaurus().getFormat(TranslationKeys.TOPOLOGY_IMPORT_RESULT_NO_REQUESTS_PROCESSED).format());
        } else if (linesProcessed == 0 && linesWithError != 0) {
            // All lines were processed with errors
            fileImportOccurrence.markFailure(this.context.getThesaurus().getFormat(TranslationKeys.TOPOLOGY_IMPORT_RESULT_FAIL).format(linesWithError));
        } else if (linesProcessed != 0 && linesWithError != 0) {
            // Some lines were processed with errors
            fileImportOccurrence.markSuccessWithFailures(this.context.getThesaurus()
                    .getFormat(TranslationKeys.TOPOLOGY_IMPORT_RESULT_SUCCESS_WITH_ERRORS)
                    .format(linesProcessed, linesWithError));
        } else if (linesProcessed != 0 && linesWithError == 0) {
            // Some lines were processed successfully
            fileImportOccurrence.markSuccess(this.context.getThesaurus().getFormat(TranslationKeys.TOPOLOGY_IMPORT_RESULT_SUCCESS).format(linesProcessed));
        }
    }
}
