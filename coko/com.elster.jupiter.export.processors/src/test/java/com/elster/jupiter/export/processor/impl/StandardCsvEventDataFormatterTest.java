/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.processor.impl;

import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.FormattedData;
import com.elster.jupiter.export.MeterEventData;
import com.elster.jupiter.export.TextLineExportData;
import com.elster.jupiter.metering.readings.beans.EndDeviceEventImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StandardCsvEventDataFormatterTest {

    @Rule
    public TestRule mcMurdo = Using.timeZoneOfMcMurdo();

    private static final ZonedDateTime time1 = ZonedDateTime.of(2014, 3, 13, 15, 42, 0, 0, TimeZoneNeutral.getMcMurdo());
    private static final ZonedDateTime time2 = ZonedDateTime.of(2014, 4, 13, 15, 42, 0, 0, TimeZoneNeutral.getMcMurdo());
    private static final ZonedDateTime time3 = ZonedDateTime.of(2014, 5, 13, 15, 42, 0, 0, TimeZoneNeutral.getMcMurdo());

    @Mock
    private DataExportService dataExportService;
    @Mock
    private Clock clock;

    @Before
    public void setUp() {
        when(dataExportService.forRoot(anyString())).thenAnswer(invocation -> TestDefaultStructureMarker.createRoot(clock, invocation.getArguments()[0].toString()));
    }

    @Test
    public void test() {
        TranslatablePropertyValueInfo translatablePropertyValueInfo = new TranslatablePropertyValueInfo(FormatterProperties.SEPARATOR_COMMA.getKey(), "Comma (,)");
        StandardCsvEventDataFormatter standardCsvEventDataFormatter = StandardCsvEventDataFormatter.from(dataExportService, translatablePropertyValueInfo, "Tag");

        MeterReadingImpl meterReading1 = MeterReadingImpl.newInstance();
        meterReading1.addEndDeviceEvent(EndDeviceEventImpl.of("1.2.3.4", time1.toInstant()));
        meterReading1.addEndDeviceEvent(EndDeviceEventImpl.of("2.2.3.4", time2.toInstant()));
        MeterEventData meterEventData1 = new MeterEventData(meterReading1, TestDefaultStructureMarker.createRoot(clock, "MRID1").child("Device1"));
        MeterReadingImpl meterReading2 = MeterReadingImpl.newInstance();
        meterReading2.addEndDeviceEvent(EndDeviceEventImpl.of("3.2.3.4", time3.toInstant()));
        MeterEventData meterEventData2 = new MeterEventData(meterReading2, TestDefaultStructureMarker.createRoot(clock, "MRID2").child("Device2"));

        FormattedData formattedData = standardCsvEventDataFormatter.processData(Stream.of(meterEventData1, meterEventData2));

        assertThat(formattedData.getData()).hasSize(3);

        assertThat(formattedData.getData().get(0)).isInstanceOf(TextLineExportData.class);
        TextLineExportData textLine1 = (TextLineExportData) formattedData.getData().get(0);
        assertThat(textLine1.getAppendablePayload()).isEqualTo("2014-03-13T15:42:00.000+13:00,1.2.3.4,MRID1,Device1\n");
        assertThat(textLine1.getStructureMarker()).isEqualTo(TestDefaultStructureMarker.createRoot(clock, "Tag").child("MRID1").child("Device1"));

        TextLineExportData textLine2 = (TextLineExportData) formattedData.getData().get(1);
        assertThat(textLine2.getAppendablePayload()).isEqualTo("2014-04-13T15:42:00.000+12:00,2.2.3.4,MRID1,Device1\n");
        assertThat(textLine2.getStructureMarker()).isEqualTo(TestDefaultStructureMarker.createRoot(clock, "Tag").child("MRID1").child("Device1"));

        TextLineExportData textLine3 = (TextLineExportData) formattedData.getData().get(2);
        assertThat(textLine3.getAppendablePayload()).isEqualTo("2014-05-13T15:42:00.000+12:00,3.2.3.4,MRID2,Device2\n");
        assertThat(textLine3.getStructureMarker()).isEqualTo(TestDefaultStructureMarker.createRoot(clock, "Tag").child("MRID2").child("Device2"));
    }
}
