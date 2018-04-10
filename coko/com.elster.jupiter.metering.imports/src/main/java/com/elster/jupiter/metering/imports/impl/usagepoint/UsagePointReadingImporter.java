/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.metering.imports.impl.usagepoint;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.metering.imports.impl.parsers.InstantParser;
import com.elster.jupiter.metering.imports.impl.parsers.csv.CsvParserWrapper;
import com.elster.jupiter.metering.imports.impl.parsers.csv.CsvRecordWrapper;
import com.elster.jupiter.metering.imports.impl.parsers.csv.ObjectMapper;
import com.elster.jupiter.metering.imports.impl.parsers.csv.exception.ObjectMapperInitException;
import com.elster.jupiter.metering.imports.impl.parsers.csv.exception.ObjectMapperNotRecoverableException;
import com.elster.jupiter.metering.imports.impl.parsers.csv.exception.ObjectMapperRecovarableException;
import com.elster.jupiter.metering.imports.impl.parsers.csv.fields.FakeCSVField;
import com.elster.jupiter.metering.imports.impl.parsers.csv.fields.InstantCsvField;
import com.elster.jupiter.metering.imports.impl.parsers.csv.fields.KeyValueRepetition;
import com.elster.jupiter.metering.imports.impl.parsers.csv.fields.StringCsvField;
import com.elster.jupiter.metering.imports.impl.properties.SupportedNumberFormat;

import org.apache.commons.csv.CSVRecord;

import java.util.Iterator;
import java.util.logging.Logger;

public class UsagePointReadingImporter implements FileImporter {

    private static final char COMMENT_MARKER = '#';

    private Logger logger;
    private String delimiter;
    private String dateFormat;
    private String timeZone;
    private SupportedNumberFormat numberFormat;


    public UsagePointReadingImporter(String delimiter, String dateFormat, String timeZone, SupportedNumberFormat numberFormat) {
        this.delimiter = delimiter;
        this.dateFormat = dateFormat;
        this.timeZone = timeZone;
        this.numberFormat = numberFormat;
    }

    @Override
    public void process(FileImportOccurrence fileImportOccurrence) {
        int lineErrors = 0;
        int linesSuccess = 0;

        try {
            UsagePointReadingImportProcessor usagePointProcessor = new UsagePointReadingImportProcessor();
            ObjectMapper<UsagePointImportRecordModel> objectMapper = getObjectMapper();
            CsvParserWrapper csvParser = new CsvParserWrapper(fileImportOccurrence.getContents(), delimiter, COMMENT_MARKER);
            Iterator<CSVRecord> recordIterator = csvParser.getCsvParser().iterator();

            while (recordIterator.hasNext()){
                try {
                    CsvRecordWrapper next = new CsvRecordWrapper(recordIterator.next());
                    UsagePointImportRecordModel usagePointRecord = objectMapper.getObject(next);
                    usagePointProcessor.process(usagePointRecord);
                    linesSuccess++;
                } catch (ObjectMapperRecovarableException| UsagePointReadingImportProcessorException e){
                    logger.warning(e.getMessage());
                    lineErrors++;
                }
            }
            markEnd(fileImportOccurrence, linesSuccess, lineErrors, csvParser.getNumberOfLines());
        } catch (ObjectMapperInitException e) {
            // nothing to do: this means we could not continue processing the file.
            fileImportOccurrence.markFailure(e.getMessage());
        } catch (ObjectMapperNotRecoverableException e) {
            fileImportOccurrence.markSuccessWithFailures(e.getMessage() +" success lines:" + linesSuccess + " error lines:" + lineErrors);
        }

    }

    private void markEnd(FileImportOccurrence fileImportOccurrence, int lineSuccess, int lineErrors, int allLines) {
        if (lineSuccess == allLines){
            fileImportOccurrence.markSuccess("All ok");
        }
            fileImportOccurrence.markSuccessWithFailures("Some failures");

    }


    private ObjectMapper<UsagePointImportRecordModel> getObjectMapper() throws ObjectMapperInitException {
        ObjectMapper<UsagePointImportRecordModel> objectMapper = new ObjectMapper<>(UsagePointImportRecordModel::new);
        objectMapper.add(new StringCsvField(UsagePointImportRecordModel.UsagePointImportRecordMapping.USAGE_POINT_NAME.getObjectField(), UsagePointImportRecordModel.UsagePointImportRecordMapping.USAGE_POINT_NAME
                .getCsvHeader(), 0));
        objectMapper.add(new InstantCsvField(UsagePointImportRecordModel.UsagePointImportRecordMapping.READING_DATE.getObjectField(), UsagePointImportRecordModel.UsagePointImportRecordMapping.READING_DATE
                .getCsvHeader(), 1, new InstantParser(dateFormat, timeZone)));
        objectMapper.add(new StringCsvField(UsagePointImportRecordModel.UsagePointImportRecordMapping.PURPOSE.getObjectField(), UsagePointImportRecordModel.UsagePointImportRecordMapping.PURPOSE
                .getCsvHeader(), 2));
        objectMapper.add(new FakeCSVField(UsagePointImportRecordModel.UsagePointImportRecordMapping.RECORD_LINE_NUMBER.getObjectField()));
        objectMapper.add(new KeyValueRepetition(UsagePointImportRecordModel.UsagePointImportRecordMapping.TYPE_AND_VALUE.getObjectField(), 3));
        return objectMapper;
    }


}
