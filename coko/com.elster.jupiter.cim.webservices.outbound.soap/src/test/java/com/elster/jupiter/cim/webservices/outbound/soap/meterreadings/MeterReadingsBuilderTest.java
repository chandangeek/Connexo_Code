/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.cim.webservices.outbound.soap.meterreadings;

import ch.iec.tc57._2011.meterreadings.MeterReading;
import ch.iec.tc57._2011.meterreadings.MeterReadings;
import com.elster.jupiter.export.MeterReadingData;
import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.metering.AggregatedChannel;
import com.elster.jupiter.metering.ReadingInfo;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.IntervalBlock;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.Reading;
import com.google.common.collect.Range;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MeterReadingsBuilderTest extends SendMeterReadingsTest {

    private static final long EPOCH_MILLI = 1594339824346L;

    @Mock
    private AggregatedChannel.AggregatedIntervalReadingRecord dailyReading1, dailyReading2;
    @Mock
    private ReadingInfo readingInfo1;
    @Mock
    private ReadingQualityRecord inferred;
    @Mock
    private ReadingTypeDataExportItem item;
    @Mock
    private ReadingType exportReadingType;
    @Mock
    private com.elster.jupiter.metering.readings.MeterReading dataLoadProfile;
    @Mock
    private IntervalBlock intervalBlock;
    @Mock
    private IntervalReading intervalReading, intervalReading1;
    @Mock
    private Reading reading;


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

    @Test
    public void testBuildReadingData() {
        mockReadingType(exportReadingType, DAILY_MRID, DAILY_FULL_ALIAS_NAME, true);
        when(item.getReadingType()).thenReturn(exportReadingType);
        when(item.getDomainObject()).thenReturn(meter);

        when(dataLoadProfile.getReadings()).thenReturn(Collections.singletonList(reading));
        when(reading.getReadingTypeCode()).thenReturn(DAILY_MRID);
        when(reading.getValue()).thenReturn(BigDecimal.ZERO);
        when(reading.getTimeStamp()).thenReturn(Instant.ofEpochMilli(EPOCH_MILLI).plusSeconds(5000));
        when(reading.getTimePeriod()).thenReturn(Optional.of(Range.all()));

        when(dataLoadProfile.getIntervalBlocks()).thenReturn(Collections.singletonList(intervalBlock));
        List<IntervalReading> intervals = Collections.unmodifiableList(Arrays.asList(intervalReading, intervalReading1));
        doReturn(intervals).when(intervalBlock).getIntervals();
        doReturn(DAILY_MRID).when(intervalBlock).getReadingTypeCode();
        when(intervalReading.getTimeStamp()).thenReturn(Instant.ofEpochMilli(EPOCH_MILLI));
        when(intervalReading.getValue()).thenReturn(BigDecimal.ONE);
        when(intervalReading1.getTimeStamp()).thenReturn(Instant.ofEpochMilli(EPOCH_MILLI).minusSeconds(1000));
        when(intervalReading1.getValue()).thenReturn(BigDecimal.TEN);

        MeterReadingsBuilder builder = new MeterReadingsBuilder();
        List<MeterReadingData> readingDataList = Collections.singletonList(new MeterReadingData(item, dataLoadProfile, null, null, TestDefaultStructureMarker.createRoot(clock, "update")));
        MeterReadings meterReadings = builder.buildReadingData(readingDataList);
        List<MeterReading> readings = meterReadings.getMeterReading();
        assertThat(readings).hasSize(1);
        assertThat(readings.get(0).getMeter().getMRID()).isEqualTo(METER_MRID);
        assertThat(readings.get(0).getMeter().getNames().stream().anyMatch(n -> n.getName().equals(METER_NAME))).isTrue();
        assertThat(readings.get(0).getReadings()).hasSize(1);
        assertThat(readings.get(0).getReadings().get(0).getTimeStamp()).isEqualTo(Instant.ofEpochMilli(EPOCH_MILLI).plusSeconds(5000));
        assertThat(readings.get(0).getReadings().get(0).getValue()).isEqualTo("" + BigDecimal.ZERO);
        assertThat(readings.get(0).getIntervalBlocks()).hasSize(1);
        assertThat(readings.get(0).getIntervalBlocks().get(0).getIntervalReadings()).hasSize(2);
        assertThat(readings.get(0).getIntervalBlocks().get(0).getIntervalReadings().get(0).getTimeStamp()).isEqualTo(Instant.ofEpochMilli(EPOCH_MILLI).minusSeconds(1000));
        assertThat(readings.get(0).getIntervalBlocks().get(0).getIntervalReadings().get(0).getValue()).isEqualTo("" + BigDecimal.TEN);
        assertThat(readings.get(0).getIntervalBlocks().get(0).getIntervalReadings().get(1).getTimeStamp()).isEqualTo(Instant.ofEpochMilli(EPOCH_MILLI));
        assertThat(readings.get(0).getIntervalBlocks().get(0).getIntervalReadings().get(1).getValue()).isEqualTo("" + BigDecimal.ONE);
    }
}