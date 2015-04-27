package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.estimation.BulkAdvanceReadingsSettings;
import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.estimation.ReadingTypeAdvanceReadingsSettings;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.units.Unit;
import com.google.common.collect.Range;
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
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalMatchers.cmpEq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class EqualDistributionTest {

    private static final ZonedDateTime BEFORE = ZonedDateTime.of(2015, 3, 11, 20, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
    private static final ZonedDateTime ESTIMATABLE1 = BEFORE.plusHours(1);
    private static final ZonedDateTime ESTIMATABLE2 = BEFORE.plusHours(2);
    private static final ZonedDateTime ESTIMATABLE3 = BEFORE.plusHours(3);
    private static final ZonedDateTime AFTER = BEFORE.plusHours(4);
    private static final ZonedDateTime BEFORE_MINUS_1 = BEFORE.minusHours(1);
    private static final ZonedDateTime BEFORE_MINUS_2 = BEFORE.minusHours(2);
    private static final ZonedDateTime BEFORE_MINUS_3 = BEFORE.minusHours(3);
    private static final ZonedDateTime AFTER_PLUS_1 = AFTER.plusHours(1);
    private static final ZonedDateTime AFTER_PLUS_2 = AFTER.plusHours(2);
    private static final ZonedDateTime AFTER_PLUS_3 = AFTER.plusHours(3);
    private static final ZonedDateTime ADVANCE_BEFORE = BEFORE_MINUS_3.minusMinutes(20);
    private static final ZonedDateTime ADVANCE_AFTER = AFTER_PLUS_3.minusMinutes(40);

    @Rule
    public TestRule mcMurdo = Using.timeZoneOfMcMurdo();

    public static final ReadingQualityType SUSPECT = ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT);
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private ReadingType deltaReadingType, bulkReadingType, advanceReadingType;
    @Mock
    private Channel channel, otherChannel;
    @Mock
    private EstimationBlock estimationBlock;
    @Mock
    private Estimatable estimatable1, estimatable2, estimatable3;
    @Mock
    private IntervalReadingRecord intervalReadingRecord1, intervalReadingRecord2, deltaReading0, deltaReading1, deltaReading2, deltaReading3, deltaReading4, deltaReading7, deltaReading8, deltaReading9, deltaReading10;
    @Mock
    private ReadingRecord advanceReadingRecord1, advanceReadingRecord2;
    @Mock
    private CimChannel deltaCimChannel, bulkCimChannel, advanceCimChannel;
    @Mock
    private MeterActivation meterActivation;
    @Mock
    private ReadingQualityRecord suspect1, suspect2, suspect3, suspect4;
    @Mock
    private MeteringService meteringService;

    @Before
    public void setUp() {
        doReturn(Arrays.asList(estimatable1, estimatable2, estimatable3)).when(estimationBlock).estimatables();
        doReturn(channel).when(estimationBlock).getChannel();
        doReturn(meterActivation).when(channel).getMeterActivation();
        doReturn(Arrays.asList(channel, otherChannel)).when(meterActivation).getChannels();
        doReturn(Arrays.asList(deltaReadingType, bulkReadingType)).when(channel).getReadingTypes();
        doReturn(Collections.singletonList(advanceReadingType)).when(otherChannel).getReadingTypes();
        doReturn(Optional.of(bulkCimChannel)).when(channel).getCimChannel(bulkReadingType);
        doReturn(Optional.of(deltaCimChannel)).when(channel).getCimChannel(deltaReadingType);
        doReturn(Optional.of(advanceCimChannel)).when(otherChannel).getCimChannel(advanceReadingType);
        doReturn(deltaReadingType).when(estimationBlock).getReadingType();
        doReturn(false).when(deltaReadingType).isCumulative();
        doReturn(true).when(deltaReadingType).isRegular();
        doReturn(true).when(bulkReadingType).isCumulative();
        doReturn(true).when(bulkReadingType).isRegular();
        doReturn(Optional.of(bulkReadingType)).when(deltaReadingType).getBulkReadingType();
        doReturn(ReadingTypeUnit.WATTHOUR).when(deltaReadingType).getUnit();
        doReturn(MetricMultiplier.ZERO).when(deltaReadingType).getMultiplier();
        doReturn(ReadingTypeUnit.WATTHOUR).when(advanceReadingType).getUnit();
        doReturn(MetricMultiplier.KILO).when(advanceReadingType).getMultiplier();
        doReturn(TimeZoneNeutral.getMcMurdo()).when(deltaCimChannel).getZoneId();

        // for bulk reading based tests
        doReturn(ESTIMATABLE1.toInstant()).when(estimatable1).getTimestamp();
        doReturn(ESTIMATABLE2.toInstant()).when(estimatable2).getTimestamp();
        doReturn(ESTIMATABLE3.toInstant()).when(estimatable3).getTimestamp();
        doReturn(Optional.of(intervalReadingRecord1)).when(channel).getReading(BEFORE.toInstant());
        doReturn(Optional.of(intervalReadingRecord2)).when(channel).getReading(ESTIMATABLE3.toInstant());
        doReturn(Optional.of(intervalReadingRecord1)).when(bulkCimChannel).getReading(BEFORE.toInstant());
        doReturn(Optional.of(intervalReadingRecord2)).when(bulkCimChannel).getReading(ESTIMATABLE3.toInstant());
        doReturn(Optional.of(intervalReadingRecord2)).when(bulkCimChannel).getReading(AFTER.toInstant());
        doReturn(ProfileStatus.of(ProfileStatus.Flag.POWERDOWN)).when(intervalReadingRecord1).getProfileStatus();
        doReturn(ProfileStatus.of(ProfileStatus.Flag.POWERUP)).when(intervalReadingRecord2).getProfileStatus();
        doReturn(Unit.WATT_HOUR.amount(BigDecimal.valueOf(5014, 2))).when(intervalReadingRecord1).getQuantity(bulkReadingType);
        doReturn(Unit.WATT_HOUR.amount(BigDecimal.valueOf(54897, 3))).when(intervalReadingRecord2).getQuantity(bulkReadingType);
        doReturn(BigDecimal.valueOf(5014, 2)).when(intervalReadingRecord1).getValue();
        doReturn(BigDecimal.valueOf(54897, 3)).when(intervalReadingRecord2).getValue();

        // for advance reading based tests
        doReturn(Arrays.asList(deltaReading0, deltaReading1, deltaReading2, deltaReading3, deltaReading4, deltaReading7, deltaReading8, deltaReading9, deltaReading10)).when(deltaCimChannel).getReadings(Range.closed(BEFORE_MINUS_3.toInstant(), AFTER_PLUS_3.toInstant()));
        doReturn(BigDecimal.valueOf(5)).when(deltaReading0).getValue();
        doReturn(BigDecimal.valueOf(6)).when(deltaReading1).getValue();
        doReturn(BigDecimal.valueOf(2)).when(deltaReading2).getValue();
        doReturn(BigDecimal.valueOf(4)).when(deltaReading3).getValue();
        doReturn(BigDecimal.valueOf(-3)).when(deltaReading4).getValue();
        doReturn(BigDecimal.valueOf(5)).when(deltaReading7).getValue();
        doReturn(BigDecimal.valueOf(2)).when(deltaReading8).getValue();
        doReturn(BigDecimal.valueOf(4)).when(deltaReading9).getValue();
        doReturn(BigDecimal.valueOf(3)).when(deltaReading10).getValue();
        doReturn(BEFORE_MINUS_3.toInstant()).when(deltaReading0).getTimeStamp();
        doReturn(BEFORE_MINUS_2.toInstant()).when(deltaReading1).getTimeStamp();
        doReturn(BEFORE_MINUS_1.toInstant()).when(deltaReading2).getTimeStamp();
        doReturn(BEFORE.toInstant()).when(deltaReading3).getTimeStamp();
        doReturn(ESTIMATABLE1.toInstant()).when(deltaReading4).getTimeStamp();
        doReturn(AFTER.toInstant()).when(deltaReading7).getTimeStamp();
        doReturn(AFTER_PLUS_1.toInstant()).when(deltaReading8).getTimeStamp();
        doReturn(AFTER_PLUS_2.toInstant()).when(deltaReading9).getTimeStamp();
        doReturn(AFTER_PLUS_3.toInstant()).when(deltaReading10).getTimeStamp();
        doReturn(Collections.singletonList(advanceReadingRecord1)).when(advanceCimChannel).getReadingsOnOrBefore(BEFORE.toInstant(), 1);
        doReturn(Collections.singletonList(advanceReadingRecord2)).when(advanceCimChannel).getReadings(Range.atLeast(ESTIMATABLE3.toInstant()));
        doReturn(BigDecimal.valueOf(18914, 3)).when(advanceReadingRecord1).getValue();
        doReturn(ADVANCE_BEFORE.toInstant()).when(advanceReadingRecord1).getTimeStamp();
        doReturn(ADVANCE_AFTER.toInstant()).when(advanceReadingRecord2).getTimeStamp();
        doReturn(BigDecimal.valueOf(18957, 3)).when(advanceReadingRecord2).getValue();
        doReturn(Arrays.asList(suspect1, suspect2, suspect3)).when(deltaCimChannel).findActualReadingQuality(Range.closed(BEFORE_MINUS_3.toInstant(), AFTER_PLUS_3.toInstant()));
        doReturn(Collections.emptyList()).when(advanceCimChannel).findReadingQuality(ADVANCE_BEFORE.toInstant());
        doReturn(Collections.emptyList()).when(advanceCimChannel).findReadingQuality(ADVANCE_AFTER.toInstant());
        doReturn(true).when(suspect1).isSuspect();
        doReturn(true).when(suspect2).isSuspect();
        doReturn(true).when(suspect3).isSuspect();
        doReturn(ESTIMATABLE1.toInstant()).when(suspect1).getReadingTimestamp();
        doReturn(ESTIMATABLE2.toInstant()).when(suspect2).getReadingTimestamp();
        doReturn(ESTIMATABLE3.toInstant()).when(suspect3).getReadingTimestamp();
        doReturn(advanceReadingType).when(advanceCimChannel).getReadingType();
        doReturn(Arrays.asList(BEFORE_MINUS_3.toInstant(), BEFORE_MINUS_2.toInstant(), BEFORE_MINUS_1.toInstant(), BEFORE.toInstant(),
                ESTIMATABLE1.toInstant(), ESTIMATABLE2.toInstant(), ESTIMATABLE3.toInstant(), AFTER.toInstant(), AFTER_PLUS_1.toInstant(),
                AFTER_PLUS_2.toInstant())).when(deltaCimChannel).toList(Range.closed(ADVANCE_BEFORE.toInstant(), ADVANCE_AFTER.toInstant()));
        doReturn(AFTER_PLUS_3.toInstant()).when(deltaCimChannel).getNextDateTime(AFTER_PLUS_2.toInstant());
        doReturn(Optional.of(Duration.ofHours(1))).when(deltaCimChannel).getIntervalLength();

        doReturn(BEFORE.toInstant()).when(channel).getPreviousDateTime(ESTIMATABLE1.toInstant());
        doReturn(AFTER.toInstant()).when(channel).getNextDateTime(ESTIMATABLE3.toInstant());
        doReturn(BEFORE.toInstant()).when(bulkCimChannel).getPreviousDateTime(ESTIMATABLE1.toInstant());
        doReturn(AFTER.toInstant()).when(bulkCimChannel).getNextDateTime(ESTIMATABLE3.toInstant());
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testEqualDistributionUsingBulk() {

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(EqualDistribution.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, BigDecimal.valueOf(10));
        properties.put(EqualDistribution.ADVANCE_READINGS_SETTINGS, BulkAdvanceReadingsSettings.INSTANCE);

        Estimator estimator = new EqualDistribution(thesaurus, propertySpecService, meteringService, properties);
        estimator.init();

        EstimationResult estimationResult = estimator.estimate(Arrays.asList(estimationBlock));

        assertThat(estimationResult.remainingToBeEstimated()).isEmpty();
        assertThat(estimationResult.estimated()).containsExactly(estimationBlock);

        //ZERO value after estimation
        verify(estimatable1).setEstimation(cmpEq(BigDecimal.valueOf(1585667, 6)));
        verify(estimatable2).setEstimation(cmpEq(BigDecimal.valueOf(1585667, 6)));
        verify(estimatable3).setEstimation(cmpEq(BigDecimal.valueOf(1585667, 6)));
    }

    @Test
    public void testEqualDistributionUsingAdvances() {

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(EqualDistribution.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, BigDecimal.valueOf(10));
        properties.put(EqualDistribution.ADVANCE_READINGS_SETTINGS, new ReadingTypeAdvanceReadingsSettings(advanceReadingType));

        Estimator estimator = new EqualDistribution(thesaurus, propertySpecService, meteringService, properties);
        estimator.init();

        EstimationResult estimationResult = estimator.estimate(Arrays.asList(estimationBlock));

        assertThat(estimationResult.remainingToBeEstimated()).isEmpty();
        assertThat(estimationResult.estimated()).containsExactly(estimationBlock);

        //ZERO value after estimation
        verify(estimatable1).setEstimation(cmpEq(BigDecimal.valueOf(5777778, 6)));
        verify(estimatable2).setEstimation(cmpEq(BigDecimal.valueOf(5777778, 6)));
        verify(estimatable3).setEstimation(cmpEq(BigDecimal.valueOf(5777778, 6)));
    }


    @Test
    public void testEqualDistributionDoesNotEstimateWhenTooManySuspects() {

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(EqualDistribution.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, BigDecimal.ONE);
        properties.put(EqualDistribution.ADVANCE_READINGS_SETTINGS, new ReadingTypeAdvanceReadingsSettings(advanceReadingType));

        Estimator estimator = new EqualDistribution(thesaurus, propertySpecService, meteringService, properties);
        estimator.init();

        EstimationResult estimationResult = estimator.estimate(Arrays.asList(estimationBlock));

        assertThat(estimationResult.estimated()).isEmpty();
        assertThat(estimationResult.remainingToBeEstimated()).containsExactly(estimationBlock);
    }

    @Test
    public void testEqualDistributionDoesNotEstimateWhenASecondGapOccursBetweenAdvanceReadings() {
        doReturn(Arrays.asList(suspect1, suspect2, suspect3, suspect4)).when(deltaCimChannel).findActualReadingQuality(Range.closed(BEFORE_MINUS_3.toInstant(), AFTER_PLUS_3.toInstant()));
        doReturn(true).when(suspect4).isSuspect();
        doReturn(AFTER_PLUS_2.toInstant()).when(suspect4).getReadingTimestamp();

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(EqualDistribution.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, BigDecimal.valueOf(10));
        properties.put(EqualDistribution.ADVANCE_READINGS_SETTINGS, new ReadingTypeAdvanceReadingsSettings(advanceReadingType));

        Estimator estimator = new EqualDistribution(thesaurus, propertySpecService, meteringService, properties);
        estimator.init();

        EstimationResult estimationResult = estimator.estimate(Arrays.asList(estimationBlock));

        assertThat(estimationResult.estimated()).isEmpty();
        assertThat(estimationResult.remainingToBeEstimated()).containsExactly(estimationBlock);
    }

    @Test
    public void testEqualDistributionDoesNotEstimateWhenAdvanceReadingIsSuspect() {
        doReturn(Arrays.asList(suspect4)).when(advanceCimChannel).findReadingQuality(ADVANCE_AFTER.toInstant());
        doReturn(true).when(suspect4).isSuspect();
        doReturn(ADVANCE_AFTER.toInstant()).when(suspect4).getReadingTimestamp();

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(EqualDistribution.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, BigDecimal.valueOf(10));
        properties.put(EqualDistribution.ADVANCE_READINGS_SETTINGS, new ReadingTypeAdvanceReadingsSettings(advanceReadingType));

        Estimator estimator = new EqualDistribution(thesaurus, propertySpecService, meteringService, properties);
        estimator.init();

        EstimationResult estimationResult = estimator.estimate(Arrays.asList(estimationBlock));

        assertThat(estimationResult.estimated()).isEmpty();
        assertThat(estimationResult.remainingToBeEstimated()).containsExactly(estimationBlock);
    }

    @Test
    public void testEqualDistributionDoesNotEstimateWhenAdvanceReadingIsEstimation() {
        doReturn(Arrays.asList(suspect4)).when(advanceCimChannel).findReadingQuality(ADVANCE_BEFORE.toInstant());
        doReturn(true).when(suspect4).hasEstimatedCategory();
        doReturn(ADVANCE_BEFORE.toInstant()).when(suspect4).getReadingTimestamp();

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(EqualDistribution.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, BigDecimal.valueOf(10));
        properties.put(EqualDistribution.ADVANCE_READINGS_SETTINGS, new ReadingTypeAdvanceReadingsSettings(advanceReadingType));

        Estimator estimator = new EqualDistribution(thesaurus, propertySpecService, meteringService, properties);
        estimator.init();

        EstimationResult estimationResult = estimator.estimate(Arrays.asList(estimationBlock));

        assertThat(estimationResult.estimated()).isEmpty();
        assertThat(estimationResult.remainingToBeEstimated()).containsExactly(estimationBlock);
    }

    @Test
    public void testEqualDistributionDoesNotEstimateWhenConsumptionReadingIsEstimation() {
        doReturn(Arrays.asList(suspect1, suspect2, suspect3, suspect4)).when(deltaCimChannel).findActualReadingQuality(Range.closed(BEFORE_MINUS_3.toInstant(), AFTER_PLUS_3.toInstant()));
        doReturn(true).when(suspect4).hasEstimatedCategory();
        doReturn(AFTER_PLUS_2.toInstant()).when(suspect4).getReadingTimestamp();

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(EqualDistribution.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, BigDecimal.valueOf(10));
        properties.put(EqualDistribution.ADVANCE_READINGS_SETTINGS, new ReadingTypeAdvanceReadingsSettings(advanceReadingType));

        Estimator estimator = new EqualDistribution(thesaurus, propertySpecService, meteringService, properties);
        estimator.init();

        EstimationResult estimationResult = estimator.estimate(Arrays.asList(estimationBlock));

        assertThat(estimationResult.estimated()).isEmpty();
        assertThat(estimationResult.remainingToBeEstimated()).containsExactly(estimationBlock);
    }

    @Test
    public void testEqualDistributionDoesNotEstimateWhenBeforeAdvanceReadingNotThere() {
        doReturn(Collections.emptyList()).when(advanceCimChannel).getReadingsOnOrBefore(BEFORE.toInstant(), 1);

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(EqualDistribution.ADVANCE_READINGS_SETTINGS, new ReadingTypeAdvanceReadingsSettings(advanceReadingType));
        properties.put(EqualDistribution.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, BigDecimal.valueOf(10));

        Estimator estimator = new EqualDistribution(thesaurus, propertySpecService, meteringService, properties);
        estimator.init();

        EstimationResult estimationResult = estimator.estimate(Arrays.asList(estimationBlock));

        assertThat(estimationResult.estimated()).isEmpty();
        assertThat(estimationResult.remainingToBeEstimated()).containsExactly(estimationBlock);

    }

    @Test
    public void testEqualDistributionDoesNotEstimateWhenBeforeReadingHasNoBulkValue() {
        doReturn(null).when(intervalReadingRecord1).getValue();

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(EqualDistribution.ADVANCE_READINGS_SETTINGS, BulkAdvanceReadingsSettings.INSTANCE);
        properties.put(EqualDistribution.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, BigDecimal.valueOf(10));

        Estimator estimator = new EqualDistribution(thesaurus, propertySpecService, meteringService, properties);
        estimator.init();

        EstimationResult estimationResult = estimator.estimate(Arrays.asList(estimationBlock));

        assertThat(estimationResult.estimated()).isEmpty();
        assertThat(estimationResult.remainingToBeEstimated()).containsExactly(estimationBlock);

    }

    @Test
    public void testEqualDistributionDoesNotEstimateWhenAfterAdvanceReadingNotThere() {
        doReturn(Collections.emptyList()).when(advanceCimChannel).getReadings(Range.atLeast(ESTIMATABLE3.toInstant()));

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(EqualDistribution.ADVANCE_READINGS_SETTINGS, new ReadingTypeAdvanceReadingsSettings(advanceReadingType));
        properties.put(EqualDistribution.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, BigDecimal.valueOf(10));

        Estimator estimator = new EqualDistribution(thesaurus, propertySpecService, meteringService, properties);
        estimator.init();

        EstimationResult estimationResult = estimator.estimate(Arrays.asList(estimationBlock));

        assertThat(estimationResult.estimated()).isEmpty();
        assertThat(estimationResult.remainingToBeEstimated()).containsExactly(estimationBlock);

    }

    @Test
    public void testEqualDistributionDoesNotEstimateWhenAfterReadingHasNoBulkValue() {
        doReturn(null).when(intervalReadingRecord2).getValue();

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(EqualDistribution.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, BigDecimal.valueOf(10));
        properties.put(EqualDistribution.ADVANCE_READINGS_SETTINGS, BulkAdvanceReadingsSettings.INSTANCE);

        Estimator estimator = new EqualDistribution(thesaurus, propertySpecService, meteringService, properties);
        estimator.init();

        EstimationResult estimationResult = estimator.estimate(Arrays.asList(estimationBlock));

        assertThat(estimationResult.estimated()).isEmpty();
        assertThat(estimationResult.remainingToBeEstimated()).containsExactly(estimationBlock);

    }

    @Test
    public void testEqualDistributionDoesNotEstimateWhenDeltaHasNoBulkReadingType() {
        doReturn(Optional.empty()).when(deltaReadingType).getBulkReadingType();

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(EqualDistribution.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, BigDecimal.valueOf(10));
        properties.put(EqualDistribution.ADVANCE_READINGS_SETTINGS, BulkAdvanceReadingsSettings.INSTANCE);

        Estimator estimator = new EqualDistribution(thesaurus, propertySpecService, meteringService, properties);
        estimator.init();

        EstimationResult estimationResult = estimator.estimate(Arrays.asList(estimationBlock));

        assertThat(estimationResult.estimated()).isEmpty();
        assertThat(estimationResult.remainingToBeEstimated()).containsExactly(estimationBlock);
    }

    @Test
    public void testEqualDistributionDoesNotEstimateWhenChannelDoesNotContainTheBulkReadingType() {
        doReturn(Optional.empty()).when(channel).getCimChannel(bulkReadingType);

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(EqualDistribution.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, BigDecimal.valueOf(10));
        properties.put(EqualDistribution.ADVANCE_READINGS_SETTINGS, BulkAdvanceReadingsSettings.INSTANCE);

        Estimator estimator = new EqualDistribution(thesaurus, propertySpecService, meteringService, properties);
        estimator.init();

        EstimationResult estimationResult = estimator.estimate(Arrays.asList(estimationBlock));

        assertThat(estimationResult.estimated()).isEmpty();
        assertThat(estimationResult.remainingToBeEstimated()).containsExactly(estimationBlock);

    }

    @Test
    public void testEqualDistributionDoesNotEstimateBulkWhenBeforeReadingNotThere() {
        doReturn(Optional.empty()).when(channel).getReading(BEFORE.toInstant());
        doReturn(Optional.empty()).when(bulkCimChannel).getReading(BEFORE.toInstant());

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(EqualDistribution.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, BigDecimal.valueOf(10));
        properties.put(EqualDistribution.ADVANCE_READINGS_SETTINGS, BulkAdvanceReadingsSettings.INSTANCE);

        Estimator estimator = new EqualDistribution(thesaurus, propertySpecService, meteringService, properties);
        estimator.init();

        EstimationResult estimationResult = estimator.estimate(Arrays.asList(estimationBlock));

        assertThat(estimationResult.estimated()).isEmpty();
        assertThat(estimationResult.remainingToBeEstimated()).containsExactly(estimationBlock);

    }

    @Test
    public void testEqualDistributionDoesNotEstimateWhenReadingTypeHasNoInterval() {
        doReturn(false).when(deltaReadingType).isRegular();
        doReturn(false).when(bulkReadingType).isRegular();

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(EqualDistribution.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, BigDecimal.valueOf(10));
        properties.put(EqualDistribution.ADVANCE_READINGS_SETTINGS, BulkAdvanceReadingsSettings.INSTANCE);

        Estimator estimator = new EqualDistribution(thesaurus, propertySpecService, meteringService, properties);
        estimator.init();

        EstimationResult estimationResult = estimator.estimate(Arrays.asList(estimationBlock));

        assertThat(estimationResult.estimated()).isEmpty();
        assertThat(estimationResult.remainingToBeEstimated()).containsExactly(estimationBlock);

    }

}
