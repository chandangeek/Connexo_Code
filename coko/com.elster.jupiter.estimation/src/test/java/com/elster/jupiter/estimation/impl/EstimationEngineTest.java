/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.tests.MockitoExtension;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingQualityWithTypeFetcher;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;

import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EstimationEngineTest {

    @Rule
    public TestRule mcMurdo = Using.timeZoneOfMcMurdo();

    @Mock
    private MeterActivation meterActivation;
    @Mock
    private ChannelsContainer channelsContainer;
    @Mock
    private ReadingType readingType, readingType2;
    @Mock
    private Channel channel1;
    @Mock
    private ReadingQualityRecord readingQualityRecord1, readingQualityRecord2, readingQualityRecord3, readingQualityRecord4, readingQualityRecord5;
    @Mock
    private BaseReadingRecord baseReadingRecord1, baseReadingRecord2, baseReadingRecord3, baseReadingRecord4, baseReadingRecord5;
    @Mock
    private CimChannel cimChannel1;
    @Mock
    private ReadingQualityWithTypeFetcher fetcher;

    @Before
    public void setUp() {
        when(meterActivation.getChannelsContainer()).thenReturn(channelsContainer);
        when(channelsContainer.getChannels()).thenReturn(Collections.singletonList(channel1));
        doReturn(Collections.singletonList(readingType)).when(channel1).getReadingTypes();
        when(channel1.isRegular()).thenReturn(true);
        when(channel1.getIntervalLength()).thenReturn(Optional.of(Duration.ofHours(1)));
        ZonedDateTime first = ZonedDateTime.of(2010, 8, 14, 9, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
        when(readingQualityRecord1.getReadingTimestamp()).thenReturn(first.toInstant());
        when(readingQualityRecord2.getReadingTimestamp()).thenReturn(first.plusHours(1).toInstant());
        when(readingQualityRecord3.getReadingTimestamp()).thenReturn(first.plusHours(2).toInstant());
        when(readingQualityRecord4.getReadingTimestamp()).thenReturn(first.plusHours(3).toInstant());
        when(readingQualityRecord5.getReadingTimestamp()).thenReturn(first.plusHours(4).toInstant());
        when(readingQualityRecord1.getBaseReadingRecord()).thenReturn(Optional.of(baseReadingRecord1));
        when(readingQualityRecord2.getBaseReadingRecord()).thenReturn(Optional.of(baseReadingRecord2));
        when(readingQualityRecord3.getBaseReadingRecord()).thenReturn(Optional.of(baseReadingRecord3));
        when(readingQualityRecord4.getBaseReadingRecord()).thenReturn(Optional.of(baseReadingRecord4));
        when(readingQualityRecord5.getBaseReadingRecord()).thenReturn(Optional.of(baseReadingRecord5));
        when(baseReadingRecord1.getTimeStamp()).thenReturn(first.toInstant());
        when(baseReadingRecord2.getTimeStamp()).thenReturn(first.plusHours(1).toInstant());
        when(baseReadingRecord3.getTimeStamp()).thenReturn(first.plusHours(2).toInstant());
        when(baseReadingRecord4.getTimeStamp()).thenReturn(first.plusHours(3).toInstant());
        when(baseReadingRecord5.getTimeStamp()).thenReturn(first.plusHours(4).toInstant());
        when(channel1.getNextDateTime(any())).thenAnswer(invocation -> ((Instant) invocation.getArguments()[0]).plus(Duration.ofHours(1)));
        when(channel1.getCimChannel(readingType)).thenReturn(Optional.of(cimChannel1));
        when(channel1.findReadingQualities()).thenReturn(fetcher);
        when(cimChannel1.findReadingQualities()).thenReturn(fetcher);
        when(fetcher.ofQualitySystems(anySetOf(QualityCodeSystem.class))).thenReturn(fetcher);
        when(fetcher.ofQualityIndex(any(QualityCodeIndex.class))).thenReturn(fetcher);
        when(fetcher.inTimeInterval(Range.all())).thenReturn(fetcher);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testFindBlocksWhenThereAreNoSuspects() {
        List<EstimationBlock> blocksToEstimate = new EstimationEngine().findBlocksToEstimate(QualityCodeSystem.MDC, meterActivation
                .getChannelsContainer(), Range.all(), readingType);
        assertThat(blocksToEstimate).isEmpty();
        verify(cimChannel1).findReadingQualities();
        verify(fetcher, MockitoExtension.and(atLeastOnce(), MockitoExtension.neverWithOtherArguments()))
                .ofQualitySystems(Collections.singleton(QualityCodeSystem.MDC));
        verify(fetcher, MockitoExtension.and(atLeastOnce(), MockitoExtension.neverWithOtherArguments()))
                .ofQualityIndex(QualityCodeIndex.SUSPECT);
        verify(fetcher, MockitoExtension.and(atLeastOnce(), MockitoExtension.neverWithOtherArguments()))
                .inTimeInterval(Range.all());
    }

    @Test
    public void testFindBlocksWhenThereIsOneSuspectForMissing() {
        when(fetcher.collect()).thenReturn(Collections.singletonList(readingQualityRecord2));
        when(readingQualityRecord2.getBaseReadingRecord()).thenReturn(Optional.empty());

        List<EstimationBlock> blocksToEstimate = new EstimationEngine().findBlocksToEstimate(QualityCodeSystem.MDC, meterActivation
                .getChannelsContainer(), Range.all(), readingType);

        assertThat(blocksToEstimate).hasSize(1);

        EstimationBlock estimationBlock = blocksToEstimate.get(0);

        assertThat(estimationBlock.estimatables()).hasSize(1);

        Estimatable estimatable = estimationBlock.estimatables().get(0);

        assertThat(estimatable.getTimestamp()).isEqualTo(readingQualityRecord2.getReadingTimestamp());
        assertThat(estimatable).isInstanceOf(MissingReadingRecordEstimatable.class);
    }

    @Test
    public void testFindBlocksWhenThereIsOneSuspectForReading() {
        when(fetcher.collect()).thenReturn(Collections.singletonList(readingQualityRecord2));

        List<EstimationBlock> blocksToEstimate = new EstimationEngine().findBlocksToEstimate(QualityCodeSystem.MDC, meterActivation
                .getChannelsContainer(), Range.all(), readingType);

        assertThat(blocksToEstimate).hasSize(1);

        EstimationBlock estimationBlock = blocksToEstimate.get(0);

        assertThat(estimationBlock.estimatables()).hasSize(1);

        Estimatable estimatable = estimationBlock.estimatables().get(0);

        assertThat(estimatable.getTimestamp()).isEqualTo(readingQualityRecord2.getReadingTimestamp());
        assertThat(estimatable).isInstanceOf(BaseReadingRecordEstimatable.class);
    }

    @Test
    public void testFindBlocksWhenThereIsOneBlockOfSuspectForReading() {
        when(fetcher.collect()).thenReturn(Arrays.asList(readingQualityRecord2, readingQualityRecord3, readingQualityRecord4));

        List<EstimationBlock> blocksToEstimate = new EstimationEngine().findBlocksToEstimate(QualityCodeSystem.MDC, meterActivation
                .getChannelsContainer(), Range.all(), readingType);

        assertThat(blocksToEstimate).hasSize(1);

        EstimationBlock estimationBlock = blocksToEstimate.get(0);

        assertThat(estimationBlock.estimatables()).hasSize(3);

        assertThat(estimationBlock.estimatables().get(0).getTimestamp()).isEqualTo(readingQualityRecord2.getReadingTimestamp());
        assertThat(estimationBlock.estimatables().get(1).getTimestamp()).isEqualTo(readingQualityRecord3.getReadingTimestamp());
        assertThat(estimationBlock.estimatables().get(2).getTimestamp()).isEqualTo(readingQualityRecord4.getReadingTimestamp());
    }

    @Test
    public void testApplyEstimations() {
        EstimationReportImpl report = new EstimationReportImpl();

        EstimationBlock estimationBlock1 = mock(EstimationBlock.class);
        Estimatable estimatable1 = mock(Estimatable.class);
        ZonedDateTime start = ZonedDateTime.of(2000, 4, 3, 14, 50, 15, 0, ZoneId.systemDefault());
        when(estimatable1.getTimestamp()).thenReturn(start.plusHours(1).toInstant());
        when(estimatable1.getEstimation()).thenReturn(BigDecimal.valueOf(41));
        Estimatable estimatable2 = mock(Estimatable.class);
        when(estimatable2.getTimestamp()).thenReturn(start.plusHours(2).toInstant());
        when(estimatable2.getEstimation()).thenReturn(BigDecimal.valueOf(42));
        doReturn(Arrays.asList(estimatable1, estimatable2)).when(estimationBlock1).estimatables();
        EstimationBlock estimationBlock2 = mock(EstimationBlock.class);
        Estimatable estimatable3 = mock(Estimatable.class);
        when(estimatable3.getTimestamp()).thenReturn(start.plusHours(3).toInstant());
        when(estimatable3.getEstimation()).thenReturn(BigDecimal.valueOf(43));
        Estimatable estimatable4 = mock(Estimatable.class);
        when(estimatable4.getTimestamp()).thenReturn(start.plusHours(4).toInstant());
        when(estimatable4.getEstimation()).thenReturn(BigDecimal.valueOf(44));
        doReturn(Arrays.asList(estimatable3, estimatable4)).when(estimationBlock2).estimatables();
        EstimationBlock estimationBlock3 = mock(EstimationBlock.class);
        Estimatable estimatable5 = mock(Estimatable.class);
        when(estimatable5.getTimestamp()).thenReturn(start.plusHours(5).toInstant());
        when(estimatable5.getEstimation()).thenReturn(BigDecimal.valueOf(45));
        Estimatable estimatable6 = mock(Estimatable.class);
        when(estimatable6.getTimestamp()).thenReturn(start.plusHours(6).toInstant());
        when(estimatable6.getEstimation()).thenReturn(BigDecimal.valueOf(46));
        doReturn(Arrays.asList(estimatable5, estimatable6)).when(estimationBlock3).estimatables();

        when(estimationBlock1.getReadingType()).thenReturn(readingType);
        when(estimationBlock2.getReadingType()).thenReturn(readingType2);
        when(estimationBlock3.getReadingType()).thenReturn(readingType);
        when(estimationBlock1.getReadingQualityType()).thenReturn(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeCategory.ESTIMATED, 1));
        when(estimationBlock2.getReadingQualityType()).thenReturn(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeCategory.ESTIMATED, 2));
        when(estimationBlock3.getReadingQualityType()).thenReturn(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeCategory.ESTIMATED, 3));
        when(estimationBlock1.getChannel()).thenReturn(channel1);
        when(estimationBlock2.getChannel()).thenReturn(channel1);
        when(estimationBlock3.getChannel()).thenReturn(channel1);
        CimChannel cimChannel1 = mock(CimChannel.class);
        when(cimChannel1.getChannel()).thenReturn(channel1);
        when(cimChannel1.getReadingType()).thenReturn(readingType);
        CimChannel cimChannel2 = mock(CimChannel.class);
        when(cimChannel2.getChannel()).thenReturn(channel1);
        when(cimChannel2.getReadingType()).thenReturn(readingType2);
        when(channel1.getCimChannel(readingType)).thenReturn(Optional.of(cimChannel1));
        when(channel1.getCimChannel(readingType2)).thenReturn(Optional.of(cimChannel2));

        report.reportEstimated(readingType, estimationBlock1);
        report.reportEstimated(readingType2, estimationBlock2);
        report.reportEstimated(readingType, estimationBlock3);

        new EstimationEngine().applyEstimations(QualityCodeSystem.MDC, report);

        ArgumentCaptor<List> readingCaptor = ArgumentCaptor.forClass(List.class);
        verify(cimChannel1).estimateReadings(eq(QualityCodeSystem.MDC), readingCaptor.capture());

        assertThat(readingCaptor.getValue()).hasSize(4);
        List list = readingCaptor.getValue();
        BaseReading reading1 = (BaseReading) list.get(0);
        assertThat(reading1.getValue()).isEqualTo(BigDecimal.valueOf(41));
        assertThat(reading1.getTimeStamp()).isEqualTo(start.plusHours(1).toInstant());
        assertThat(reading1.getReadingQualities()).hasSize(1);
        assertThat(reading1.getReadingQualities().get(0).getType()).isEqualTo(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeCategory.ESTIMATED, 1));
        BaseReading reading2 = (BaseReading) list.get(1);
        assertThat(reading2.getValue()).isEqualTo(BigDecimal.valueOf(42));
        assertThat(reading2.getTimeStamp()).isEqualTo(start.plusHours(2).toInstant());
        assertThat(reading2.getReadingQualities()).hasSize(1);
        assertThat(reading2.getReadingQualities().get(0).getType()).isEqualTo(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeCategory.ESTIMATED, 1));
        BaseReading reading3 = (BaseReading) list.get(2);
        assertThat(reading3.getValue()).isEqualTo(BigDecimal.valueOf(45));
        assertThat(reading3.getTimeStamp()).isEqualTo(start.plusHours(5).toInstant());
        assertThat(reading3.getReadingQualities()).hasSize(1);
        assertThat(reading3.getReadingQualities().get(0).getType()).isEqualTo(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeCategory.ESTIMATED, 3));
        BaseReading reading4 = (BaseReading) list.get(3);
        assertThat(reading4.getValue()).isEqualTo(BigDecimal.valueOf(46));
        assertThat(reading4.getTimeStamp()).isEqualTo(start.plusHours(6).toInstant());
        assertThat(reading4.getReadingQualities()).hasSize(1);
        assertThat(reading4.getReadingQualities().get(0).getType()).isEqualTo(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeCategory.ESTIMATED, 3));

        verify(cimChannel2).estimateReadings(eq(QualityCodeSystem.MDC), readingCaptor.capture());

        assertThat(readingCaptor.getValue()).hasSize(2);
        list = readingCaptor.getValue();
        reading1 = (BaseReading) list.get(0);
        assertThat(reading1.getValue()).isEqualTo(BigDecimal.valueOf(43));
        assertThat(reading1.getTimeStamp()).isEqualTo(start.plusHours(3).toInstant());
        assertThat(reading1.getReadingQualities()).hasSize(1);
        assertThat(reading1.getReadingQualities().get(0).getType()).isEqualTo(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeCategory.ESTIMATED, 2));
        reading2 = (BaseReading) list.get(1);
        assertThat(reading2.getValue()).isEqualTo(BigDecimal.valueOf(44));
        assertThat(reading2.getTimeStamp()).isEqualTo(start.plusHours(4).toInstant());
        assertThat(reading2.getReadingQualities()).hasSize(1);
        assertThat(reading2.getReadingQualities().get(0).getType()).isEqualTo(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeCategory.ESTIMATED, 2));
    }
}
