package com.energyict.mdc.device.data.importers.impl;


import com.energyict.mdc.device.data.importers.impl.fields.CommonField;
import com.energyict.mdc.device.data.importers.impl.fields.FileImportField;
import com.energyict.mdc.device.data.importers.impl.parsers.LiteralStringParser;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.exception.MessageSeed;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FileImportDescriptionBasedParserTest {
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private DeviceDataImporterContext context;
    @Mock
    private Logger logger;

    private static class RepetitiveRecord extends FileImportRecord {
        private List<String> readings;
        private List<String> values;

        public RepetitiveRecord() {
            this.readings = new ArrayList<>();
            this.values = new ArrayList<>();
        }

        public List<String> getReadings() {
            return readings;
        }

        public List<String> getValues() {
            return values;
        }

        public void addReading(String reading) {
            this.readings.add(reading);
        }

        public void addValue(String value) {
            this.values.add(value);
        }
    }

    private static class RepetitiveDescription implements FileImportDescription<RepetitiveRecord> {

        private final RepetitiveRecord record;

        public RepetitiveDescription(RepetitiveRecord record) {
            this.record = record;
        }

        @Override
        public RepetitiveRecord getFileImportRecord() {
            return this.record;
        }

        @Override
        public List<FileImportField<?>> getFields(RepetitiveRecord record) {
            List<FileImportField<?>> fields = new ArrayList<>();
            LiteralStringParser stringParser = new LiteralStringParser();
            fields.add(CommonField.withParser(stringParser)
                    .withSetter(record::setDeviceMRID)
                    .markMandatory()
                    .build());
            fields.add(CommonField.withParser(stringParser)
                    .withSetter(record::addReading)
                    .markMandatory()
                    .markRepetitive()
                    .build());
            fields.add(CommonField.withParser(stringParser)
                    .withSetter(record::addValue)
                    .markMandatory()
                    .markRepetitive()
                    .build());
            return fields;
        }
    }


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

    private FileImportOccurrence mockFileImportOccurrence(String csv) {
        FileImportOccurrence importOccurrence = mock(FileImportOccurrence.class);
        when(importOccurrence.getLogger()).thenReturn(logger);
        when(importOccurrence.getContents()).thenReturn(new ByteArrayInputStream(csv.getBytes()));
        return importOccurrence;
    }

    @Test
    public void testProcessRepetitiveColumns() throws Exception {
        String csv = "Device MRID;Reading1;Value1;Reading2;Value2;Reading3;Value3\n"
                + "SPE001;r1;v1;r2;v2;r3;v3";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        RepetitiveRecord record = spy(new RepetitiveRecord());
        FileImportParser<FileImportRecord> parser = new FileImportDescriptionBasedParser(new RepetitiveDescription(record));
        FileImportProcessor<FileImportRecord> processor = mock(FileImportProcessor.class);
        DeviceDataCsvImporter<FileImportRecord> importer = DeviceDataCsvImporter.withParser(parser).withProcessor(processor).withLogger(new DevicePerLineFileImportLogger(context)).withDelimiter(';').build();

        importer.process(importOccurrence);

        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.IMPORT_RESULT_SUCCESS).format(1));
        verify(record, times(3)).addReading(Matchers.startsWith("r"));
        verify(record, times(3)).addValue(Matchers.startsWith("v"));
    }

    @Test
    public void testProcessRepetitiveColumnsDeviceReadingsCase() throws Exception {
        String csv = "Device MRID;Reading date;Reading1;Value1;Reading2;Value2;Reading3;Value3\n"
                + "SPE001;28/07/2015 09:14;r1;v1;r2;v2;r3;v3";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        RepetitiveRecord record = spy(new RepetitiveRecord());
        FileImportParser<FileImportRecord> parser = new FileImportDescriptionBasedParser(new FileImportDescription<RepetitiveRecord>() {
            @Override
            public RepetitiveRecord getFileImportRecord() {
                return record;
            }

            @Override
            public List<FileImportField<?>> getFields(RepetitiveRecord record) {
                List<FileImportField<?>> fields = new ArrayList<>();
                LiteralStringParser stringParser = new LiteralStringParser();
                fields.add(CommonField.withParser(stringParser)
                        .withSetter(record::setDeviceMRID)
                        .markMandatory()
                        .build());
                fields.add(CommonField.withParser(stringParser)
                        .withSetter(record::setDeviceMRID)
                        .markMandatory()
                        .build());
                fields.add(CommonField.withParser(stringParser)
                        .withSetter(record::addReading)
                        .markMandatory()
                        .markRepetitive()
                        .build());
                fields.add(CommonField.withParser(stringParser)
                        .withSetter(record::addValue)
                        .markMandatory()
                        .markRepetitive()
                        .build());
                return fields;
            }
        });
        FileImportProcessor<FileImportRecord> processor = mock(FileImportProcessor.class);
        DeviceDataCsvImporter<FileImportRecord> importer = DeviceDataCsvImporter.withParser(parser).withProcessor(processor).withLogger(new DevicePerLineFileImportLogger(context)).withDelimiter(';').build();

        importer.process(importOccurrence);

        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.IMPORT_RESULT_SUCCESS).format(1));
        verify(record, times(3)).addReading(Matchers.startsWith("r"));
        verify(record, times(3)).addValue(Matchers.startsWith("v"));
    }

    @Test
    public void testProcessRepetitiveColumnsWithoutDescriptionSupport() throws Exception {
        String csv = "Device MRID;Reading1;Value1;Reading2;Value2;Reading3;Value3\n"
                + "SPE001;r1;v1;r2;v2;r3;v3";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        RepetitiveRecord record = spy(new RepetitiveRecord());
        FileImportParser<FileImportRecord> parser = new FileImportDescriptionBasedParser(new FileImportDescription<RepetitiveRecord>() {
            @Override
            public RepetitiveRecord getFileImportRecord() {
                return record;
            }

            @Override
            public List<FileImportField<?>> getFields(RepetitiveRecord record) {
                List<FileImportField<?>> fields = new ArrayList<>();
                LiteralStringParser stringParser = new LiteralStringParser();
                fields.add(CommonField.withParser(stringParser)
                        .withSetter(record::setDeviceMRID)
                        .markMandatory()
                        .build());
                fields.add(CommonField.withParser(stringParser)
                        .withSetter(record::addReading)
                        .markMandatory()
                        .build());
                fields.add(CommonField.withParser(stringParser)
                        .withSetter(record::addValue)
                        .markMandatory()
                        .build());
                return fields;
            }
        });
        FileImportProcessor<FileImportRecord> processor = mock(FileImportProcessor.class);
        DeviceDataCsvImporter<FileImportRecord> importer = DeviceDataCsvImporter.withParser(parser).withProcessor(processor).withLogger(new DevicePerLineFileImportLogger(context)).withDelimiter(';').build();

        importer.process(importOccurrence);

        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.IMPORT_RESULT_SUCCESS).format(1));
        verify(record, times(1)).addReading(Matchers.startsWith("r"));
        verify(record, times(1)).addValue(Matchers.startsWith("v"));
    }

    @Test
    public void testTrimEmptyColumns() {
        String csv = "Device MRID;Optional1;Optional2;Optional3\n"
                + "SPE001;;;";
        FileImportOccurrence importOccurrence = mockFileImportOccurrence(csv);
        RepetitiveRecord record = spy(new RepetitiveRecord());
        FileImportParser<FileImportRecord> parser = new FileImportDescriptionBasedParser(new FileImportDescription<RepetitiveRecord>() {
            @Override
            public RepetitiveRecord getFileImportRecord() {
                return record;
            }

            @Override
            public List<FileImportField<?>> getFields(RepetitiveRecord record) {
                List<FileImportField<?>> fields = new ArrayList<>();
                LiteralStringParser stringParser = new LiteralStringParser();
                fields.add(CommonField.withParser(stringParser)
                        .withSetter(record::setDeviceMRID)
                        .markMandatory()
                        .build());
                fields.add(CommonField.withParser(stringParser)
                        .withSetter(record::addReading)
                        .build());
                fields.add(CommonField.withParser(stringParser)
                        .withSetter(record::addValue)
                        .build());
                fields.add(CommonField.withParser(stringParser)
                        .withSetter(record::addValue)
                        .build());
                return fields;
            }
        });
        FileImportProcessor<FileImportRecord> processor = mock(FileImportProcessor.class);
        DeviceDataCsvImporter<FileImportRecord> importer = DeviceDataCsvImporter.withParser(parser).withProcessor(processor)
                .withLogger(new DevicePerLineFileImportLogger(context)).withDelimiter(';').build();

        importer.process(importOccurrence);

        verify(importOccurrence).markSuccess(thesaurus.getFormat(TranslationKeys.IMPORT_RESULT_SUCCESS).format(1));
        //assert no errors
    }
}
