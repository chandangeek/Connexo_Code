package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.google.common.collect.Range;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
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
public class ZeroFillEstimatorTest {

    private EstimationServiceImpl estimationService;

    @Rule
    public TestRule mcMurdo = Using.timeZoneOfMcMurdo();

    public static final ReadingQualityType SUSPECT = ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT);
    @Mock
    private MeterActivation meterActivation;
    @Mock
    private ReadingType readingType;
    @Mock
    private Channel channel;
    @Mock
    private ReadingQualityRecord readingQualityRecord;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private PropertySpecService propertySpecService;

    @Before
    public void setUp() {
        estimationService = new EstimationServiceImpl();
        when(meterActivation.getChannels()).thenReturn(Arrays.asList(channel));
        doReturn(Arrays.asList(readingType)).when(channel).getReadingTypes();
        when(channel.isRegular()).thenReturn(true);
        when(channel.getIntervalLength()).thenReturn(Optional.of(Duration.ofHours(1)));
        when(channel.findReadingQuality(SUSPECT, Range.<Instant>all())).thenReturn(Collections.emptyList());
        ZonedDateTime first = ZonedDateTime.of(2010, 8, 14, 9, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
        when(readingQualityRecord.getTimestamp()).thenReturn(first.plusHours(1).toInstant());

    }

    @After
    public void tearDown() {

    }

    @Test
    public void testZeroFillEstimator() {
        when(channel.findReadingQuality(SUSPECT, Range.<Instant>all())).thenReturn(Arrays.asList(readingQualityRecord));
        when(readingQualityRecord.getBaseReadingRecord()).thenReturn(Optional.<BaseReadingRecord>empty());

        List<EstimationBlock> blocksToEstimate = new EstimationEngine().findBlocksToEstimate(meterActivation, readingType);

        EstimationBlock estimationBlock = blocksToEstimate.get(0);
        Estimatable estimatable = estimationBlock.estimatables().get(0);
        //Missing value "null"
        Assertions.assertThat(estimatable.getEstimation()).isNull();

        Estimator estimator = new ZeroFillEstimator(thesaurus, propertySpecService);

        EstimationResult estimationResult =
                estimator.estimate(estimationBlock);


        assertThat(estimationResult).isEqualTo(EstimationResult.ESTIMATED);

        //ZERO value after estimation
        assertThat(estimatable.getEstimation()).isEqualTo(BigDecimal.ZERO);
    }

}