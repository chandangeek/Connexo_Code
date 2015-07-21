package com.energyict.mdc.device.data.importers.impl;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.data.importers.impl.exceptions.FileImportParserException;
import com.energyict.mdc.device.data.importers.impl.exceptions.ProcessorException;
import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.logging.Logger;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeviceDataCsvImporterTestIT {

    static class CSVLineMatcher extends ArgumentMatcher<CSVRecord> implements Serializable {

        private int line;

        public CSVLineMatcher(int lineNumber) {
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

        public RecordLineMatcher(int lineNumber) {
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
    DeviceDataImporterContext context;

    @Before
    public void beforeTest() {
        when(thesaurus.getString(anyString(), anyString())).thenAnswer(invocationOnMock -> {
            for (MessageSeed messageSeeds : MessageSeeds.values()) {
                if (messageSeeds.getKey().equals(invocationOnMock.getArguments()[0])) {
                    return messageSeeds.getDefaultFormat();
                }
            }
            for (TranslationKey translation : TranslationKeys.values()) {
                if (translation.getKey().equals(invocationOnMock.getArguments()[0])) {
                    return translation.getDefaultFormat();
                }
            }
            return invocationOnMock.getArguments()[1];
        });
        when(context.getThesaurus()).thenReturn(thesaurus);
    }

    public FileImportParser<FileImportRecord> mockParserWithExceptionOnLine(Integer lineNumber) {
        FileImportParser<FileImportRecord> parser = mock(FileImportParser.class);
        doAnswer(invocationOnMock -> new FileImportRecord(((CSVRecord) invocationOnMock.getArguments()[0]).getRecordNumber()))
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
                .when(processor).process(Matchers.argThat(new RecordLineMatcher(errorLineNumber)), Matchers.any(FileImportRecordContext.class));
        }
        if (warningLineNumber != null) {
            doAnswer(invocationOnMock -> {
                ((FileImportRecordContext) invocationOnMock.getArguments()[1]).warning(TranslationKeys.DATA_COLUMN_BATCH);
                return null;
            }).when(processor).process(Matchers.argThat(new RecordLineMatcher(warningLineNumber)), Matchers.any(FileImportRecordContext.class));
        }
        return processor;
    }

    @Test
    // No devices were processed (No devices in file)
    public void testNoDevicesInFile() throws Exception {
        String csv = "Device MRID;SomeColumn";
        Logger logger = mock(Logger.class);
        FileImportOccurrence importOccurrence = mock(FileImportOccurrence.class);
        when(importOccurrence.getLogger()).thenReturn(logger);
        when(importOccurrence.getContents()).thenReturn(new ByteArrayInputStream(csv.getBytes()));
        FileImportParser<FileImportRecord> parser = mockParserWithExceptionOnLine(null);
        FileImportProcessor<FileImportRecord> processor = mockProcessor(null, null);
        DeviceDataCsvImporter<FileImportRecord> importer = DeviceDataCsvImporter.withParser(parser).withProcessor(processor).withDelimiter(';').build(context);

        importer.process(importOccurrence);
        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED.getDefaultFormat());
    }

    @Test
    // Processor fails on device 1
    // 0 success, 0 warning, 0 error
    public void testFailOnDevice1() throws Exception {
        String csv = "Device MRID\ndevice1\ndevice2\ndevice3";
        Logger logger = mock(Logger.class);
        FileImportOccurrence importOccurrence = mock(FileImportOccurrence.class);
        when(importOccurrence.getLogger()).thenReturn(logger);
        when(importOccurrence.getContents()).thenReturn(new ByteArrayInputStream(csv.getBytes()));
        FileImportParser<FileImportRecord> parser = mockParserWithExceptionOnLine(2);
        FileImportProcessor<FileImportRecord> processor = mockProcessor(null, null);
        DeviceDataCsvImporter<FileImportRecord> importer = DeviceDataCsvImporter.withParser(parser).withProcessor(processor).withDelimiter(';').build(context);

        importer.process(importOccurrence);
        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED.getDefaultFormat());
    }

    @Test
    // Processor fails on device 2
    // 1 success, 0 warning, 0 error
    public void testFailOnDevice2() throws Exception {
        String csv = "Device MRID\ndevice1\ndevice2\ndevice3";
        Logger logger = mock(Logger.class);
        FileImportOccurrence importOccurrence = mock(FileImportOccurrence.class);
        when(importOccurrence.getLogger()).thenReturn(logger);
        when(importOccurrence.getContents()).thenReturn(new ByteArrayInputStream(csv.getBytes()));
        FileImportParser<FileImportRecord> parser = mockParserWithExceptionOnLine(3);
        FileImportProcessor<FileImportRecord> processor = mockProcessor(null, null);
        DeviceDataCsvImporter<FileImportRecord> importer = DeviceDataCsvImporter.withParser(parser).withProcessor(processor).withDelimiter(';').build(context);

        importer.process(importOccurrence);
        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_SOME_DEVICES_WERE_PROCESSED_WITH_ERRORS.getTranslated(thesaurus, 1, 1));
    }

    @Test
    // Processor fails on device 2
    // 1 success, 1 warning, 0 error
    public void testFailOnDevice2WithWarningOnDevice1() throws Exception {
        String csv = "Device MRID\ndevice1\ndevice2\ndevice3";
        Logger logger = mock(Logger.class);
        FileImportOccurrence importOccurrence = mock(FileImportOccurrence.class);
        when(importOccurrence.getLogger()).thenReturn(logger);
        when(importOccurrence.getContents()).thenReturn(new ByteArrayInputStream(csv.getBytes()));
        FileImportParser<FileImportRecord> parser = mockParserWithExceptionOnLine(3);
        FileImportProcessor<FileImportRecord> processor = mockProcessor(null, 2);
        DeviceDataCsvImporter<FileImportRecord> importer = DeviceDataCsvImporter.withParser(parser).withProcessor(processor).withDelimiter(';').build(context);

        importer.process(importOccurrence);
        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_SOME_DEVICES_WERE_PROCESSED_WITH_WARN.getTranslated(thesaurus, 1, 1));
    }

    @Test
    // Processor fails on device 2
    // 0 success, 0 warning, 1 error
    public void testFailOnDevice2WithErrorOnDevice1() throws Exception {
        String csv = "Device MRID\ndevice1\ndevice2\ndevice3";
        Logger logger = mock(Logger.class);
        FileImportOccurrence importOccurrence = mock(FileImportOccurrence.class);
        when(importOccurrence.getLogger()).thenReturn(logger);
        when(importOccurrence.getContents()).thenReturn(new ByteArrayInputStream(csv.getBytes()));
        FileImportParser<FileImportRecord> parser = mockParserWithExceptionOnLine(3);
        FileImportProcessor<FileImportRecord> processor = mockProcessor(2, null);
        DeviceDataCsvImporter<FileImportRecord> importer = DeviceDataCsvImporter.withParser(parser).withProcessor(processor).withDelimiter(';').build(context);

        importer.process(importOccurrence);
        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_SOME_DEVICES_WERE_PROCESSED_WITH_ERRORS.getTranslated(thesaurus, 0, 1));
    }

    @Test
    // Processor fails on device 3
    // 1 success, 1 warning, 1 error
    public void testFailOnDevice3WithErrorOnDevice1AndWarningOnDevice2() throws Exception {
        String csv = "Device MRID\ndevice1\ndevice2\ndevice3";
        Logger logger = mock(Logger.class);
        FileImportOccurrence importOccurrence = mock(FileImportOccurrence.class);
        when(importOccurrence.getLogger()).thenReturn(logger);
        when(importOccurrence.getContents()).thenReturn(new ByteArrayInputStream(csv.getBytes()));
        FileImportParser<FileImportRecord> parser = mockParserWithExceptionOnLine(4);
        FileImportProcessor<FileImportRecord> processor = mockProcessor(2, 3);
        DeviceDataCsvImporter<FileImportRecord> importer = DeviceDataCsvImporter.withParser(parser).withProcessor(processor).withDelimiter(';').build(context);

        importer.process(importOccurrence);
        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_SOME_DEVICES_WERE_PROCESSED_WITH_ERRORS_AND_WARN.getTranslated(thesaurus, 1, 1, 1));
    }

    @Test
    // Processor fails on device 3
    // 1 success, 0 warning, 1 error
    public void testFailOnDevice3WithErrorOnDevice1() throws Exception {
        String csv = "Device MRID\ndevice1\ndevice2\ndevice3";
        Logger logger = mock(Logger.class);
        FileImportOccurrence importOccurrence = mock(FileImportOccurrence.class);
        when(importOccurrence.getLogger()).thenReturn(logger);
        when(importOccurrence.getContents()).thenReturn(new ByteArrayInputStream(csv.getBytes()));
        FileImportParser<FileImportRecord> parser = mockParserWithExceptionOnLine(4);
        FileImportProcessor<FileImportRecord> processor = mockProcessor(2, null);
        DeviceDataCsvImporter<FileImportRecord> importer = DeviceDataCsvImporter.withParser(parser).withProcessor(processor).withDelimiter(';').build(context);

        importer.process(importOccurrence);
        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_SOME_DEVICES_WERE_PROCESSED_WITH_ERRORS.getTranslated(thesaurus, 1, 1));
    }
}
