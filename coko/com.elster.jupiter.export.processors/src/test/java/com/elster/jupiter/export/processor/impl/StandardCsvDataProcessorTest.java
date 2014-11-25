package com.elster.jupiter.export.processor.impl;

import com.elster.jupiter.export.DataExportException;
import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.DataExportProperty;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.metering.*;
import com.elster.jupiter.metering.readings.*;
import com.elster.jupiter.nls.Thesaurus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
public class StandardCsvDataProcessorTest {

    @Mock
    DataExportOccurrence dataExportOccurrence;

    @Mock
    ReadingTypeDataExportItem item;

    @Mock
    ReadingTypeDataExportItem item1;

    @Mock
    MeterReading data;

    @Mock
    MeterReading dataLoadProfile;

    @Mock
    Meter meter;

    @Mock
    Meter meter1;

    @Mock
    MeterActivation meterActivation;

    @Mock
    ReadingType readingType;

    @Mock
    ReadingType readingType1;

    @Mock
    Logger logger;

    @Mock
    Reading reading;

    @Mock
    Reading reading1;

    @Mock
    Reading reading2;

    @Mock
    IntervalBlock intervalBlock;

    @Mock
    DataExportProperty propertyExtension;

    @Mock
    DataExportProperty propertyPrefix;

    @Mock
    DataExportProperty propertySeparator;

    @Mock
    DataExportProperty propertyExtensionUpdated;

    @Mock
    DataExportProperty propertyPrefixUpdated;

    @Mock
    DataExportProperty propertyUpdateSeparateFile;

    @Mock
    DataExportProperty property;

    @Mock
    ReadingQualityRecord readingQuality;

    @Mock
    ReadingQualityRecord readingQuality1;

    @Mock
    IntervalReading intervalReading;

    @Mock
    IntervalReading intervalReading1;

    @Mock
    ReadingContainer readingContainer;

    @Mock
    ReadingContainer readingContainer1;

    @Mock
    private Thesaurus thesaurus;

    StandardCsvDataProcessor processor;

    @Before
    public void setUp() {
        List<DataExportProperty> properties = Arrays.asList(propertyPrefix, propertyExtension, propertySeparator, propertyExtensionUpdated, propertyPrefixUpdated, propertyUpdateSeparateFile);
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
        processor = Mockito.spy(new StandardCsvDataProcessor(properties, thesaurus));

        when(dataExportOccurrence.getTriggerTime()).thenReturn(Instant.ofEpochMilli(1416783612449L));

        when(item.getReadingContainer()).thenReturn(readingContainer);
        when(readingContainer.getMeter(Instant.ofEpochMilli(1416783612449L))).thenReturn(Optional.of(meter));
        when(item1.getReadingContainer()).thenReturn(readingContainer1);
        when(readingContainer1.getMeter(Instant.ofEpochMilli(1416783612449L))).thenReturn(Optional.of(meter1));
        when(meter.getMRID()).thenReturn("DeviceMRID");
        when(meter1.getMRID()).thenReturn("AnotherDeviceMRID");
        when(item.getReadingType()).thenReturn(readingType);
        when(item1.getReadingType()).thenReturn(readingType1);
        when(readingType.getMRID()).thenReturn("0.0.5.1.16.1.12.0.0.0.0.0.0.0.0.3.73.0");
        when(readingType1.getMRID()).thenReturn("0.0.5.1.17.1.13.0.0.0.0.0.0.0.0.4.75.1");

        when(data.getReadings()).thenReturn(Arrays.asList(reading, reading1, reading2));
        when(reading.getTimeStamp()).thenReturn(Instant.ofEpochMilli(1416783612449L));
        when(reading.getValue()).thenReturn(BigDecimal.TEN);

        when(reading1.getTimeStamp()).thenReturn(Instant.ofEpochMilli(1416783612449L));
        when(reading1.getValue()).thenReturn(BigDecimal.ONE);

        when(reading2.getTimeStamp()).thenReturn(Instant.ofEpochMilli(1416783612449L));
        when(reading2.getValue()).thenReturn(BigDecimal.ZERO);

        when(dataLoadProfile.getReadings()).thenReturn(Arrays.asList());
        when(dataLoadProfile.getIntervalBlocks()).thenReturn(Arrays.asList(intervalBlock));

        List<IntervalReading> intervals = Collections.unmodifiableList(Arrays.asList(intervalReading, intervalReading1));
        doReturn(intervals).when(intervalBlock).getIntervals();
        when(intervalReading.getTimeStamp()).thenReturn(Instant.ofEpochMilli(1416783612449L));
        when(intervalReading.getValue()).thenReturn(BigDecimal.ONE);
        when(intervalReading1.getTimeStamp()).thenReturn(Instant.ofEpochMilli(1416783612449L));
        when(intervalReading1.getValue()).thenReturn(BigDecimal.TEN);

        List<? extends ReadingQuality> list = Collections.unmodifiableList(Arrays.asList(readingQuality, readingQuality1));
        doReturn(list).when(reading).getReadingQualities();
        doReturn(list).when(reading1).getReadingQualities();
        doReturn(list).when(reading2).getReadingQualities();
        when(readingQuality.getTypeCode()).thenReturn("3.5.259");
        when(readingQuality1.getTypeCode()).thenReturn("3.5.258");

        doReturn(list).when(intervalReading).getReadingQualities();
        doReturn(Arrays.asList()).when(intervalReading1).getReadingQualities();
    }


    @Test
    public  void testExportToCsv() {
        processor.startExport(dataExportOccurrence, logger);
        processor.startItem(item);
        processor.processData(data);
        processor.endItem(item);
        processor.startItem(item1);
        processor.processData(dataLoadProfile);
        processor.endItem(item1);
        processor.endExport();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        // TODO correct ZoneId
        ZonedDateTime date = ZonedDateTime.ofInstant(Instant.ofEpochMilli(1416783612449L), ZoneId.systemDefault());
        StringBuilder fileName = new StringBuilder("MainFile").append('_').append(date.format(formatter)).append('.').append("csv");
        File file = new File(fileName.toString());
        assertThat(file.exists()).isTrue();
        StringBuilder fileNameUpdated = new StringBuilder("UpdateFile").append('_').append(formatter.format(date)).append('.').append("csv");
        File updatedFile = new File(fileNameUpdated.toString());
        assertThat(updatedFile.exists()).isTrue();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgumentException() {
        processor.startExport(dataExportOccurrence, logger);
        processor.startItem(item);
        processor.processData(data);
        processor.endItem(item1);
    }

    @Test(expected = DataExportException.class)
    public void test() {
        when(readingContainer.getMeter(Instant.ofEpochMilli(1416783612449L))).thenReturn(Optional.empty());
        processor.startExport(dataExportOccurrence, logger);
        processor.startItem(item);
        processor.processData(data);
    }

}
