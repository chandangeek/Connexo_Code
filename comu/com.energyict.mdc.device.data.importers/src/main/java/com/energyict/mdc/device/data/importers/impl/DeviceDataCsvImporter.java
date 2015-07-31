package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;
import com.energyict.mdc.device.data.importers.impl.exceptions.FileImportParserException;
import com.energyict.mdc.device.data.importers.impl.exceptions.ProcessorException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.InputStreamReader;

public class DeviceDataCsvImporter<T extends FileImportRecord> implements FileImporter {

    public static class Builder<T extends FileImportRecord> {

        private final DeviceDataCsvImporter<T> importer;

        private Builder() {
            this.importer = new DeviceDataCsvImporter<>();
        }

        public Builder<T> withProcessor(FileImportProcessor<T> processor) {
            this.importer.processor = processor;
            return this;
        }

        public Builder<T> withLogger(FileImportLogger<FileImportRecord> logger) {
            this.importer.logger = logger;
            return this;
        }

        public Builder<T> withDelimiter(char delimiter) {
            this.importer.csvDelimiter = delimiter;
            return this;
        }

        public DeviceDataCsvImporter<T> build() {
            return this.importer;
        }
    }

    public static final char COMMENT_MARKER = '#';

    private char csvDelimiter;

    private FileImportParser<T> parser;
    private FileImportProcessor<T> processor;
    private FileImportLogger<FileImportRecord> logger;

    public static <T extends FileImportRecord> Builder<T> withParser(FileImportParser<T> parser) {
        Builder<T> builder = new Builder<>();
        builder.importer.parser = parser;
        return builder;
    }

    private DeviceDataCsvImporter() {
    }

    @Override
    public void process(FileImportOccurrence fileImportOccurrence) {
        logger.init(fileImportOccurrence);
        try (CSVParser csvParser = getCSVParser(fileImportOccurrence)) {
            parser.init(csvParser);
            for (CSVRecord csvRecord : csvParser) {
                processRecord(csvRecord);
            }
            logger.importFinished();
        } catch (Exception e) {
            logger.importFailed(e);
        }
    }

    // Parser exceptions should always fail whole importing process
    private void processRecord(CSVRecord csvRecord) throws FileImportParserException, ProcessorException {
        T data = parser.parse(csvRecord);
        try {
            processor.process(data, logger);
            logger.importLineFinished(data);
        } catch (ProcessorException exception) {
            if (exception.shouldStopImport()) {
                throw exception;
            }
            logger.importLineFailed(data, exception);
        } catch (Exception exception) {
            logger.importLineFailed(data, exception);
        }
    }

    private CSVParser getCSVParser(FileImportOccurrence fileImportOccurrence) throws IOException {
        CSVFormat csvFormat = CSVFormat.DEFAULT
                .withHeader()
                .withAllowMissingColumnNames(true)
                .withIgnoreSurroundingSpaces(true)
                .withDelimiter(csvDelimiter)
                .withCommentMarker(COMMENT_MARKER);
        return new CSVParser(new InputStreamReader(fileImportOccurrence.getContents()), csvFormat);
    }
}