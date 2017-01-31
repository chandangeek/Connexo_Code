/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.processor.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportProperty;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.FormattedData;
import com.elster.jupiter.export.FormattedExportData;
import com.elster.jupiter.export.MeterReadingData;
import com.elster.jupiter.export.MeterReadingValidationData;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.kpi.KpiService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.IntervalBlock;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationResult;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

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
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StandardCsvDataFormatterTest {

    private static final long EPOCH_MILLI = 1416783612449L;
    @Rule
    public TestRule timeZone = Using.timeZoneOfMcMurdo();
    @Mock
    private DataExportService dataExportService;
    @Mock
    private KpiService kpiService;
    @Mock
    private MeteringService meteringService;
    @Mock
    private ValidationEvaluator validationEvaluator;
    @Mock
    private Channel channel;
    @Mock
    private AppService appService;
    @Mock
    private DataExportOccurrence dataExportOccurrence;
    @Mock
    private ReadingTypeDataExportItem item, item1;
    @Mock
    private MeterReading data, dataLoadProfile;
    @Mock
    private MeterReadingValidationData suspectData, notValidatedData;
    @Mock
    private Meter meter, meter1;
    @Mock
    private MeterActivation meterActivation;
    @Mock
    private ChannelsContainer channelsContainer;
    @Mock
    ReadingType readingType, readingType1;
    @Mock
    Logger logger;
    @Mock(extraInterfaces = IntervalReading.class)
    Reading reading, reading1, reading2;
    @Mock
    IntervalBlock intervalBlock;
    @Mock
    IntervalReading intervalReading, intervalReading1;
    @Mock
    DataExportProperty propertyExtension, propertyPrefix, propertySeparator, propertyExtensionUpdated, propertyPrefixUpdated, propertyUpdateSeparateFile, property, propertyPath;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    DataValidationStatus dataValidationStatus;
    @Mock
    private AppServer appServer;

    private StandardCsvDataFormatter processor;
    private List<DataExportProperty> properties;
    private Clock clock = Clock.system(ZoneId.systemDefault());

    @Before
    public void setUp() throws IOException {
        when(meterActivation.getChannelsContainer()).thenReturn(channelsContainer);
        FileSystem fileSystem = Jimfs.newFileSystem(Configuration.windows());
        Path root = fileSystem.getRootDirectories().iterator().next();
        Files.createTempDirectory(root, "tmp");

        when(channelsContainer.getChannels()).thenReturn(Collections.singletonList(channel));
        doReturn(Arrays.asList(readingType, readingType1)).when(channel).getReadingTypes();
        when(dataValidationStatus.getValidationResult()).thenReturn(ValidationResult.SUSPECT);

        TranslatablePropertyValueInfo semicolonValueInfo = new TranslatablePropertyValueInfo(FormatterProperties.SEPARATOR_SEMICOLON.getKey(), "Semicolon (;)");
        properties = Arrays.asList(propertyPrefix, propertyExtension, propertySeparator, propertyExtensionUpdated, propertyPrefixUpdated, propertyUpdateSeparateFile, propertyPath);
        when(propertyExtension.getName()).thenReturn("fileFormat.fileExtension");
        when(propertyExtension.getValue()).thenReturn("csv");
        when(propertyPrefix.getName()).thenReturn("fileFormat.filenamePrefix");
        when(propertyPrefix.getValue()).thenReturn("MainFile");
        when(propertySeparator.getName()).thenReturn(FormatterProperties.SEPARATOR.getKey());
        when(propertySeparator.getValue()).thenReturn(semicolonValueInfo);
        when(propertyExtensionUpdated.getName()).thenReturn("fileFormat.updatedData.updateFileExtension");
        when(propertyExtensionUpdated.getValue()).thenReturn("csv");
        when(propertyPrefixUpdated.getName()).thenReturn("fileFormat.updatedData.updateFilenamePrefix");
        when(propertyPrefixUpdated.getValue()).thenReturn("UpdateFile");
        when(propertyUpdateSeparateFile.getName()).thenReturn("fileFormat.updatedData.separateFile");
        when(propertyUpdateSeparateFile.getValue()).thenReturn("true");
        when(propertyPath.getName()).thenReturn("fileFormat.path");
        when(propertyPath.getValue()).thenReturn("c:\\export");
        when(dataExportOccurrence.getTriggerTime()).thenReturn(Instant.ofEpochMilli(EPOCH_MILLI));

        when(item.getDomainObject()).thenReturn(meter);
        when(item1.getDomainObject()).thenReturn(meter1);
        when(meter.getMRID()).thenReturn("MRMR");
        when(meter1.getMRID()).thenReturn("MRMRMR");
        when(meter.getName()).thenReturn("Device");
        when(meter1.getName()).thenReturn("AnotherDevice");
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

        when(suspectData.getValidationStatus(Instant.ofEpochMilli(EPOCH_MILLI))).thenReturn(dataValidationStatus);

        when(dataLoadProfile.getReadings()).thenReturn(Collections.emptyList());
        when(dataLoadProfile.getIntervalBlocks()).thenReturn(Collections.singletonList(intervalBlock));

        List<IntervalReading> intervals = Collections.unmodifiableList(Arrays.asList(intervalReading, intervalReading1));
        doReturn(intervals).when(intervalBlock).getIntervals();
        doReturn("0.0.5.1.16.1.12.0.0.0.0.0.0.0.0.3.73").when(intervalBlock).getReadingTypeCode();
        when(intervalReading.getTimeStamp()).thenReturn(Instant.ofEpochMilli(EPOCH_MILLI));
        when(intervalReading.getValue()).thenReturn(BigDecimal.ONE);
        when(intervalReading1.getTimeStamp()).thenReturn(Instant.ofEpochMilli(EPOCH_MILLI));
        when(intervalReading1.getValue()).thenReturn(BigDecimal.TEN);

        doReturn(Optional.of(readingType)).when(meteringService).getReadingType("0.0.5.1.16.1.12.0.0.0.0.0.0.0.0.3.73");

        when(appService.getAppServer()).thenReturn(Optional.of(appServer));
        when(dataExportService.getExportDirectory(appServer)).thenReturn(Optional.of(fileSystem.getPath("c:\\appserver\\export")));

        doAnswer(invocation -> TestDefaultStructureMarker.createRoot(clock, invocation.getArguments()[0].toString())).when(dataExportService).forRoot(any());
    }

    @Test
    public void testLinesAreOkForLoadProfile() {
        processor = new StandardCsvDataFormatter(getPropertyMap(properties), dataExportService);

        processor.startExport(dataExportOccurrence, logger);
        processor.startItem(item);
        FormattedData formattedData = processor.processData(Stream.of(new MeterReadingData(item, dataLoadProfile, notValidatedData, TestDefaultStructureMarker.createRoot(clock, "root"))));
        List<FormattedExportData> lines = formattedData.getData();
        processor.endItem(item);
        assertThat(lines).hasSize(2);
        assertThat(lines.get(0).getAppendablePayload()).isEqualTo("2014-11-24T12:00:12.449+13:00;MRMR;Device;0.0.5.1.16.1.12.0.0.0.0.0.0.0.0.3.73.0;1;\n");
        assertThat(lines.get(1).getAppendablePayload()).isEqualTo("2014-11-24T12:00:12.449+13:00;MRMR;Device;0.0.5.1.16.1.12.0.0.0.0.0.0.0.0.3.73.0;10;\n");

        processor.startExport(dataExportOccurrence, logger);
        processor.startItem(item1);
        formattedData = processor.processData(Stream.of(new MeterReadingData(item1, this.dataLoadProfile, notValidatedData, TestDefaultStructureMarker.createRoot(clock, "root"))));
        lines = formattedData.getData();
        processor.endItem(item1);
        assertThat(lines).hasSize(2);
        assertThat(lines.get(0).getAppendablePayload()).isEqualTo("2014-11-24T12:00:12.449+13:00;MRMRMR;AnotherDevice;0.0.5.1.17.1.13.0.0.0.0.0.0.0.0.4.75.1;1;\n");
        assertThat(lines.get(1).getAppendablePayload()).isEqualTo("2014-11-24T12:00:12.449+13:00;MRMRMR;AnotherDevice;0.0.5.1.17.1.13.0.0.0.0.0.0.0.0.4.75.1;10;\n");
    }

    @Test
    public void testLinesAreOk() {
        processor = new StandardCsvDataFormatter(getPropertyMap(properties), dataExportService);

        processor.startExport(dataExportOccurrence, logger);
        processor.startItem(item);
        FormattedData formattedData = processor.processData(Stream.of(new MeterReadingData(item, data, suspectData, TestDefaultStructureMarker.createRoot(clock, "root"))));
        List<FormattedExportData> lines = formattedData.getData();
        processor.endItem(item);
        assertThat(lines).hasSize(3);
        assertThat(lines.get(0).getAppendablePayload()).isEqualTo("2014-11-24T12:00:12.449+13:00;MRMR;Device;0.0.5.1.16.1.12.0.0.0.0.0.0.0.0.3.73.0;10;suspect\n");
        assertThat(lines.get(1).getAppendablePayload()).isEqualTo("2014-11-24T12:00:12.449+13:00;MRMR;Device;0.0.5.1.16.1.12.0.0.0.0.0.0.0.0.3.73.0;1;suspect\n");
        assertThat(lines.get(2).getAppendablePayload()).isEqualTo("2014-11-24T12:00:12.449+13:00;MRMR;Device;0.0.5.1.16.1.12.0.0.0.0.0.0.0.0.3.73.0;0;suspect\n");

        processor.startExport(dataExportOccurrence, logger);
        processor.startItem(item1);
        formattedData = processor.processData(Stream.of(new MeterReadingData(item1, this.data, suspectData, TestDefaultStructureMarker.createRoot(clock, "root"))));
        lines = formattedData.getData();
        processor.endItem(item1);
        assertThat(lines).hasSize(3);
        assertThat(lines.get(0).getAppendablePayload()).isEqualTo("2014-11-24T12:00:12.449+13:00;MRMRMR;AnotherDevice;0.0.5.1.17.1.13.0.0.0.0.0.0.0.0.4.75.1;10;suspect\n");
        assertThat(lines.get(1).getAppendablePayload()).isEqualTo("2014-11-24T12:00:12.449+13:00;MRMRMR;AnotherDevice;0.0.5.1.17.1.13.0.0.0.0.0.0.0.0.4.75.1;1;suspect\n");
        assertThat(lines.get(2).getAppendablePayload()).isEqualTo("2014-11-24T12:00:12.449+13:00;MRMRMR;AnotherDevice;0.0.5.1.17.1.13.0.0.0.0.0.0.0.0.4.75.1;0;suspect\n");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgumentException() {
        processor = new StandardCsvDataFormatter(getPropertyMap(properties), dataExportService);

        processor.startExport(dataExportOccurrence, logger);
        processor.startItem(item);
        processor.processData(Stream.of(new MeterReadingData(item, data, notValidatedData, TestDefaultStructureMarker.createRoot(clock, "root"))));
        processor.endItem(item1);
    }

    private Map<String, Object> getPropertyMap(List<DataExportProperty> properties) {
        Map<String, Object> propertyMap = new HashMap<>();
        for (DataExportProperty dataExportProperty : properties) {
            propertyMap.put(dataExportProperty.getName(), dataExportProperty.getValue());
        }
        return propertyMap;
    }
}
