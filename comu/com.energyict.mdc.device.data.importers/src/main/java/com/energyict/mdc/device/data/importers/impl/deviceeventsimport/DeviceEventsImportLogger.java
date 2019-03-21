package com.energyict.mdc.device.data.importers.impl.deviceeventsimport;

import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.FileImportLoggerImpl;
import com.energyict.mdc.device.data.importers.impl.TranslationKeys;

import java.util.HashMap;
import java.util.Map;

public class DeviceEventsImportLogger extends FileImportLoggerImpl<DeviceEventsImportRecord> {
    public DeviceEventsImportLogger(DeviceDataImporterContext context) {
        super(context);
    }
    private int warnings = 0;
    private Map<String, Long> eventsAddedSuccessfully = new HashMap<>();
    private Map<String, Long> eventsFailedToImport = new HashMap<>();

    @Override
    protected void summarizeFailedImport() {
        long numberOfFailedDevices = getNumberOfFailedDevices();
        long numberOfSuccessDevices = getNumberOfSuccessDevices();
        long numberOfFailedReadings = getNumberOfFailedEvents();
        long numberOfSuccessReadings = getNumberOfSuccessEvents();

        if (numberOfFailedReadings != 0 && warnings == 0) {
            fileImportOccurrence.markFailure(context.getThesaurus()
                    .getFormat(TranslationKeys.DEVICE_EVENTS_IMPORT_RESULT_FAIL_WITH_ERRORS)
                    .format(numberOfSuccessReadings, numberOfSuccessDevices, numberOfFailedReadings, numberOfFailedDevices));
        } else if (numberOfFailedReadings != 0 && warnings != 0) {
            fileImportOccurrence.markFailure(this.context.getThesaurus()
                    .getFormat(TranslationKeys.DEVICE_EVENTS_IMPORT_RESULT_FAIL_WITH_WARN_AND_ERRORS)
                    .format(numberOfSuccessReadings, numberOfSuccessDevices, warnings, numberOfFailedReadings, numberOfFailedDevices));
        } else if (numberOfFailedReadings == 0 && warnings != 0) {
            fileImportOccurrence.markFailure(this.context.getThesaurus()
                    .getFormat(TranslationKeys.DEVICE_EVENTS_IMPORT_RESULT_FAIL_WITH_WARN)
                    .format(numberOfSuccessReadings, numberOfSuccessDevices, warnings));
        } else if (numberOfSuccessReadings != 0 && numberOfFailedReadings == 0 && warnings == 0) {
            fileImportOccurrence.markFailure(this.context.getThesaurus().getFormat(TranslationKeys.DEVICE_EVENTS_IMPORT_RESULT_FAIL).format(numberOfSuccessReadings, numberOfSuccessDevices));
        } else if (numberOfSuccessReadings == 0 && numberOfFailedReadings == 0 && warnings == 0) {
            fileImportOccurrence.markFailure(this.context.getThesaurus().getFormat(TranslationKeys.DEVICE_EVENTS_IMPORT_RESULT_NO_EVENTS_WERE_PROCESSED).format());
        }
    }

    @Override
    protected void summarizeSuccessImport() {
        long numberOfFailedDevices = getNumberOfFailedDevices();
        long numberOfSuccessDevices = getNumberOfSuccessDevices();
        long numberOfFailedEvents = getNumberOfFailedEvents();
        long numberOfSuccessEvents = getNumberOfSuccessEvents();

        if (numberOfSuccessEvents == 0) {
            fileImportOccurrence.markFailure(this.context.getThesaurus().getFormat(TranslationKeys.DEVICE_EVENTS_IMPORT_RESULT_NO_EVENTS_WERE_PROCESSED).format());
        } else if (numberOfSuccessEvents != 0 && numberOfFailedEvents == 0 && warnings == 0) {
            fileImportOccurrence.markSuccess(this.context.getThesaurus().getFormat(TranslationKeys.DEVICE_EVENTS_IMPORT_RESULT_SUCCESS).format(numberOfSuccessEvents, numberOfSuccessDevices));
        } else if (numberOfFailedEvents != 0 && warnings == 0) {
            fileImportOccurrence.markSuccessWithFailures(this.context.getThesaurus()
                    .getFormat(TranslationKeys.DEVICE_EVENTS_IMPORT_RESULT_SUCCESS_WITH_ERRORS)
                    .format(numberOfSuccessEvents, numberOfSuccessDevices, numberOfFailedEvents, numberOfFailedDevices));
        } else if (numberOfFailedEvents != 0 && warnings != 0) {
            fileImportOccurrence.markSuccessWithFailures(this.context.getThesaurus()
                    .getFormat(TranslationKeys.DEVICE_EVENTS_IMPORT_RESULT_SUCCESS_WITH_WARN_AND_ERRORS)
                    .format(numberOfSuccessEvents, numberOfSuccessDevices, warnings, numberOfFailedEvents, numberOfFailedDevices));
        } else if (numberOfFailedEvents == 0 && warnings != 0) {
            fileImportOccurrence.markSuccess(this.context.getThesaurus()
                    .getFormat(TranslationKeys.DEVICE_EVENTS_IMPORT_RESULT_SUCCESS_WITH_WARN)
                    .format(numberOfSuccessEvents, numberOfSuccessDevices, warnings));
        } else {
            fileImportOccurrence.markFailure(this.context.getThesaurus().getFormat(TranslationKeys.DEVICE_EVENTS_IMPORT_RESULT_NO_EVENTS_WERE_PROCESSED).format());
        }
    }
    @Override
    public void warning(MessageSeed message, Object... arguments) {
        super.warning(message, arguments);
        warnings++;
    }

    @Override
    public void warning(TranslationKey message, Object... arguments) {
        super.warning(message, arguments);
        warnings++;
    }

    @Override
    public void importLineFailed(DeviceEventsImportRecord data, Exception exception) {
        super.importLineFailed(data, exception);
        String device = data.getDeviceIdentifier();
        Long currentNumberOfEvents = eventsFailedToImport.get(device);
        eventsFailedToImport.put(device, currentNumberOfEvents++);
    }

    @Override
    public void importLineFinished(DeviceEventsImportRecord data) {
        super.importLineFinished(data);
        String device = data.getDeviceIdentifier();
        Long currentNumberOfEvents = eventsAddedSuccessfully.get(device);
        eventsAddedSuccessfully.put(device, currentNumberOfEvents++);
    }

    private int getNumberOfSuccessEvents() {
        return eventsAddedSuccessfully.size();
    }

    private long getNumberOfSuccessDevices() {
        return eventsAddedSuccessfully.values().stream().mapToLong(i -> i).sum();
    }

    private long getNumberOfFailedEvents() {
        return eventsFailedToImport.values().stream().mapToLong(i -> i).sum();
    }

    private int getNumberOfFailedDevices() {
        return eventsFailedToImport.size();
    }
}
