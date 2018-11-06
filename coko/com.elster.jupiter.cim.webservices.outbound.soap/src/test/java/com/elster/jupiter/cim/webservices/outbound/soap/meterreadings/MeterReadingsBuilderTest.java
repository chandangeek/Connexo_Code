/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.outbound.soap.meterreadings;

import com.elster.jupiter.metering.AggregatedChannel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingInfoType;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.UsagePoint;

import ch.iec.tc57._2011.meterreadings.MeterReading;
import ch.iec.tc57._2011.meterreadings.MeterReadings;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MeterReadingsBuilderTest extends SendMeterReadingsTest {

    @Mock
    private AggregatedChannel.AggregatedIntervalReadingRecord dailyReading1, dailyReading2;
    @Mock
    private ReadingInfoType readingInfoType1, readingInfoType2;
    @Mock
    private Meter anotherMeter;
    @Mock
    private UsagePoint anotherUsagePoint;
    @Mock
    private ReadingQualityRecord inferred;

    private static Instant day(int n) {
        return JAN_1ST.withDayOfMonth(n).toInstant();
    }

    @Before
    public void setUp() throws Exception {
        when(clock.instant()).thenReturn(JAN_1ST.toInstant());
        mockMeter();
        mockUsagePoint();
        mockReadingsInfoType(readingInfoType1, dailyReadingType, dailyReading1);
        mockReadingType(dailyReadingType, DAILY_MRID, DAILY_FULL_ALIAS_NAME, true);
        when(dailyReading1.getReadingType()).thenReturn(dailyReadingType);
        listReadingInfoType.add(readingInfoType1);
        when(readingStorer.getReadings()).thenReturn(listReadingInfoType);
        mockIntervalReadings();
        mockReadingQualities();
    }

    private void mockIntervalReadings() {
        mockIntervalReading(dailyReading1, Range.openClosed(JAN_1ST.minusDays(1).toInstant(), JAN_1ST.toInstant()), 1.05);
        mockIntervalReading(dailyReading2, Range.openClosed(JAN_1ST.minusDays(2).toInstant(), JAN_1ST.toInstant()), 1.05);
    }

    private void mockReadingQualities() {
        mockReadingQuality(inferred, INFERRED, day(2), null);
    }

    @Test
    public void testBuild() throws Exception {
        MeterReadingsBuilder builder = new MeterReadingsBuilder();
        MeterReadings meterReadings = builder.build(readingStorer);
        List<MeterReading> readings = meterReadings.getMeterReading();
        assertThat(readings).hasSize(1);
        assertThat(readings.get(0).getMeter().getMRID()).isEqualTo(METER_MRID);
        assertThat(readings.get(0).getUsagePoint().getMRID()).isEqualTo(USAGE_POINT_MRID);
    }

    @Test
    public void testMeterNotTheSame() throws Exception {
        mockReadingsInfoType(readingInfoType2, dailyReadingType, dailyReading2);
        when(readingInfoType2.getMeter()).thenReturn(anotherMeter);
        when(dailyReading2.getReadingType()).thenReturn(dailyReadingType);
        listReadingInfoType.add(readingInfoType2);

        MeterReadingsBuilder builder = new MeterReadingsBuilder();

        expectedException.expect(MeterReadinsServiceException.class);
        expectedException.expectMessage(MessageSeeds.READINGS_METER_IS_NOT_THE_SAME.getDefaultFormat());

        builder.build(readingStorer);
    }

    @Test
    public void testUsagePointNotTheSame() throws Exception {
        mockReadingsInfoType(readingInfoType2, dailyReadingType, dailyReading2);
        when(readingInfoType2.getUsagePoint()).thenReturn(anotherUsagePoint);
        when(dailyReading2.getReadingType()).thenReturn(dailyReadingType);
        listReadingInfoType.add(readingInfoType2);

        MeterReadingsBuilder builder = new MeterReadingsBuilder();

        expectedException.expect(MeterReadinsServiceException.class);
        expectedException.expectMessage(MessageSeeds.READINGS_USAGE_POINT_IS_NOT_THE_SAME.getDefaultFormat());

        builder.build(readingStorer);
    }
}