package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;
import static java.math.BigDecimal.ZERO;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ZeroFillEstimatorTest {

    @Rule
    public TestRule mcMurdo = Using.timeZoneOfMcMurdo();

    public static final ReadingQualityType SUSPECT = ReadingQualityType.of(QualityCodeSystem.MDM, QualityCodeIndex.SUSPECT);
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private PropertySpecService propertySpecService;

    @Test
    public void testZeroFillEstimator() {
        EstimationBlock estimationBlock = mock(EstimationBlock.class);
        Estimatable estimatable1 = mock(Estimatable.class);
        Estimatable estimatable2 = mock(Estimatable.class);
        doReturn(Arrays.asList(estimatable1, estimatable2)).when(estimationBlock).estimatables();

        Estimator estimator = new ValueFillEstimator(thesaurus, propertySpecService);

        EstimationResult estimationResult = estimator.estimate(Arrays.asList(estimationBlock));

        assertThat(estimationResult.remainingToBeEstimated()).isEmpty();
        assertThat(estimationResult.estimated()).containsExactly(estimationBlock);

        //ZERO value after estimation
        verify(estimatable1).setEstimation(ZERO);
        verify(estimatable2).setEstimation(ZERO);
    }

}