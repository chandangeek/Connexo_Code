package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;
import com.energyict.mdc.device.data.importers.impl.exceptions.FileImportParserException;
import com.energyict.mdc.device.data.importers.impl.exceptions.ImportException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.InputStreamReader;

public class DeviceDataCsvImporter<T extends FileImportRecord> implements FileImporter {

    public static class Builder<T extends FileImportRecord> {
        private DeviceDataCsvImporter<T> importer;

        private Builder() {
            this.importer = new DeviceDataCsvImporter<>();
        }

        public Builder<T> withProcessor(FileImportProcessor<T> processor) {
            this.importer.processor = processor;
            return this;
        }

        public Builder<T> withDelimiter(char delimiter) {
            this.importer.csvDelimiter = delimiter;
            return this;
        }

        public DeviceDataCsvImporter<T> build(DeviceDataImporterContext context) {
            this.importer.context = context;
            return this.importer;
        }
    }

    public static final char COMMENT_MARKER = '#';

    private DeviceDataImporterContext context;
    private char csvDelimiter;
    private FileImportParser<T> parser;
    private FileImportProcessor<T> processor;

    private int linesWithError = 0;
    private int linesProcessed = 0;
    private int linesWithWarning = 0;

    public static <T extends FileImportRecord> Builder<T> withParser(FileImportParser<T> parser) {
        Builder<T> builder = new Builder<>();
        builder.importer.parser = parser;
        return builder;
    }

    private DeviceDataCsvImporter() {
    }

    @Override
    public void process(FileImportOccurrence fileImportOccurrence) {
        try (CSVParser csvParser = getCSVParser(fileImportOccurrence)) {
            for (CSVRecord csvRecord : csvParser) {
                processRecord(fileImportOccurrence, csvRecord);
            }
            finishProcess(fileImportOccurrence);
        } catch (Exception e) {
            failImportProcess(fileImportOccurrence, e);
        }
    }

    // Parser exceptions should always fail whole importing process
    private void processRecord(FileImportOccurrence fileImportOccurrence, CSVRecord csvRecord) throws FileImportParserException {
        T data = parser.parse(csvRecord);
        FileImportRecordContext recordContext = new FileImportRecordContext(context.getThesaurus(), fileImportOccurrence.getLogger());
        try {
            processor.process(data, recordContext);
            linesProcessed++;
            if (recordContext.hasWarnings()){
                linesWithWarning++;
            }
        } catch (Exception e) {
            failRecordProcess(fileImportOccurrence, e, data);
        }
    }

    private CSVParser getCSVParser(FileImportOccurrence fileImportOccurrence) throws IOException {
        CSVFormat csvFormat =  CSVFormat.DEFAULT.withHeader().withIgnoreSurroundingSpaces(true).withDelimiter(csvDelimiter).withCommentMarker(COMMENT_MARKER);
        return new CSVParser(new InputStreamReader(fileImportOccurrence.getContents()), csvFormat);
    }

    private void failImportProcess(FileImportOccurrence importOccurrence, Exception exception) {
        String message = exception.getLocalizedMessage();
        if (exception instanceof ImportException) {
            message = ((ImportException) exception).getLocalizedMessage(this.context.getThesaurus());
        }
        importOccurrence.getLogger().severe(message);
        if (linesWithError != 0 && linesWithWarning == 0) {
            // Some devices were processed with errors
            importOccurrence.markFailure(TranslationKeys.IMPORT_RESULT_FAIL_WITH_ERRORS
                    .getTranslated(this.context.getThesaurus(), linesProcessed, linesWithError));
        } else if (linesWithError != 0 && linesWithWarning != 0){
            // Some devices were processed with errors and warnings
            importOccurrence.markFailure(TranslationKeys.IMPORT_RESULT_FAIL_WITH_WARN_AND_ERRORS
                    .getTranslated(this.context.getThesaurus(), linesProcessed, linesWithWarning, linesWithError));
        } else if (linesWithError == 0 && linesWithWarning != 0){
            // Some devices were processed with warnings
            importOccurrence.markFailure(TranslationKeys.IMPORT_RESULT_FAIL_WITH_WARN
                    .getTranslated(this.context.getThesaurus(), linesProcessed, linesWithWarning));
        } else if (linesProcessed != 0 && linesWithError ==0 && linesWithWarning == 0){
            // Some devices were processed
            importOccurrence.markFailure(TranslationKeys.IMPORT_RESULT_FAIL
                    .getTranslated(this.context.getThesaurus(), linesProcessed));
        } else if (linesProcessed == 0 && linesWithError == 0 && linesWithWarning == 0){
            // No devices were processed (Bad column headers)
            importOccurrence.markFailure(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED
                    .getTranslated(this.context.getThesaurus()));
        }
    }

    private void failRecordProcess(FileImportOccurrence importOccurrence, Exception exception, T data) {
        String message = null;
        if (exception instanceof ImportException) {
            message = ((ImportException) exception).getLocalizedMessage(this.context.getThesaurus());
        } else {
            // Always specify line number and device mrid
            message = TranslationKeys.IMPORT_DEFAULT_PROCESSOR_ERROR_TEMPLATE.getTranslated(this.context.getThesaurus(),
                    data.getLineNumber(), data.getDeviceMrid(), exception.getLocalizedMessage());
        }
        importOccurrence.getLogger().warning(message);
        this.linesWithError++;
    }

    private void finishProcess(FileImportOccurrence importOccurrence) {
        if (linesProcessed == 0 && linesWithError == 0){
            // No devices were processed (No devices in file)
            importOccurrence.markFailure(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED
                    .getTranslated(this.context.getThesaurus()));
        } else if (linesProcessed != 0 && linesWithError == 0 && linesWithWarning == 0){
            // All devices were processed without warnings/errors
            importOccurrence.markSuccess(TranslationKeys.IMPORT_RESULT_SUCCESS
                    .getTranslated(this.context.getThesaurus(), linesProcessed));
        } else if (linesWithError != 0 && linesWithWarning == 0){
            // All devices were processed but some of the devices failed
            importOccurrence.markSuccessWithFailures(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_ERRORS
                    .getTranslated(this.context.getThesaurus(), linesProcessed, linesWithError));
        } else if (linesWithError != 0 && linesWithWarning != 0){
            // All devices were processed but part of them were processed with warnings and failures
            importOccurrence.markSuccessWithFailures(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_WARN_AND_ERRORS
                    .getTranslated(this.context.getThesaurus(), linesProcessed, linesWithWarning, linesWithError));
        } else if (linesWithError == 0 && linesWithWarning != 0){
            // All devices were processed but part of them were processed with warnings
            importOccurrence.markSuccess(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_WARN
                    .getTranslated(this.context.getThesaurus(), linesProcessed, linesWithWarning));
        } else {
            // Fallback case
            importOccurrence.markFailure(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED
                    .getTranslated(this.context.getThesaurus()));
        }
    }
}