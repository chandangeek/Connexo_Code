package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.google.common.collect.Range;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;
import static java.math.BigDecimal.ZERO;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ValueFillEstimatorTest {

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

    @Test
    public void testZeroFillEstimator() {
        EstimationBlock estimationBlock = mock(EstimationBlock.class);
        Estimatable estimatable1 = mock(Estimatable.class);
        Estimatable estimatable2 = mock(Estimatable.class);
        doReturn(Arrays.asList(estimatable1, estimatable2)).when(estimationBlock).estimatables();

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(ValueFillEstimator.FILL_VALUE, new BigDecimal(5));
        properties.put(ValueFillEstimator.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, new BigDecimal(10));

        Estimator estimator = new ValueFillEstimator(thesaurus, propertySpecService, properties);
        estimator.init(channel, readingType, Range.all());

        EstimationResult estimationResult = estimator.estimate(Arrays.asList(estimationBlock));

        assertThat(estimationResult.remainingToBeEstimated()).isEmpty();
        assertThat(estimationResult.estimated()).containsExactly(estimationBlock);

        //ZERO value after estimation
        verify(estimatable1).setEstimation(new BigDecimal(5));
        verify(estimatable2).setEstimation(new BigDecimal(5));
    }

}