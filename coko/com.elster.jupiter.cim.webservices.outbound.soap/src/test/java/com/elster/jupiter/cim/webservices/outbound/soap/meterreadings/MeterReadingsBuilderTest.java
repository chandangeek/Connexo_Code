/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.outbound.soap.meterreadings;

import com.elster.jupiter.metering.AggregatedChannel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingInfo;
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
    private ReadingInfo readingInfo1, readingInfo2;
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
        mockReadingsInfoType(readingInfo1, dailyReadingType, dailyReading1);
        mockReadingType(dailyReadingType, DAILY_MRID, DAILY_FULL_ALIAS_NAME, true);
        when(dailyReading1.getReadingType()).thenReturn(dailyReadingType);
        listReadingInfo.add(readingInfo1);
        when(readingStorer.getReadings()).thenReturn(listReadingInfo);
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
    public void testBuild() {
        MeterReadingsBuilder builder = new MeterReadingsBuilder();
        MeterReadings meterReadings = builder.build(listReadingInfo);
        List<MeterReading> readings = meterReadings.getMeterReading();
        assertThat(readings).hasSize(1);
        assertThat(readings.get(0).getMeter().getMRID()).isEqualTo(METER_MRID);
        assertThat(readings.get(0).getUsagePoint().getMRID()).isEqualTo(USAGE_POINT_MRID);
    }
}