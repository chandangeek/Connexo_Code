package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.devtools.tests.fakes.LogRecorder;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleProperties;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.logging.LoggingContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;
import static com.elster.jupiter.estimators.impl.ValueFillEstimator.MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ValueFillEstimatorTest {

    private static final Logger LOGGER = Logger.getLogger(ValueFillEstimatorTest.class.getName());

    @Rule
    public TestRule mcMurdo = Using.timeZoneOfMcMurdo();

    @Mock
    private Thesaurus thesaurus;
    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private ReadingType readingType;
    @Mock
    private Channel channel;
    private LogRecorder logRecorder;
    @Mock
    private MeterActivation meterActivation;
    @Mock
    private Meter meter;

    @Before
    public void setUp() {
        logRecorder = new LogRecorder(Level.ALL);
        LOGGER.addHandler(logRecorder);
        doReturn("readingType").when(readingType).getMRID();
        doReturn(meterActivation).when(channel).getMeterActivation();
        doReturn(Optional.of(meter)).when(meterActivation).getMeter();

        LoggingContext.get().with("rule", "rule");
    }

    @After
    public void tearDown() {
        LOGGER.removeHandler(logRecorder);
        LoggingContext.get().close();
    }

    @Test
    public void testZeroFillEstimator() {
        EstimationBlock estimationBlock = mock(EstimationBlock.class);
        Estimatable estimatable1 = mock(Estimatable.class);
        Estimatable estimatable2 = mock(Estimatable.class);
        doReturn(Arrays.asList(estimatable1, estimatable2)).when(estimationBlock).estimatables();
        doReturn(readingType).when(estimationBlock).getReadingType();
        doReturn(channel).when(estimationBlock).getChannel();
        doReturn(Instant.ofEpochMilli(50000L)).when(estimatable1).getTimestamp();
        doReturn(Instant.ofEpochMilli(75000L)).when(estimatable2).getTimestamp();

        Map<String, Object> properties = new HashMap<>();
        properties.put(ValueFillEstimator.FILL_VALUE, new BigDecimal(5));
        properties.put(MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, 10L);

        Estimator estimator = new ValueFillEstimator(thesaurus, propertySpecService, properties);
        estimator.init(LOGGER);

        EstimationResult estimationResult = estimator.estimate(Collections.singletonList(estimationBlock));

        assertThat(estimationResult.remainingToBeEstimated()).isEmpty();
        assertThat(estimationResult.estimated()).containsExactly(estimationBlock);

        //ZERO value after estimation
        verify(estimatable1).setEstimation(new BigDecimal(5));
        verify(estimatable2).setEstimation(new BigDecimal(5));
    }

    @Test
    public void testValueFillDoesNotEstimateWhenBlockIsTooLarge() {
        EstimationBlock estimationBlock = mock(EstimationBlock.class);
        Estimatable estimatable1 = mock(Estimatable.class);
        Estimatable estimatable2 = mock(Estimatable.class);
        doReturn(Instant.ofEpochMilli(50000)).when(estimatable1).getTimestamp();
        doReturn(Instant.ofEpochMilli(100000)).when(estimatable2).getTimestamp();
        doReturn(Arrays.asList(estimatable1, estimatable2)).when(estimationBlock).estimatables();
        doReturn(readingType).when(estimationBlock).getReadingType();
        doReturn(channel).when(estimationBlock).getChannel();
        doReturn(meterActivation).when(channel).getMeterActivation();
        doReturn(Optional.of(meter)).when(meterActivation).getMeter();

        Map<String, Object> properties = new HashMap<>();
        properties.put(ValueFillEstimator.FILL_VALUE, BigDecimal.valueOf(5));
        properties.put(MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS,1L);

        Estimator estimator = new ValueFillEstimator(thesaurus, propertySpecService, properties);
        estimator.init(LOGGER);

        EstimationResult estimationResult = estimator.estimate(Collections.singletonList(estimationBlock));

        assertThat(estimationResult.estimated()).isEmpty();
        assertThat(estimationResult.remainingToBeEstimated()).containsExactly(estimationBlock);
        assertThat(logRecorder).hasRecordWithMessage(message -> message.startsWith("Failed estimation with rule:")).atLevel(Level.INFO);
    }

    @Test(expected = LocalizedFieldValidationException.class)
    public void testInvalidPropertiesWhenConsecutiveIsZero() {
        EstimationRuleProperties property = estimationRuleProperty(MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, 0L);

        Estimator estimator = new ValueFillEstimator(thesaurus, propertySpecService);

        estimator.validateProperties(Collections.singletonMap(property.getName(), property.getValue()));
    }

    @Test(expected = LocalizedFieldValidationException.class)
    public void testInvalidPropertiesWhenConsecutiveIsNegative() {
        EstimationRuleProperties property = estimationRuleProperty(MAX_NUMBER_OF_CONSECUTIVE_SUSPECTS, -1L);

        Estimator estimator = new ValueFillEstimator(thesaurus, propertySpecService);

        estimator.validateProperties(Collections.singletonMap(property.getName(), property.getValue()));
    }

    private EstimationRuleProperties estimationRuleProperty(final String name, final Object value) {
        return new EstimationRuleProperties() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getDisplayName() {
                return name;
            }

            @Override
            public Object getValue() {
                return value;
            }

            @Override
            public void setValue(Object value) {
            }

            @Override
            public EstimationRule getRule() {
                return mock(EstimationRule.class);
            }
        };
    }


}