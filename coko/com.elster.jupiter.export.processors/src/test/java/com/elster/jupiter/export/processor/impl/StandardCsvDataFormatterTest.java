package com.elster.jupiter.export.processor.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.export.DataExportException;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportProperty;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.FormattedExportData;
import com.elster.jupiter.export.MeterReadingData;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingContainer;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.IntervalBlock;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationService;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StandardCsvDataFormatterTest {

    public static final long EPOCH_MILLI = 1416783612449L;
    @Rule
    public TestRule timeZone = Using.timeZoneOfMcMurdo();

    @Mock
    private DataExportService dataExportService;

    @Mock
    private ValidationService validationService;

    @Mock
    private ValidationEvaluator validationEvaluator;

    @Mock
    private Channel channel;

    @Mock
    private AppService appService;

    @Mock
    DataExportOccurrence dataExportOccurrence;

    @Mock
    ReadingTypeDataExportItem item, item1;

    @Mock
    MeterReading data, dataLoadProfile;

    @Mock
    Meter meter, meter1;

    @Mock
    MeterActivation meterActivation;

    @Mock
    ReadingType readingType, readingType1;

    @Mock
    Logger logger;

    @Mock
    Reading reading, reading1, reading2;

    @Mock
    IntervalBlock intervalBlock;

    @Mock
    DataExportProperty propertyExtension, propertyPrefix, propertySeparator, propertyExtensionUpdated, propertyPrefixUpdated, propertyUpdateSeparateFile, property, propertyPath;

    @Mock
    ReadingQualityRecord readingQuality, readingQuality1;

    @Mock
    IntervalReading intervalReading, intervalReading1;

    @Mock
    ReadingContainer readingContainer, readingContainer1;

    @Mock
    private Thesaurus thesaurus;

    @Mock
    DataValidationStatus dataValidationStatus, dataValidationStatus1, dataValidationStatus2, dataValidationStatus3;

    @Mock
    private AppServer appServer;


    private FileSystem fileSystem;

    StandardCsvDataFormatter processor;
    List<DataExportProperty> properties;
    private Path tempDirectory;
    private Clock clock = Clock.system(ZoneId.systemDefault());

    @Before
    public void setUp() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.windows());
        Path root = fileSystem.getRootDirectories().iterator().next();
        tempDirectory = Files.createTempDirectory(root, "tmp");
        when(validationService.getEvaluator(meter, Interval.sinceEpoch().toOpenClosedRange())).thenReturn(validationEvaluator);
        when(validationService.getEvaluator(meter1, Interval.sinceEpoch().toOpenClosedRange())).thenReturn(validationEvaluator);
        doReturn(Optional.of(meterActivation)).when(meter).getMeterActivation(Instant.ofEpochMilli(EPOCH_MILLI));
        doReturn(Optional.of(meterActivation)).when(meter1).getMeterActivation(Instant.ofEpochMilli(EPOCH_MILLI));

        List<? extends BaseReading> listReadings = Arrays.asList(reading, reading1, reading2);
        when(meterActivation.getChannels()).thenReturn(Arrays.asList(channel));
        doReturn(Arrays.asList(readingType, readingType1)).when(channel).getReadingTypes();
        when(readingQuality.isMissing()).thenReturn(true);
        when(readingQuality1.isMissing()).thenReturn(false);
        when(validationEvaluator.getValidationStatus(channel, listReadings)).thenReturn(Arrays.asList(dataValidationStatus, dataValidationStatus1, dataValidationStatus2, dataValidationStatus3));
        when(dataValidationStatus.getValidationResult()).thenReturn(ValidationResult.SUSPECT);
        when(dataValidationStatus.getReadingTimestamp()).thenReturn(Instant.ofEpochMilli(EPOCH_MILLI));
        when(dataValidationStatus1.getValidationResult()).thenReturn(ValidationResult.SUSPECT);
        when(dataValidationStatus1.getReadingTimestamp()).thenReturn(Instant.ofEpochMilli(EPOCH_MILLI));
        when(dataValidationStatus2.getValidationResult()).thenReturn(ValidationResult.VALID);
        when(dataValidationStatus3.getValidationResult()).thenReturn(ValidationResult.NOT_VALIDATED);

        properties = Arrays.asList(propertyPrefix, propertyExtension, propertySeparator, propertyExtensionUpdated, propertyPrefixUpdated, propertyUpdateSeparateFile, propertyPath);
        when(propertyExtension.getName()).thenReturn("fileFormat.fileExtension");
        when(propertyExtension.getValue()).thenReturn("csv");
        when(propertyPrefix.getName()).thenReturn("fileFormat.filenamePrefix");
        when(propertyPrefix.getValue()).thenReturn("MainFile");
        when(propertySeparator.getName()).thenReturn("formatterProperties.separator");
        when(propertySeparator.getValue()).thenReturn("semicolon");
        when(propertyExtensionUpdated.getName()).thenReturn("fileFormat.updatedData.updateFileExtension");
        when(propertyExtensionUpdated.getValue()).thenReturn("csv");
        when(propertyPrefixUpdated.getName()).thenReturn("fileFormat.updatedData.updateFilenamePrefix");
        when(propertyPrefixUpdated.getValue()).thenReturn("UpdateFile");
        when(propertyUpdateSeparateFile.getName()).thenReturn("fileFormat.updatedData.separateFile");
        when(propertyUpdateSeparateFile.getValue()).thenReturn("true");
        when(propertyPath.getName()).thenReturn("fileFormat.path");
        when(propertyPath.getValue()).thenReturn("c:\\export");
        when(dataExportOccurrence.getTriggerTime()).thenReturn(Instant.ofEpochMilli(EPOCH_MILLI));

        when(item.getReadingContainer()).thenReturn(readingContainer);
        when(readingContainer.getMeter(Instant.ofEpochMilli(EPOCH_MILLI))).thenReturn(Optional.of(meter));
        when(item1.getReadingContainer()).thenReturn(readingContainer1);
        when(readingContainer1.getMeter(Instant.ofEpochMilli(EPOCH_MILLI))).thenReturn(Optional.of(meter1));
        when(meter.getMRID()).thenReturn("DeviceMRID");
        when(meter1.getMRID()).thenReturn("AnotherDeviceMRID");
        when(item.getReadingType()).thenReturn(readingType);
        when(item1.getReadingType()).thenReturn(readingType1);
        when(readingType.getMRID()).thenReturn("0.0.5.1.16.1.12.0.0.0.0.0.0.0.0.3.73.0");
        when(readingType1.getMRID()).thenReturn("0.0.5.1.17.1.13.0.0.0.0.0.0.0.0.4.75.1");

        when(data.getReadings()).thenReturn(Arrays.asList(reading, reading1, reading2));
        when(reading.getTimeStamp()).thenReturn(Instant.ofEpochMilli(EPOCH_MILLI));
        when(reading.getValue()).thenReturn(BigDecimal.TEN);

        when(reading1.getTimeStamp()).thenReturn(Instant.ofEpochMilli(EPOCH_MILLI));
        when(reading1.getValue()).thenReturn(BigDecimal.ONE);

        when(reading2.getTimeStamp()).thenReturn(Instant.ofEpochMilli(EPOCH_MILLI));
        when(reading2.getValue()).thenReturn(BigDecimal.ZERO);

        when(dataLoadProfile.getReadings()).thenReturn(Arrays.asList());
        when(dataLoadProfile.getIntervalBlocks()).thenReturn(Arrays.asList(intervalBlock));

        List<IntervalReading> intervals = Collections.unmodifiableList(Arrays.asList(intervalReading, intervalReading1));
        doReturn(intervals).when(intervalBlock).getIntervals();
        when(intervalReading.getTimeStamp()).thenReturn(Instant.ofEpochMilli(EPOCH_MILLI));
        when(intervalReading.getValue()).thenReturn(BigDecimal.ONE);
        when(intervalReading1.getTimeStamp()).thenReturn(Instant.ofEpochMilli(EPOCH_MILLI));
        when(intervalReading1.getValue()).thenReturn(BigDecimal.TEN);

        List<? extends ReadingQuality> list = Collections.unmodifiableList(Arrays.asList(readingQuality, readingQuality1));
        doReturn(list).when(reading).getReadingQualities();
        doReturn(list).when(reading1).getReadingQualities();
        doReturn(list).when(reading2).getReadingQualities();
        when(readingQuality.getTypeCode()).thenReturn("3.5.259");
        when(readingQuality1.getTypeCode()).thenReturn("3.5.258");

        doReturn(list).when(intervalReading).getReadingQualities();
        doReturn(Arrays.asList()).when(intervalReading1).getReadingQualities();

        when(appService.getAppServer()).thenReturn(Optional.of(appServer));
        when(dataExportService.getExportDirectory(appServer)).thenReturn(Optional.of(fileSystem.getPath("c:\\appserver\\export")));

        doAnswer(invocation -> DefaultStructureMarker.createRoot(clock, invocation.getArguments()[0].toString())).when(dataExportService).forRoot(any());
    }

    @Test
    public void testLinesAreOk() {
        processor = new StandardCsvDataFormatter(getPropertyMap(properties), thesaurus, validationService, dataExportService);

        processor.startExport(dataExportOccurrence, logger);
        processor.startItem(item);
        List<FormattedExportData> lines = processor.processData(new MeterReadingData(item, data, DefaultStructureMarker.createRoot(clock, "root")));
        processor.endItem(item);
        assertThat(lines).hasSize(3);
        assertThat(lines.get(0).getAppendablePayload()).isEqualTo("2014-11-24 12:00:12;DeviceMRID;0.0.5.1.16.1.12.0.0.0.0.0.0.0.0.3.73.0;10;suspect;\n");
        assertThat(lines.get(1).getAppendablePayload()).isEqualTo("2014-11-24 12:00:12;DeviceMRID;0.0.5.1.16.1.12.0.0.0.0.0.0.0.0.3.73.0;1;suspect;\n");
        assertThat(lines.get(2).getAppendablePayload()).isEqualTo("2014-11-24 12:00:12;DeviceMRID;0.0.5.1.16.1.12.0.0.0.0.0.0.0.0.3.73.0;0;suspect;\n");

        processor.startExport(dataExportOccurrence, logger);
        processor.startItem(item1);
        lines = processor.processData(new MeterReadingData(item1, data, DefaultStructureMarker.createRoot(clock, "root")));
        processor.endItem(item1);
        assertThat(lines).hasSize(3);
        assertThat(lines.get(0).getAppendablePayload()).isEqualTo("2014-11-24 12:00:12;AnotherDeviceMRID;0.0.5.1.17.1.13.0.0.0.0.0.0.0.0.4.75.1;10;suspect;\n");
        assertThat(lines.get(1).getAppendablePayload()).isEqualTo("2014-11-24 12:00:12;AnotherDeviceMRID;0.0.5.1.17.1.13.0.0.0.0.0.0.0.0.4.75.1;1;suspect;\n");
        assertThat(lines.get(2).getAppendablePayload()).isEqualTo("2014-11-24 12:00:12;AnotherDeviceMRID;0.0.5.1.17.1.13.0.0.0.0.0.0.0.0.4.75.1;0;suspect;\n");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgumentException() {
        processor = new StandardCsvDataFormatter(getPropertyMap(properties), thesaurus, validationService, dataExportService);

        processor.startExport(dataExportOccurrence, logger);
        processor.startItem(item);
        processor.processData(new MeterReadingData(item, data, DefaultStructureMarker.createRoot(clock ,"root")));
        processor.endItem(item1);
    }

    @Test(expected = DataExportException.class)
    public void testNoMeter() {
        when(readingContainer.getMeter(Instant.ofEpochMilli(EPOCH_MILLI))).thenReturn(Optional.empty());
        processor = new StandardCsvDataFormatter(getPropertyMap(properties), thesaurus, validationService, dataExportService);

        processor.startExport(dataExportOccurrence, logger);
        processor.startItem(item);
        processor.processData(new MeterReadingData(item, data, DefaultStructureMarker.createRoot(clock, "root")));
    }

    private Map<String, Object> getPropertyMap(List<DataExportProperty> properties) {
        Map<String, Object> propertyMap = new HashMap<>();
        for (DataExportProperty dataExportProperty : properties) {
            propertyMap.put(dataExportProperty.getName(), dataExportProperty.getValue());
        }
        return propertyMap;
    }

}
