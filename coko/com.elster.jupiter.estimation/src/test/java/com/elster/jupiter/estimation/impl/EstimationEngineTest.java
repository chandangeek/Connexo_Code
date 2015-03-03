package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.google.common.collect.Range;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EstimationEngineTest {

    @Rule
    public TestRule mcMurdo = Using.timeZoneOfMcMurdo();

    public static final ReadingQualityType SUSPECT = ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT);
    @Mock
    private MeterActivation meterActivation;
    @Mock
    private ReadingType readingType;
    @Mock
    private Channel channel1;
    @Mock
    private ReadingQualityRecord readingQualityRecord1, readingQualityRecord2, readingQualityRecord3, readingQualityRecord4, readingQualityRecord5;
    @Mock
    private BaseReadingRecord baseReadingRecord1, baseReadingRecord2, baseReadingRecord3, baseReadingRecord4, baseReadingRecord5;

    @Before
    public void setUp() {
        when(meterActivation.getChannels()).thenReturn(Arrays.asList(channel1));
        doReturn(Arrays.asList(readingType)).when(channel1).getReadingTypes();
        when(channel1.isRegular()).thenReturn(true);
        when(channel1.getIntervalLength()).thenReturn(Optional.of(Duration.ofHours(1)));
        when(channel1.findReadingQuality(SUSPECT, Range.<Instant>all())).thenReturn(Collections.emptyList());
        ZonedDateTime first = ZonedDateTime.of(2010, 8, 14, 9, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
        when(readingQualityRecord1.getTimestamp()).thenReturn(first.toInstant());
        when(readingQualityRecord2.getTimestamp()).thenReturn(first.plusHours(1).toInstant());
        when(readingQualityRecord3.getTimestamp()).thenReturn(first.plusHours(2).toInstant());
        when(readingQualityRecord4.getTimestamp()).thenReturn(first.plusHours(3).toInstant());
        when(readingQualityRecord5.getTimestamp()).thenReturn(first.plusHours(4).toInstant());
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

    }

    @After
    public void tearDown() {

    }

    @Test
    public void testFindBlocksWhenThereAreNoSuspects() {
        List<EstimationBlock> blocksToEstimate = new EstimationEngine().findBlocksToEstimate(meterActivation, readingType);

        assertThat(blocksToEstimate).isEmpty();
    }

    @Test
    public void testFindBlocksWhenThereIsOneSuspectForMissing() {
        when(channel1.findReadingQuality(SUSPECT, Range.<Instant>all())).thenReturn(Arrays.asList(readingQualityRecord2));
        when(readingQualityRecord2.getBaseReadingRecord()).thenReturn(Optional.<BaseReadingRecord>empty());

        List<EstimationBlock> blocksToEstimate = new EstimationEngine().findBlocksToEstimate(meterActivation, readingType);

        assertThat(blocksToEstimate).hasSize(1);

        EstimationBlock estimationBlock = blocksToEstimate.get(0);

        assertThat(estimationBlock.estimatables()).hasSize(1);

        Estimatable estimatable = estimationBlock.estimatables().get(0);

        assertThat(estimatable.getTimestamp()).isEqualTo(readingQualityRecord2.getTimestamp());
        assertThat(estimatable).isInstanceOf(MissingReadingRecordEstimatable.class);
    }

    @Test
    public void testFindBlocksWhenThereIsOneSuspectForReading() {
        when(channel1.findReadingQuality(SUSPECT, Range.<Instant>all())).thenReturn(Arrays.asList(readingQualityRecord2));

        List<EstimationBlock> blocksToEstimate = new EstimationEngine().findBlocksToEstimate(meterActivation, readingType);

        assertThat(blocksToEstimate).hasSize(1);

        EstimationBlock estimationBlock = blocksToEstimate.get(0);

        assertThat(estimationBlock.estimatables()).hasSize(1);

        Estimatable estimatable = estimationBlock.estimatables().get(0);

        assertThat(estimatable.getTimestamp()).isEqualTo(readingQualityRecord2.getTimestamp());
        assertThat(estimatable).isInstanceOf(BaseReadingRecordEstimatable.class);
    }

    @Test
    public void testFindBlocksWhenThereIsOneBlockOfSuspectForReading() {
        when(channel1.findReadingQuality(SUSPECT, Range.<Instant>all())).thenReturn(Arrays.asList(readingQualityRecord2, readingQualityRecord3, readingQualityRecord4));

        List<EstimationBlock> blocksToEstimate = new EstimationEngine().findBlocksToEstimate(meterActivation, readingType);

        assertThat(blocksToEstimate).hasSize(1);

        EstimationBlock estimationBlock = blocksToEstimate.get(0);

        assertThat(estimationBlock.estimatables()).hasSize(3);

        assertThat(estimationBlock.estimatables().get(0).getTimestamp()).isEqualTo(readingQualityRecord2.getTimestamp());
        assertThat(estimationBlock.estimatables().get(1).getTimestamp()).isEqualTo(readingQualityRecord3.getTimestamp());
        assertThat(estimationBlock.estimatables().get(2).getTimestamp()).isEqualTo(readingQualityRecord4.getTimestamp());
    }

//    @Test
//    public void testFindBlocksWhenThereIsOneBlockOfSuspectForReading() {
//        when(channel1.findReadingQuality(SUSPECT, Range.<Instant>all())).thenReturn(Arrays.asList(readingQualityRecord2, readingQualityRecord3, readingQualityRecord4));
//
//        List<EstimationBlock> blocksToEstimate = new EstimationEngine().findBlocksToEstimate(meterActivation, readingType);
//
//        assertThat(blocksToEstimate).hasSize(1);
//
//        EstimationBlock estimationBlock = blocksToEstimate.get(0);
//
//        assertThat(estimationBlock.estimatables()).hasSize(1);
//
//        Estimatable estimatable = estimationBlock.estimatables().get(0);
//
//        assertThat(estimatable.getTimestamp()).isEqualTo(readingQualityRecord2.getTimestamp());
//        assertThat(estimatable).isInstanceOf(BaseReadingRecordEstimatable.class);
//    }
//
//

}