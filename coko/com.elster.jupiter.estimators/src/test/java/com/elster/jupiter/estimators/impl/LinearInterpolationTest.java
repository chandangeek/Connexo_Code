package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.units.Unit;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class LinearInterpolationTest {

    private static final ZonedDateTime BEFORE = ZonedDateTime.of(2015, 3, 11, 20, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
    private static final ZonedDateTime ESTIMATABLE1 = BEFORE.plusHours(1);
    private static final ZonedDateTime ESTIMATABLE2 = BEFORE.plusHours(2);
    private static final ZonedDateTime AFTER = BEFORE.plusHours(3);

    @Rule
    public TestRule mcMurdo = Using.timeZoneOfMcMurdo();

    public static final ReadingQualityType SUSPECT = ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT);
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private ReadingType readingType;
    @Mock
    private Channel channel;
    @Mock
    private EstimationBlock estimationBlock;
    @Mock
    private Estimatable estimatable1, estimatable2;
    @Mock
    private BaseReadingRecord readingRecord1, readingRecord2;

    @Before
    public void setUp() {
        doReturn(Arrays.asList(estimatable1, estimatable2)).when(estimationBlock).estimatables();
        doReturn(channel).when(estimationBlock).getChannel();
        doReturn(readingType).when(estimationBlock).getReadingType();
        doReturn(true).when(readingType).isCumulative();
        doReturn(ESTIMATABLE1.toInstant()).when(estimatable1).getTimestamp();
        doReturn(ESTIMATABLE2.toInstant()).when(estimatable2).getTimestamp();
        doReturn(Optional.of(readingRecord1)).when(channel).getReading(BEFORE.toInstant());
        doReturn(Optional.of(readingRecord2)).when(channel).getReading(AFTER.toInstant());
        doReturn(Unit.WATT_HOUR.amount(BigDecimal.valueOf(5014, 2))).when(readingRecord1).getQuantity(readingType);
        doReturn(Unit.WATT_HOUR.amount(BigDecimal.valueOf(54897, 3))).when(readingRecord2).getQuantity(readingType);

        doReturn(BEFORE.toInstant()).when(channel).getPreviousDateTime(ESTIMATABLE1.toInstant());
        doReturn(AFTER.toInstant()).when(channel).getNextDateTime(ESTIMATABLE2.toInstant());
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testLinearInterpolation() {

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(LinearInterpolation.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, BigDecimal.valueOf(10));

        Estimator estimator = new LinearInterpolation(thesaurus, propertySpecService, properties);
        estimator.init();

        EstimationResult estimationResult = estimator.estimate(Arrays.asList(estimationBlock));

        assertThat(estimationResult.remainingToBeEstimated()).isEmpty();
        assertThat(estimationResult.estimated()).containsExactly(estimationBlock);

        //ZERO value after estimation
        verify(estimatable1).setEstimation(BigDecimal.valueOf(51725667L, 6));
        verify(estimatable2).setEstimation(BigDecimal.valueOf(53311334L, 6));
    }

    @Test
    public void testLinearInterpolationDoesNotEstimateWhenTooManySuspects() {

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(LinearInterpolation.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, BigDecimal.valueOf(1));

        Estimator estimator = new LinearInterpolation(thesaurus, propertySpecService, properties);
        estimator.init();

        EstimationResult estimationResult = estimator.estimate(Arrays.asList(estimationBlock));

        assertThat(estimationResult.estimated()).isEmpty();
        assertThat(estimationResult.remainingToBeEstimated()).containsExactly(estimationBlock);

    }

    @Test
    public void testLinearInterpolationDoesNotEstimateWhenReadingTypeIsNotCumulative() {
        doReturn(false).when(readingType).isCumulative();

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(LinearInterpolation.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, BigDecimal.valueOf(10));

        Estimator estimator = new LinearInterpolation(thesaurus, propertySpecService, properties);
        estimator.init();

        EstimationResult estimationResult = estimator.estimate(Arrays.asList(estimationBlock));

        assertThat(estimationResult.estimated()).isEmpty();
        assertThat(estimationResult.remainingToBeEstimated()).containsExactly(estimationBlock);

    }

    @Test
    public void testLinearInterpolationDoesNotEstimateWhenBeforeReadingNotThere() {
        doReturn(Optional.empty()).when(channel).getReading(BEFORE.toInstant());

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(LinearInterpolation.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, BigDecimal.valueOf(10));

        Estimator estimator = new LinearInterpolation(thesaurus, propertySpecService, properties);
        estimator.init();

        EstimationResult estimationResult = estimator.estimate(Arrays.asList(estimationBlock));

        assertThat(estimationResult.estimated()).isEmpty();
        assertThat(estimationResult.remainingToBeEstimated()).containsExactly(estimationBlock);

    }

    @Test
    public void testLinearInterpolationDoesNotEstimateWhenAfterReadingNotThere() {
        doReturn(Optional.empty()).when(channel).getReading(AFTER.toInstant());

        Map<String, Object> properties = new HashMap<>();
        properties.put(LinearInterpolation.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, BigDecimal.valueOf(10));

        Estimator estimator = new LinearInterpolation(thesaurus, propertySpecService, properties);
        estimator.init();

        EstimationResult estimationResult = estimator.estimate(Arrays.asList(estimationBlock));

        assertThat(estimationResult.estimated()).isEmpty();
        assertThat(estimationResult.remainingToBeEstimated()).containsExactly(estimationBlock);

    }

    @Test
    public void testLinearInterpolationDoesNotEstimateWhenBeforeReadingDoesNotHaveQuantityForThatReadingType() {
        doReturn(null).when(readingRecord1).getQuantity(readingType);

        Map<String, Object> properties = new HashMap<>();
        properties.put(LinearInterpolation.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, BigDecimal.valueOf(10));

        Estimator estimator = new LinearInterpolation(thesaurus, propertySpecService, properties);
        estimator.init();

        EstimationResult estimationResult = estimator.estimate(Arrays.asList(estimationBlock));

        assertThat(estimationResult.estimated()).isEmpty();
        assertThat(estimationResult.remainingToBeEstimated()).containsExactly(estimationBlock);

    }

    @Test
    public void testLinearInterpolationDoesNotEstimateWhenAfterReadingDoesNotHaveQuantityForThatReadingType() {
        doReturn(null).when(readingRecord2).getQuantity(readingType);

        Map<String, Object> properties = new HashMap<>();
        properties.put(LinearInterpolation.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, BigDecimal.valueOf(10));

        Estimator estimator = new LinearInterpolation(thesaurus, propertySpecService, properties);
        estimator.init();

        EstimationResult estimationResult = estimator.estimate(Arrays.asList(estimationBlock));

        assertThat(estimationResult.estimated()).isEmpty();
        assertThat(estimationResult.remainingToBeEstimated()).containsExactly(estimationBlock);

    }


}