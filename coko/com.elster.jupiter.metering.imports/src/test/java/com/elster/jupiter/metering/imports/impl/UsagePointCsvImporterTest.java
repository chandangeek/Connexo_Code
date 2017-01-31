/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.imports.impl;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.metering.imports.impl.exceptions.FileImportParserException;
import com.elster.jupiter.metering.imports.impl.exceptions.ProcessorException;
import com.elster.jupiter.metering.imports.impl.usagepoint.UsagePointImportRecord;
import com.elster.jupiter.metering.imports.impl.usagepoint.UsagePointsImportLogger;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;

import org.apache.commons.csv.CSVRecord;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UsagePointCsvImporterTest {

    static class CSVLineMatcher extends ArgumentMatcher<CSVRecord> implements Serializable {
        private int line;

        CSVLineMatcher(int lineNumber) {
            this.line = lineNumber;
        }

        @Override
        public boolean matches(Object argument) {
            if (argument instanceof CSVRecord) {
                return ((CSVRecord) argument).getRecordNumber() == line;
            } else if (argument instanceof FileImportRecord) {
                return ((FileImportRecord) argument).getLineNumber() == line;
            }
            return false;
        }
    }

    static class RecordLineMatcher extends ArgumentMatcher<FileImportRecord> implements Serializable {
        private int line;

        RecordLineMatcher(int lineNumber) {
            this.line = lineNumber;
        }

        @Override
        public boolean matches(Object argument) {
            if (argument instanceof FileImportRecord) {
                return ((FileImportRecord) argument).getLineNumber() == line;
            }
            return false;
        }
    }

    @Mock
    private Thesaurus thesaurus;
    @Mock
    private MeteringDataImporterContext context;
    @Mock
    private Logger logger;
    @Mock
    private NlsMessageFormat simpleNlsMessageFormat;

    @Before
    public void beforeTest() {
        reset(logger, context, thesaurus);
        when(simpleNlsMessageFormat.format(anyObject())).thenReturn("format");
        when(thesaurus.getFormat(any(TranslationKey.class))).thenAnswer(invocationOnMock -> simpleNlsMessageFormat);
        when(thesaurus.getFormat(any(MessageSeed.class))).thenAnswer(invocationOnMock -> simpleNlsMessageFormat);
        when(context.getThesaurus()).thenReturn(thesaurus);
    }

    public FileImportParser<FileImportRecord> mockParserWithExceptionOnLine(Integer lineNumber) {
        FileImportParser<FileImportRecord> parser = mock(FileImportParser.class);
        doAnswer(invocationOnMock -> new UsagePointImportRecord(((CSVRecord) invocationOnMock.getArguments()[0]).getRecordNumber()))
                .when(parser).parse(Matchers.any(CSVRecord.class));
        if (lineNumber != null) {
            doThrow(new FileImportParserException(MessageSeeds.FILE_FORMAT_ERROR, 0, 0, 0))
                    .when(parser).parse(Matchers.argThat(new CSVLineMatcher(lineNumber)));
        }
        return parser;
    }

    public FileImportProcessor<FileImportRecord> mockProcessor(Integer errorLineNumber, Integer warningLineNumber) {
        FileImportProcessor<FileImportRecord> processor = mock(FileImportProcessor.class);
        if (errorLineNumber != null) {
            doThrow(new ProcessorException(MessageSeeds.FILE_FORMAT_ERROR, 0, 0, 0))
                    .when(processor)
                    .process(Matchers.argThat(new RecordLineMatcher(errorLineNumber)), Matchers.any(FileImportLogger.class));
        }
        if (warningLineNumber != null) {
            doAnswer(invocationOnMock -> {
                ((FileImportLogger) invocationOnMock.getArguments()[1]).warning(TranslationKeys.Labels.IMPORT_RESULT_FAIL);
                return null;
            }).when(processor)
                    .process(Matchers.argThat(new RecordLineMatcher(warningLineNumber)), Matchers.any(FileImportLogger.class));
        }
        return processor;
    }

    private FileImportOccurrence mockFileImportOccurrence(String csv) {
        FileImportOccurrence importOccurrence = mock(FileImportOccurrence.class);
        when(importOccurrence.getLogger()).thenReturn(logger);
        when(importOccurrence.getContents()).thenReturn(new ByteArrayInputStream(csv.getBytes()));
        return importOccurrence;
    }

    private CsvImporter<FileImportRecord> mockImporter(FileImportParser<FileImportRecord> parser, FileImportProcessor<FileImportRecord> processor) {
        return CsvImporter.withParser(parser)
                .withProcessor(processor)
                .withLogger(new UsagePointsImportLogger(context))
                .withDelimiter(';')
                .build();
    }

    @Test
    // No ups were processed (No ups in file)
    public void testNoUsagePointsInFile() throws Exception {
        String csv = "mRID;SomeColumn";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImportParser<FileImportRecord> parser = mockParserWithExceptionOnLine(null);
        FileImportProcessor<FileImportRecord> processor = mockProcessor(null, null);
        CsvImporter<FileImportRecord> importer = mockImporter(parser, processor);

        importer.process(importOccurrence);
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, never()).severe(Matchers.anyString());
    }

    @Test
    // No ups were processed
    public void testBadColumnTitles() throws Exception {
        String csv = "Column1;Column2;Column2\nvalue;value2;value3";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImportParser<FileImportRecord> parser = mockParserWithExceptionOnLine(null);
        FileImportProcessor<FileImportRecord> processor = mockProcessor(null, null);
        CsvImporter<FileImportRecord> importer = mockImporter(parser, processor);

        importer.process(importOccurrence);
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, times(1)).severe(Matchers.anyString());
    }

    @Test
    // Parser fails on up 1
    // 0 success, 0 warning, 0 error
    public void testFailOnUsagePoint1() throws Exception {
        String csv = "mRID\nup1\nup2\nup3";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImportParser<FileImportRecord> parser = mockParserWithExceptionOnLine(2);
        FileImportProcessor<FileImportRecord> processor = mockProcessor(null, null);
        CsvImporter<FileImportRecord> importer = mockImporter(parser, processor);

        importer.process(importOccurrence);
    }

    @Test
    // Parser fails on up 2
    // 1 success, 0 warning, 0 error
    public void testFailOnUsagePoint2() throws Exception {
        String csv = "mRID\nup1\nup2\nup3";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImportParser<FileImportRecord> parser = mockParserWithExceptionOnLine(3);
        FileImportProcessor<FileImportRecord> processor = mockProcessor(null, null);
        CsvImporter<FileImportRecord> importer = mockImporter(parser, processor);

        importer.process(importOccurrence);
        verify(importOccurrence).markFailure(thesaurus.getFormat(TranslationKeys.Labels.IMPORT_RESULT_FAIL).format(1));
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, times(1)).severe(Matchers.anyString());
    }

    @Test
    // Parser fails on up 2
    // 1 success, 1 warning, 0 error
    public void testFailOnUsagePoint2WithWarningOnUsagePoint1() throws Exception {
        String csv = "mRID\nup1\nup2\nup3";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImportParser<FileImportRecord> parser = mockParserWithExceptionOnLine(3);
        FileImportProcessor<FileImportRecord> processor = mockProcessor(null, 2);
        CsvImporter<FileImportRecord> importer = mockImporter(parser, processor);

        importer.process(importOccurrence);
        verify(importOccurrence).markFailure(thesaurus.getFormat(TranslationKeys.Labels.IMPORT_RESULT_FAIL_WITH_WARN)
                .format(1, 1));
        verify(logger, times(1)).info(Matchers.anyString());
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, times(1)).severe(Matchers.anyString());
    }

    @Test
    // Parser fails on up 2
    // 0 success, 0 warning, 1 error
    public void testFailOnUsagePoint2WithErrorOnUsagePoint1() throws Exception {
        String csv = "mRID\nup1\nup2\nup3";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImportParser<FileImportRecord> parser = mockParserWithExceptionOnLine(3);
        FileImportProcessor<FileImportRecord> processor = mockProcessor(2, null);
        CsvImporter<FileImportRecord> importer = mockImporter(parser, processor);

        importer.process(importOccurrence);
        verify(importOccurrence).markFailure(thesaurus.getFormat(TranslationKeys.Labels.IMPORT_RESULT_FAIL_WITH_ERRORS)
                .format(0, 1));
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, times(1)).warning(Matchers.anyString());
        verify(logger, times(1)).severe(Matchers.anyString());
    }

    @Test
    // Parser fails on up 3
    // 1 success, 1 warning, 1 error
    public void testFailOnUsagePoint3WithErrorOnUsagePoint1AndWarningOnUsagePoint2() throws Exception {
        String csv = "mRID\nup1\nup2\nup3";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImportParser<FileImportRecord> parser = mockParserWithExceptionOnLine(4);
        FileImportProcessor<FileImportRecord> processor = mockProcessor(2, 3);
        CsvImporter<FileImportRecord> importer = mockImporter(parser, processor);

        importer.process(importOccurrence);
        verify(importOccurrence).markFailure(thesaurus.getFormat(TranslationKeys.Labels.IMPORT_RESULT_FAIL_WITH_WARN_AND_ERRORS)
                .format(1, 1, 1));
        verify(logger, times(1)).info(Matchers.anyString());
        verify(logger, times(1)).warning(Matchers.anyString());
        verify(logger, times(1)).severe(Matchers.anyString());
    }

    @Test
    // Parser fails on up 3
    // 1 success, 0 warning, 1 error
    public void testFailOnUsagePoint3WithErrorOnUsagePoint1() throws Exception {
        String csv = "mRID\nup1\nup2\nup3";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImportParser<FileImportRecord> parser = mockParserWithExceptionOnLine(4);
        FileImportProcessor<FileImportRecord> processor = mockProcessor(2, null);
        CsvImporter<FileImportRecord> importer = mockImporter(parser, processor);

        importer.process(importOccurrence);
        verify(importOccurrence).markFailure(thesaurus.getFormat(TranslationKeys.Labels.IMPORT_RESULT_FAIL_WITH_ERRORS)
                .format(1, 1));
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, times(1)).warning(Matchers.anyString());
        verify(logger, times(1)).severe(Matchers.anyString());
    }

    @Test
    // Parser successfully finished
    // 2 success, 0 warning, 0 error
    public void testSuccessWithTwoUsagePoints() throws Exception {
        String csv = "mRID\nup1\nup2";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImportParser<FileImportRecord> parser = mockParserWithExceptionOnLine(null);
        FileImportProcessor<FileImportRecord> processor = mockProcessor(null, null);
        CsvImporter<FileImportRecord> importer = mockImporter(parser, processor);

        importer.process(importOccurrence);
        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.Labels.IMPORT_RESULT_SUCCESS)
                .format(2));
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, never()).severe(Matchers.anyString());
    }

    @Test
    // Parser successfully finished
    // 1 success, 1 warning, 0 error
    public void testSuccessWithWarn() throws Exception {
        String csv = "mRID\nup1";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImportParser<FileImportRecord> parser = mockParserWithExceptionOnLine(null);
        FileImportProcessor<FileImportRecord> processor = mockProcessor(null, 2);
        CsvImporter<FileImportRecord> importer = mockImporter(parser, processor);

        importer.process(importOccurrence);
        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.Labels.IMPORT_RESULT_SUCCESS_WITH_WARN)
                .format(1, 1));
        verify(logger, times(1)).info(Matchers.anyString());
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, never()).severe(Matchers.anyString());
    }

    @Test
    // Parser successfully finished
    // 0 success, 0 warning, 1 error
    public void testSuccessWithErrors() throws Exception {
        String csv = "mRID\nup1\nup2";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImportParser<FileImportRecord> parser = mockParserWithExceptionOnLine(null);
        FileImportProcessor<FileImportRecord> processor = mockProcessor(2, null);
        CsvImporter<FileImportRecord> importer = mockImporter(parser, processor);

        importer.process(importOccurrence);
        verify(importOccurrence).markSuccessWithFailures(thesaurus.getFormat(TranslationKeys.Labels.IMPORT_RESULT_SUCCESS_WITH_ERRORS)
                .format(0, 1));
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, times(1)).warning(Matchers.anyString());
        verify(logger, never()).severe(Matchers.anyString());
    }

    @Test
    // Parser successfully finished
    // 1 success, 1 warning, 1 error
    public void testSuccessWithWarnAndErrors() throws Exception {
        String csv = "mRID\nup1\nup2";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImportParser<FileImportRecord> parser = mockParserWithExceptionOnLine(null);
        FileImportProcessor<FileImportRecord> processor = mockProcessor(3, 2);
        CsvImporter<FileImportRecord> importer = mockImporter(parser, processor);

        importer.process(importOccurrence);
        verify(importOccurrence).markSuccessWithFailures(thesaurus.getFormat(TranslationKeys.Labels.IMPORT_RESULT_SUCCESS_WITH_WARN_AND_ERRORS)
                .format(1, 1, 1));
        verify(logger, times(1)).info(Matchers.anyString());
        verify(logger, times(1)).warning(Matchers.anyString());
        verify(logger, never()).severe(Matchers.anyString());
    }
}
