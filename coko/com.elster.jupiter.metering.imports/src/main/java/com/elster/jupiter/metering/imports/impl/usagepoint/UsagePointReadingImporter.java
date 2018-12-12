/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.metering.imports.impl.usagepoint;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporter;
import com.elster.jupiter.metering.aggregation.DataAggregationService;
import com.elster.jupiter.metering.imports.impl.MeteringDataImporterContext;
import com.elster.jupiter.metering.imports.impl.parsers.BigDecimalParser;
import com.elster.jupiter.metering.imports.impl.parsers.InstantParser;
import com.elster.jupiter.metering.imports.impl.parsers.csv.CsvParserWrapper;
import com.elster.jupiter.metering.imports.impl.parsers.csv.CsvRecordWrapper;
import com.elster.jupiter.metering.imports.impl.parsers.csv.ObjectMapper;
import com.elster.jupiter.metering.imports.impl.parsers.csv.exception.ObjectMapperInitException;
import com.elster.jupiter.metering.imports.impl.parsers.csv.exception.ObjectMapperNotRecoverableException;
import com.elster.jupiter.metering.imports.impl.parsers.csv.exception.ObjectMapperRecovarableException;
import com.elster.jupiter.metering.imports.impl.parsers.csv.fields.FakeCSVField;
import com.elster.jupiter.metering.imports.impl.parsers.csv.fields.InstantCsvField;
import com.elster.jupiter.metering.imports.impl.parsers.csv.fields.KeyStringValueBigDecimalRepetition;
import com.elster.jupiter.metering.imports.impl.parsers.csv.fields.StringCsvField;
import com.elster.jupiter.metering.imports.impl.properties.SupportedNumberFormat;
import com.elster.jupiter.metering.imports.impl.properties.UsagePointReadingImportProperties;

import org.apache.commons.csv.CSVRecord;

import java.util.Iterator;
import java.util.logging.Logger;

import static com.elster.jupiter.metering.imports.impl.TranslationKeys.Labels.UP_READING_IMPORT_RESULT_FAILED;
import static com.elster.jupiter.metering.imports.impl.TranslationKeys.Labels.UP_READING_IMPORT_RESULT_FAILED_INIT_ERROR;
import static com.elster.jupiter.metering.imports.impl.TranslationKeys.Labels.UP_READING_IMPORT_RESULT_FAIL_WITH_ERRORS;
import static com.elster.jupiter.metering.imports.impl.TranslationKeys.Labels.UP_READING_IMPORT_RESULT_SUCCESS;

public class UsagePointReadingImporter implements FileImporter {

    private static final char COMMENT_MARKER = '#';
    private final MeteringDataImporterContext context;
    private final DataAggregationService dataAggregationService;

    private Logger logger;
    private String delimiter;
    private String dateFormat;
    private String timeZone;
    private SupportedNumberFormat numberFormat;


    public UsagePointReadingImporter(UsagePointReadingImportProperties usagePointReadingImportProperties) {
        this.context = usagePointReadingImportProperties.getContext();
        this.dataAggregationService = usagePointReadingImportProperties.getDataAggregationService();
        this.delimiter = usagePointReadingImportProperties.getDelimiter();
        this.dateFormat = usagePointReadingImportProperties.getDateFormat();
        this.timeZone = usagePointReadingImportProperties.getTimeZone();
        this.numberFormat = usagePointReadingImportProperties.getNumberFormat();
    }

    @Override
    public void process(FileImportOccurrence fileImportOccurrence) {
        logger = fileImportOccurrence.getLogger();
        int lineErrors = 0;
        int linesSuccess = 0;
        int numberOfTotalLines = 0;
        try {
            UsagePointReadingImportProcessor usagePointProcessor = new UsagePointReadingImportProcessor(context, dataAggregationService, dateFormat);
            ObjectMapper<UsagePointImportRecordModel> objectMapper = getObjectMapper();
            CsvParserWrapper csvParser = new CsvParserWrapper(fileImportOccurrence.getContents(), delimiter, COMMENT_MARKER);
            Iterator<CSVRecord> recordIterator = csvParser.getCsvParser().iterator();
            while (recordIterator.hasNext()) {
                numberOfTotalLines++;
                try {
                    CsvRecordWrapper next = new CsvRecordWrapper(recordIterator.next());
                    UsagePointImportRecordModel usagePointRecord = objectMapper.getObject(next);
                    usagePointProcessor.process(usagePointRecord);
                    linesSuccess++;
                } catch (ObjectMapperRecovarableException | UsagePointReadingImportProcessorException e) {
                    logger.warning(e.getMessage());
                    lineErrors++;
                }
            }
            markEnd(fileImportOccurrence, linesSuccess, lineErrors, numberOfTotalLines);
        } catch (ObjectMapperInitException e) {
            logger.severe(e.getMessage());
            fileImportOccurrence.markFailure(context.getThesaurus().getFormat(UP_READING_IMPORT_RESULT_FAILED_INIT_ERROR).format(e.getMessage()));
        } catch (ObjectMapperNotRecoverableException e) {
            fileImportOccurrence.markSuccessWithFailures(context.getThesaurus().getFormat(UP_READING_IMPORT_RESULT_FAIL_WITH_ERRORS).format(linesSuccess, lineErrors));

        }

    }

    private void markEnd(FileImportOccurrence fileImportOccurrence, int lineSuccess, int lineErrors, int allLines) {
        if (lineSuccess == allLines) {
            fileImportOccurrence.markSuccess(context.getThesaurus().getFormat(UP_READING_IMPORT_RESULT_SUCCESS).format(lineSuccess));
        } else if (lineErrors == allLines) {
            fileImportOccurrence.markFailure(context.getThesaurus().getFormat(UP_READING_IMPORT_RESULT_FAILED).format(lineSuccess, allLines));
        } else {
            fileImportOccurrence.markSuccessWithFailures(context.getThesaurus().getFormat(UP_READING_IMPORT_RESULT_FAIL_WITH_ERRORS).format(lineSuccess, lineErrors));
        }
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
        objectMapper.add(new KeyStringValueBigDecimalRepetition(UsagePointImportRecordModel.UsagePointImportRecordMapping.TYPE_AND_VALUE.getObjectField(), 3, new BigDecimalParser(numberFormat)));
        return objectMapper;
    }


}
