/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.validation.ValidationService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultEstimatorFactoryTest {

    @Mock
    private NlsService nlsService;
    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private ValidationService validationService;
    @Mock
    private MeteringService meteringService;
    @Mock
    private TimeService timeService;

    @Before
    public void setUp() {
        when(nlsService.getThesaurus(MessageSeeds.COMPONENT_NAME, Layer.DOMAIN)).thenReturn(NlsModule.FakeThesaurus.INSTANCE);
        when(nlsService.getThesaurus(EstimationService.COMPONENTNAME, Layer.DOMAIN)).thenReturn(NlsModule.FakeThesaurus.INSTANCE);
    }

    @Test
    public void testCreatePowerGapFill() {
        DefaultEstimatorFactory defaultEstimatorFactory = new DefaultEstimatorFactory(nlsService, propertySpecService, validationService, meteringService, timeService);

        assertThat(defaultEstimatorFactory.available()).containsOnly(
                DefaultEstimatorFactory.POWER_GAP_FILL_ESTIMATOR,
                DefaultEstimatorFactory.AVG_WITH_SAMPLES_ESTIMATOR,
                DefaultEstimatorFactory.EQUAL_DISTRIBUTION_ESTIMATOR,
                DefaultEstimatorFactory.LINEAR_INTERPOLATION_ESTIMATOR,
                DefaultEstimatorFactory.VALUE_FILL_ESTIMATOR);

        assertThat(defaultEstimatorFactory.createTemplate(DefaultEstimatorFactory.POWER_GAP_FILL_ESTIMATOR)).isInstanceOf(PowerGapFill.class);
        assertThat(defaultEstimatorFactory.createTemplate(DefaultEstimatorFactory.AVG_WITH_SAMPLES_ESTIMATOR)).isInstanceOf(AverageWithSamplesEstimator.class);
        assertThat(defaultEstimatorFactory.createTemplate(DefaultEstimatorFactory.EQUAL_DISTRIBUTION_ESTIMATOR)).isInstanceOf(EqualDistribution.class);
        assertThat(defaultEstimatorFactory.createTemplate(DefaultEstimatorFactory.LINEAR_INTERPOLATION_ESTIMATOR)).isInstanceOf(LinearInterpolation.class);
        assertThat(defaultEstimatorFactory.createTemplate(DefaultEstimatorFactory.VALUE_FILL_ESTIMATOR)).isInstanceOf(ValueFillEstimator.class);
    }
}
