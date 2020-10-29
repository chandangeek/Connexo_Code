package com.energyict.mdc.device.data.importers.impl.certificatesimport;

import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.FileImportZipEntry;
import com.energyict.mdc.device.data.importers.impl.FileImportZipLoggerImpl;
import com.energyict.mdc.device.data.importers.impl.TranslationKeys;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;

import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipFile;

public class DeviceCertificatesImportLogger extends FileImportZipLoggerImpl {

    private int warnings = 0;
    private Map<String, Long> deviceCertificatesAddedSuccessfully = new HashMap<>();
    private Map<String, Long> deviceCertificatesFailedToImport = new HashMap<>();

    public DeviceCertificatesImportLogger(DeviceDataImporterContext context) {
        super(context);
    }

    @Override
    public void info(MessageSeeds message, Object... arguments) {
        super.info(message, arguments);
    }

    @Override
    public void warning(MessageSeed message, Object... arguments) {
        super.warning(message, arguments);
        warnings++;
    }

    @Override
    public void importZipEntryFailed(ZipFile zipFile, FileImportZipEntry data, Exception exception) {
        super.importZipEntryFailed(zipFile, data, exception);
        addDeviceCertificatesFailed(data.getDirectory());
    }

    @Override
    public void importZipEntryFinished(ZipFile zipFile, FileImportZipEntry data) {
        super.importZipEntryFinished(zipFile, data);
        addDeviceCertificatesSuccess(data.getDirectory());
    }

    private void addDeviceCertificatesSuccess(String device) {
        addDeviceCertificatesProcessed(deviceCertificatesAddedSuccessfully, device);
    }

    private void addDeviceCertificatesFailed(String device) {
        addDeviceCertificatesProcessed(deviceCertificatesFailedToImport, device);
    }

    private void addDeviceCertificatesProcessed(Map<String, Long> map, String device) {
        Long currentNumberOfDeviceCertificates = map.get(device);
        map.put(device, 1L + (currentNumberOfDeviceCertificates != null ? currentNumberOfDeviceCertificates : 0));
    }

    @Override
    protected void summarizeFailedImport() {
        long numberOfFailedDevices = getNumberOfFailedDevices();
        long numberOfSuccessDevices = getNumberOfSuccessDevices();
        long numberOfFailedCertificates = getNumberOfFailedCertificates();
        long numberOfSuccessCertificates = getNumberOfSuccessCertificates();

        //TODO remove surplus after testing : error when certifate can't be extracted , otherwise warnings that can be skipped

        if (numberOfFailedCertificates != 0 && warnings == 0) {
            // Some certificates were processed with errors
            fileImportOccurrence.markFailure(context.getThesaurus()
                    .getFormat(TranslationKeys.CERTIFICATES_IMPORT_RESULT_FAIL_WITH_ERRORS)
                    .format(numberOfSuccessCertificates, numberOfSuccessDevices, numberOfFailedCertificates, numberOfFailedDevices));
        } else if (numberOfFailedCertificates != 0 && warnings != 0) {
            // Some certificates were processed with errors and warnings
            fileImportOccurrence.markFailure(this.context.getThesaurus()
                    .getFormat(TranslationKeys.CERTIFICATES_IMPORT_RESULT_FAIL_WITH_WARN_AND_ERRORS)
                    .format(numberOfSuccessCertificates, numberOfSuccessDevices, warnings, numberOfFailedCertificates, numberOfFailedDevices));
        } else if (numberOfFailedCertificates == 0 && warnings != 0) {
            // Some certificates were processed with warnings
            fileImportOccurrence.markFailure(this.context.getThesaurus()
                    .getFormat(TranslationKeys.CERTIFICATES_IMPORT_RESULT_FAIL_WITH_WARN)
                    .format(numberOfSuccessCertificates, numberOfSuccessDevices, warnings));
        } else if (numberOfSuccessCertificates != 0 && numberOfFailedCertificates == 0 && warnings == 0) {
            // Some certificates were processed
            fileImportOccurrence.markFailure(this.context.getThesaurus().getFormat(TranslationKeys.CERTIFICATES_IMPORT_RESULT_FAIL).format(numberOfSuccessCertificates, numberOfSuccessDevices));
        } else if (numberOfSuccessCertificates == 0 && numberOfFailedCertificates == 0 && warnings == 0) {
            // No certificates were processed (Bad column headers)
            fileImportOccurrence.markFailure(this.context.getThesaurus().getFormat(TranslationKeys.CERTIFICATES_IMPORT_RESULT_NO_CERTIFICATES_WERE_PROCESSED).format());
        }
    }

    @Override
    protected void summarizeSuccessImport() {
        long numberOfFailedDevices = getNumberOfFailedDevices();
        long numberOfSuccessDevices = getNumberOfSuccessDevices();
        long numberOfFailedCertificates = getNumberOfFailedCertificates();
        long numberOfSuccessCertificates = getNumberOfSuccessCertificates();

        if (numberOfSuccessCertificates == 0) {
            // No certificates were processed (No devices in file)
            fileImportOccurrence.markFailure(this.context.getThesaurus().getFormat(TranslationKeys.CERTIFICATES_IMPORT_RESULT_NO_CERTIFICATES_WERE_PROCESSED).format());
        } else if (numberOfSuccessCertificates != 0 && numberOfFailedCertificates == 0 && warnings == 0) {
            // All certificates were processed without warnings/errors
            fileImportOccurrence.markSuccess(this.context.getThesaurus().getFormat(TranslationKeys.CERTIFICATES_IMPORT_RESULT_SUCCESS).format(numberOfSuccessCertificates, numberOfSuccessDevices));
        } else if (numberOfFailedCertificates != 0 && warnings == 0) {
            // All certificates were processed but some of the devices failed
            fileImportOccurrence.markSuccessWithFailures(this.context.getThesaurus()
                    .getFormat(TranslationKeys.CERTIFICATES_IMPORT_RESULT_SUCCESS_WITH_ERRORS)
                    .format(numberOfSuccessCertificates, numberOfSuccessDevices, numberOfFailedCertificates, numberOfFailedDevices));
        } else if (numberOfFailedCertificates != 0 && warnings != 0) {
            // All certificates were processed but part of them were processed with warnings and failures
            fileImportOccurrence.markSuccessWithFailures(this.context.getThesaurus()
                    .getFormat(TranslationKeys.CERTIFICATES_IMPORT_RESULT_SUCCESS_WITH_WARN_AND_ERRORS)
                    .format(numberOfSuccessCertificates, numberOfSuccessDevices, warnings, numberOfFailedCertificates, numberOfFailedDevices));
        } else if (numberOfFailedCertificates == 0 && warnings != 0) {
            // All certificates were processed but part of them were processed with warnings
            fileImportOccurrence.markSuccess(this.context.getThesaurus()
                    .getFormat(TranslationKeys.CERTIFICATES_IMPORT_RESULT_SUCCESS_WITH_WARN)
                    .format(numberOfSuccessCertificates, numberOfSuccessDevices, warnings));
        } else {
            // Fallback case
            fileImportOccurrence.markFailure(this.context.getThesaurus().getFormat(TranslationKeys.CERTIFICATES_IMPORT_RESULT_NO_CERTIFICATES_WERE_PROCESSED).format());
        }
    }

    private long getNumberOfSuccessCertificates() {
        return deviceCertificatesAddedSuccessfully.values().stream().mapToLong(i -> i).sum();
    }

    private int getNumberOfSuccessDevices() {
        return deviceCertificatesAddedSuccessfully.size();
    }

    private long getNumberOfFailedCertificates() {
        return deviceCertificatesFailedToImport.values().stream().mapToLong(i -> i).sum();
    }

    private int getNumberOfFailedDevices() {
        return deviceCertificatesFailedToImport.size();
    }
}