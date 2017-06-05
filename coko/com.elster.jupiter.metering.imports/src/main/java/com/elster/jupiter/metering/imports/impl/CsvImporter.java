/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.imports.impl;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.fileimport.csvimport.FileImportLogger;
import com.elster.jupiter.fileimport.csvimport.FileImportParser;
import com.elster.jupiter.fileimport.csvimport.FileImportProcessor;
import com.elster.jupiter.fileimport.csvimport.FileImportRecord;
import com.elster.jupiter.fileimport.csvimport.exceptions.FileImportLineException;
import com.elster.jupiter.fileimport.csvimport.exceptions.ProcessorException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

public final class CsvImporter<T extends FileImportRecord> implements FileImporter {

    public static final class Builder<T extends FileImportRecord> {

        private final CsvImporter<T> importer;

        private Builder() {
            this.importer = new CsvImporter<>();
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

        public CsvImporter<T> build() {
            return this.importer;
        }
    }

    private static final char COMMENT_MARKER = '#';

    private char csvDelimiter;

    private FileImportParser<T> parser;
    private FileImportProcessor<T> processor;
    private FileImportLogger<FileImportRecord> logger;

    public static <T extends FileImportRecord> Builder<T> withParser(FileImportParser<T> parser) {
        Builder<T> builder = new Builder<>();
        builder.importer.parser = parser;
        return builder;
    }

    private CsvImporter() {
    }

    @Override
    public void process(FileImportOccurrence fileImportOccurrence) {
        logger.init(fileImportOccurrence);
        try (CSVParser csvParser = getCSVParser(fileImportOccurrence)) {
            parser.init(csvParser);
            for (CSVRecord csvRecord : csvParser) {
                processRecord(csvRecord, fileImportOccurrence);
            }
            logger.importFinished();
        } catch (Exception e) {
            logger.importFailed(e);
        }
    }

    // Parser exceptions should always fail whole importing process
    private void processRecord(CSVRecord csvRecord, FileImportOccurrence fileImportOccurrence) throws FileImportLineException, ProcessorException, SQLException {
        try {
            T data = parser.parse(csvRecord);
            Connection connection = fileImportOccurrence.getCurrentConnection();
            Savepoint savepoint = connection.setSavepoint();
            try {
                processor.process(data, logger);
                logger.importLineFinished(data);
            } catch (Exception exception) {
                connection.rollback(savepoint);
                logger.importLineFailed(data, exception);
            } finally {
                fileImportOccurrence.save();
            }
        } catch (FileImportLineException lineException) {
            logger.importLineFailed(lineException.getLineNumber(), lineException);
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