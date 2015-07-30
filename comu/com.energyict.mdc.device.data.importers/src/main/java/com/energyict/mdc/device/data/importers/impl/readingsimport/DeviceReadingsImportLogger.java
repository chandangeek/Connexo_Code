package com.energyict.mdc.device.data.importers.impl.readingsimport;

import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.FileImportLoggerImpl;
import com.energyict.mdc.device.data.importers.impl.TranslationKeys;

import java.util.HashMap;
import java.util.Map;

public class DeviceReadingsImportLogger extends FileImportLoggerImpl<DeviceReadingsImportRecord> {

    private int warnings = 0;
    private Map<String, Long> readingsAddedSuccessfully = new HashMap<>();
    private Map<String, Long> readingsFailedToImport = new HashMap<>();

    public DeviceReadingsImportLogger(DeviceDataImporterContext context) {
        super(context);
    }

    @Override
    public void importLineFailed(DeviceReadingsImportRecord data, Exception exception) {
        super.importLineFailed(data, exception);
        addReadingsFailed(data.getDeviceMrid(), data.getReadingTypes().size());
    }

    @Override
    public void importLineFinished(DeviceReadingsImportRecord data) {
        super.importLineFinished(data);
        addReadingsSuccess(data.getDeviceMrid(), data.getReadingTypes().size());
    }

    @Override
    public void warning(TranslationKey message, Object... arguments) {
        super.warning(message, arguments);
        warnings++;
    }

    private void addReadingsSuccess(String device, long numberOfSuccessReadings) {
        addReadingsProcessed(readingsAddedSuccessfully, device, numberOfSuccessReadings);
    }

    private void addReadingsFailed(String device, long numberOfFailedReadings) {
        addReadingsProcessed(readingsFailedToImport, device, numberOfFailedReadings);
    }

    private void addReadingsProcessed(Map<String, Long> map, String device, long numberOfReadings) {
        Long currentNumberOfReadings = map.get(device);
        map.put(device, numberOfReadings + (currentNumberOfReadings != null ? currentNumberOfReadings : 0));
    }

    @Override
    protected void summarizeFailedImport() {
        long numberOfFailedDevices = getNumberOfFailedDevices();
        long numberOfSuccessDevices = getNumberOfSuccessDevices();
        long numberOfFailedReadings = getNumberOfFailedReadings();
        long numberOfSuccessReadings = getNumberOfSuccessReadings();

        if (numberOfFailedReadings != 0 && warnings == 0) {
            // Some readings were processed with errors
            fileImportOccurrence.markFailure(TranslationKeys.READINGS_IMPORT_RESULT_FAIL_WITH_ERRORS
                    .getTranslated(context.getThesaurus(), numberOfSuccessReadings, numberOfSuccessDevices, numberOfFailedReadings, numberOfFailedDevices));
        } else if (numberOfFailedReadings != 0 && warnings != 0) {
            // Some readings were processed with errors and warnings
            fileImportOccurrence.markFailure(TranslationKeys.READINGS_IMPORT_RESULT_FAIL_WITH_WARN_AND_ERRORS
                    .getTranslated(this.context.getThesaurus(), numberOfSuccessReadings, numberOfSuccessDevices, warnings, numberOfFailedReadings, numberOfFailedDevices));
        } else if (numberOfFailedReadings == 0 && warnings != 0) {
            // Some readings were processed with warnings
            fileImportOccurrence.markFailure(TranslationKeys.READINGS_IMPORT_RESULT_FAIL_WITH_WARN
                    .getTranslated(this.context.getThesaurus(), numberOfSuccessReadings, numberOfSuccessDevices, warnings));
        } else if (numberOfSuccessReadings != 0 && numberOfFailedReadings == 0 && warnings == 0) {
            // Some readings were processed
            fileImportOccurrence.markFailure(TranslationKeys.READINGS_IMPORT_RESULT_FAIL
                    .getTranslated(this.context.getThesaurus(), numberOfSuccessReadings, numberOfSuccessDevices));
        } else if (numberOfSuccessReadings == 0 && numberOfFailedReadings == 0 && warnings == 0) {
            // No readings were processed (Bad column headers)
            fileImportOccurrence.markFailure(TranslationKeys.READINGS_IMPORT_RESULT_NO_READINGS_WERE_PROCESSED
                    .getTranslated(this.context.getThesaurus()));
        }
    }

    @Override
    protected void summarizeSuccessImport() {
        long numberOfFailedDevices = getNumberOfFailedDevices();
        long numberOfSuccessDevices = getNumberOfSuccessDevices();
        long numberOfFailedReadings = getNumberOfFailedReadings();
        long numberOfSuccessReadings = getNumberOfSuccessReadings();

        if (numberOfSuccessReadings == 0 && numberOfFailedReadings == 0) {
            // No readings were processed (No devices in file)
            fileImportOccurrence.markFailure(TranslationKeys.READINGS_IMPORT_RESULT_NO_READINGS_WERE_PROCESSED
                    .getTranslated(this.context.getThesaurus()));
        } else if (numberOfSuccessReadings != 0 && numberOfFailedReadings == 0 && warnings == 0) {
            // All readings were processed without warnings/errors
            fileImportOccurrence.markSuccess(TranslationKeys.READINGS_IMPORT_RESULT_SUCCESS
                    .getTranslated(this.context.getThesaurus(), numberOfSuccessReadings, numberOfSuccessDevices));
        } else if (numberOfFailedReadings != 0 && warnings == 0) {
            // All readings were processed but some of the devices failed
            fileImportOccurrence.markSuccessWithFailures(TranslationKeys.READINGS_IMPORT_RESULT_SUCCESS_WITH_ERRORS
                    .getTranslated(this.context.getThesaurus(), numberOfSuccessReadings, numberOfSuccessDevices, numberOfFailedReadings, numberOfFailedDevices));
        } else if (numberOfFailedReadings != 0 && warnings != 0) {
            // All readings were processed but part of them were processed with warnings and failures
            fileImportOccurrence.markSuccessWithFailures(TranslationKeys.READINGS_IMPORT_RESULT_SUCCESS_WITH_WARN_AND_ERRORS
                    .getTranslated(this.context.getThesaurus(), numberOfSuccessReadings, numberOfSuccessDevices, warnings, numberOfFailedReadings, numberOfFailedDevices));
        } else if (numberOfFailedReadings == 0 && warnings != 0) {
            // All readings were processed but part of them were processed with warnings
            fileImportOccurrence.markSuccess(TranslationKeys.READINGS_IMPORT_RESULT_SUCCESS_WITH_WARN
                    .getTranslated(this.context.getThesaurus(), numberOfSuccessReadings, numberOfSuccessDevices, warnings));
        } else {
            // Fallback case
            fileImportOccurrence.markFailure(TranslationKeys.READINGS_IMPORT_RESULT_NO_READINGS_WERE_PROCESSED
                    .getTranslated(this.context.getThesaurus()));
        }
    }

    private long getNumberOfSuccessReadings() {
        return readingsAddedSuccessfully.values().stream().mapToLong(i -> i).sum();
    }

    private int getNumberOfSuccessDevices() {
        return readingsAddedSuccessfully.size();
    }

    private long getNumberOfFailedReadings() {
        return readingsFailedToImport.values().stream().mapToLong(i -> i).sum();
    }

    private int getNumberOfFailedDevices() {
        return readingsFailedToImport.size();
    }
}
