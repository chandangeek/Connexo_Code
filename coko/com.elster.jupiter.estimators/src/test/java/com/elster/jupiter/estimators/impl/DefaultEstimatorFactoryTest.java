package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.validation.ValidationService;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;

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

    @Test
    public void testCreatePowerGapFill() {

        DefaultEstimatorFactory defaultEstimatorFactory = new DefaultEstimatorFactory(nlsService, propertySpecService, validationService, meteringService, timeService);

        assertThat(defaultEstimatorFactory.available()).contains(DefaultEstimatorFactory.POWER_GAP_FILL_ESTIMATOR);

        assertThat(defaultEstimatorFactory.createTemplate(DefaultEstimatorFactory.POWER_GAP_FILL_ESTIMATOR)).isInstanceOf(PowerGapFill.class);
    }

}