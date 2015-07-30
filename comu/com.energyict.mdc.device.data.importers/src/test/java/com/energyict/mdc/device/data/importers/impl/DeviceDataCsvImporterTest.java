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
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DeviceDataCsvImporterTest {

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
    private DeviceDataImporterContext context;
    @Mock
    private Logger logger;

    @Before
    public void beforeTest() {
        reset(logger, context, thesaurus);
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
                    .when(processor).process(Matchers.argThat(new RecordLineMatcher(errorLineNumber)), Matchers.any(FileImportLogger.class));
        }
        if (warningLineNumber != null) {
            doAnswer(invocationOnMock -> {
                ((FileImportLogger) invocationOnMock.getArguments()[1]).warning(TranslationKeys.IMPORT_RESULT_FAIL);
                return null;
            }).when(processor).process(Matchers.argThat(new RecordLineMatcher(warningLineNumber)), Matchers.any(FileImportLogger.class));
        }
        return processor;
    }

    private FileImportOccurrence mockFileImportOccurrence(String csv) {
        FileImportOccurrence importOccurrence = mock(FileImportOccurrence.class);
        when(importOccurrence.getLogger()).thenReturn(logger);
        when(importOccurrence.getContents()).thenReturn(new ByteArrayInputStream(csv.getBytes()));
        return importOccurrence;
    }

    private DeviceDataCsvImporter<FileImportRecord> mockImporter(FileImportParser<FileImportRecord> parser, FileImportProcessor<FileImportRecord> processor) {
        return DeviceDataCsvImporter.withParser(parser).withProcessor(processor).withLogger(new DevicePerLineFileImportLogger(context)).withDelimiter(';').build(context);
    }

    @Test
    // No devices were processed (No devices in file)
    public void testNoDevicesInFile() throws Exception {
        String csv = "Device MRID;SomeColumn";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImportParser<FileImportRecord> parser = mockParserWithExceptionOnLine(null);
        FileImportProcessor<FileImportRecord> processor = mockProcessor(null, null);
        DeviceDataCsvImporter<FileImportRecord> importer = mockImporter(parser, processor);

        importer.process(importOccurrence);
        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED.getDefaultFormat());
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, never()).severe(Matchers.anyString());
    }

    @Test
    // No devices were processed
    public void testBadColumnTitles() throws Exception {
        String csv = "Column1;Column2;Column2\nvalue;value2;value3";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImportParser<FileImportRecord> parser = mockParserWithExceptionOnLine(null);
        FileImportProcessor<FileImportRecord> processor = mockProcessor(null, null);
        DeviceDataCsvImporter<FileImportRecord> importer = mockImporter(parser, processor);

        importer.process(importOccurrence);
        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED.getDefaultFormat());
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, times(1)).severe(Matchers.anyString());
    }

    @Test
    // Parser fails on device 1
    // 0 success, 0 warning, 0 error
    public void testFailOnDevice1() throws Exception {
        String csv = "Device MRID\ndevice1\ndevice2\ndevice3";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImportParser<FileImportRecord> parser = mockParserWithExceptionOnLine(2);
        FileImportProcessor<FileImportRecord> processor = mockProcessor(null, null);
        DeviceDataCsvImporter<FileImportRecord> importer = mockImporter(parser, processor);

        importer.process(importOccurrence);
        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_NO_DEVICES_WERE_PROCESSED.getDefaultFormat());
    }

    @Test
    // Parser fails on device 2
    // 1 success, 0 warning, 0 error
    public void testFailOnDevice2() throws Exception {
        String csv = "Device MRID\ndevice1\ndevice2\ndevice3";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImportParser<FileImportRecord> parser = mockParserWithExceptionOnLine(3);
        FileImportProcessor<FileImportRecord> processor = mockProcessor(null, null);
        DeviceDataCsvImporter<FileImportRecord> importer = mockImporter(parser, processor);

        importer.process(importOccurrence);
        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_FAIL.getTranslated(thesaurus, 1));
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, times(1)).severe(Matchers.anyString());
    }

    @Test
    // Parser fails on device 2
    // 1 success, 1 warning, 0 error
    public void testFailOnDevice2WithWarningOnDevice1() throws Exception {
        String csv = "Device MRID\ndevice1\ndevice2\ndevice3";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImportParser<FileImportRecord> parser = mockParserWithExceptionOnLine(3);
        FileImportProcessor<FileImportRecord> processor = mockProcessor(null, 2);
        DeviceDataCsvImporter<FileImportRecord> importer = mockImporter(parser, processor);

        importer.process(importOccurrence);
        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_FAIL_WITH_WARN.getTranslated(thesaurus, 1, 1));
        verify(logger, times(1)).info(Matchers.anyString());
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, times(1)).severe(Matchers.anyString());
    }

    @Test
    // Parser fails on device 2
    // 0 success, 0 warning, 1 error
    public void testFailOnDevice2WithErrorOnDevice1() throws Exception {
        String csv = "Device MRID\ndevice1\ndevice2\ndevice3";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImportParser<FileImportRecord> parser = mockParserWithExceptionOnLine(3);
        FileImportProcessor<FileImportRecord> processor = mockProcessor(2, null);
        DeviceDataCsvImporter<FileImportRecord> importer = mockImporter(parser, processor);

        importer.process(importOccurrence);
        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_FAIL_WITH_ERRORS.getTranslated(thesaurus, 0, 1));
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, times(1)).warning(Matchers.anyString());
        verify(logger, times(1)).severe(Matchers.anyString());
    }

    @Test
    // Parser fails on device 3
    // 1 success, 1 warning, 1 error
    public void testFailOnDevice3WithErrorOnDevice1AndWarningOnDevice2() throws Exception {
        String csv = "Device MRID\ndevice1\ndevice2\ndevice3";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImportParser<FileImportRecord> parser = mockParserWithExceptionOnLine(4);
        FileImportProcessor<FileImportRecord> processor = mockProcessor(2, 3);
        DeviceDataCsvImporter<FileImportRecord> importer = mockImporter(parser, processor);

        importer.process(importOccurrence);
        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_FAIL_WITH_WARN_AND_ERRORS.getTranslated(thesaurus, 1, 1, 1));
        verify(logger, times(1)).info(Matchers.anyString());
        verify(logger, times(1)).warning(Matchers.anyString());
        verify(logger, times(1)).severe(Matchers.anyString());
    }

    @Test
    // Parser fails on device 3
    // 1 success, 0 warning, 1 error
    public void testFailOnDevice3WithErrorOnDevice1() throws Exception {
        String csv = "Device MRID\ndevice1\ndevice2\ndevice3";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImportParser<FileImportRecord> parser = mockParserWithExceptionOnLine(4);
        FileImportProcessor<FileImportRecord> processor = mockProcessor(2, null);
        DeviceDataCsvImporter<FileImportRecord> importer = mockImporter(parser, processor);

        importer.process(importOccurrence);
        verify(importOccurrence).markFailure(TranslationKeys.IMPORT_RESULT_FAIL_WITH_ERRORS.getTranslated(thesaurus, 1, 1));
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, times(1)).warning(Matchers.anyString());
        verify(logger, times(1)).severe(Matchers.anyString());
    }

    @Test
    // Parser successfully finished
    // 2 success, 0 warning, 0 error
    public void testSuccessWithTwoDevices() throws Exception {
        String csv = "Device MRID\ndevice1\ndevice2";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImportParser<FileImportRecord> parser = mockParserWithExceptionOnLine(null);
        FileImportProcessor<FileImportRecord> processor = mockProcessor(null, null);
        DeviceDataCsvImporter<FileImportRecord> importer = mockImporter(parser, processor);

        importer.process(importOccurrence);
        verify(importOccurrence).markSuccess(TranslationKeys.IMPORT_RESULT_SUCCESS.getTranslated(thesaurus, 2));
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, never()).severe(Matchers.anyString());
    }

    @Test
    // Parser successfully finished
    // 1 success, 1 warning, 0 error
    public void testSuccessWithWarn() throws Exception {
        String csv = "Device MRID\ndevice1";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImportParser<FileImportRecord> parser = mockParserWithExceptionOnLine(null);
        FileImportProcessor<FileImportRecord> processor = mockProcessor(null, 2);
        DeviceDataCsvImporter<FileImportRecord> importer = mockImporter(parser, processor);

        importer.process(importOccurrence);
        verify(importOccurrence).markSuccess(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_WARN.getTranslated(thesaurus, 1, 1));
        verify(logger, times(1)).info(Matchers.anyString());
        verify(logger, never()).warning(Matchers.anyString());
        verify(logger, never()).severe(Matchers.anyString());
    }

    @Test
    // Parser successfully finished
    // 0 success, 0 warning, 1 error
    public void testSuccessWithErrors() throws Exception {
        String csv = "Device MRID\ndevice1";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImportParser<FileImportRecord> parser = mockParserWithExceptionOnLine(null);
        FileImportProcessor<FileImportRecord> processor = mockProcessor(2, null);
        DeviceDataCsvImporter<FileImportRecord> importer = mockImporter(parser, processor);

        importer.process(importOccurrence);
        verify(importOccurrence).markSuccessWithFailures(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_ERRORS.getTranslated(thesaurus, 0, 1));
        verify(logger, never()).info(Matchers.anyString());
        verify(logger, times(1)).warning(Matchers.anyString());
        verify(logger, never()).severe(Matchers.anyString());
    }

    @Test
    // Parser successfully finished
    // 1 success, 1 warning, 1 error
    public void testSuccessWithWarnAndErrors() throws Exception {
        String csv = "Device MRID\ndevice1\ndevice2";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        FileImportParser<FileImportRecord> parser = mockParserWithExceptionOnLine(null);
        FileImportProcessor<FileImportRecord> processor = mockProcessor(3, 2);
        DeviceDataCsvImporter<FileImportRecord> importer = mockImporter(parser, processor);

        importer.process(importOccurrence);
        verify(importOccurrence).markSuccessWithFailures(TranslationKeys.IMPORT_RESULT_SUCCESS_WITH_WARN_AND_ERRORS.getTranslated(thesaurus, 1, 1, 1));
        verify(logger, times(1)).info(Matchers.anyString());
        verify(logger, times(1)).warning(Matchers.anyString());
        verify(logger, never()).severe(Matchers.anyString());
    }
}
