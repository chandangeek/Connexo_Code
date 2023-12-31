/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.tests.fakes.LogRecorder;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleProperties;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.impl.PropertySpecServiceImpl;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.logging.LoggingContext;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;
import static com.elster.jupiter.estimators.impl.ValueFillEstimator.MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValueFillEstimatorTest {

    private static final Logger LOGGER = Logger.getLogger(ValueFillEstimatorTest.class.getName());

    @Rule
    public TestRule mcMurdo = Using.timeZoneOfMcMurdo();

    private Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;

    @Mock
    private TimeService timeService;
    @Mock
    private OrmService ormService;
    @Mock
    private BeanService beanService;

    private PropertySpecService propertySpecService = new PropertySpecServiceImpl(timeService, ormService, beanService);
    @Mock
    private ReadingType readingType;
    @Mock
    private Channel channel;
    private LogRecorder logRecorder;
    @Mock
    private MeterActivation meterActivation;
    @Mock
    private ChannelsContainer channelsContainer;
    @Mock
    private Meter meter;

    @Before
    public void setUp() {
        logRecorder = new LogRecorder(Level.ALL);
        LOGGER.addHandler(logRecorder);
        when(meterActivation.getChannelsContainer()).thenReturn(channelsContainer);
        doReturn("readingType").when(readingType).getMRID();
        doReturn(channelsContainer).when(channel).getChannelsContainer();
        doReturn(Optional.of(meter)).when(channelsContainer).getMeter();
        doReturn(Optional.empty()).when(channelsContainer).getUsagePoint();

        LoggingContext.getCloseableContext().with("rule", "rule");
    }

    @After
    public void tearDown() {
        LOGGER.removeHandler(logRecorder);
        LoggingContext.getCloseableContext().close();
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
        properties.put(MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS, TimeDuration.seconds(26));

        Estimator estimator = new ValueFillEstimator(thesaurus, propertySpecService, properties);
        estimator.init(LOGGER);

        EstimationResult estimationResult = estimator.estimate(Collections.singletonList(estimationBlock), QualityCodeSystem.NOTAPPLICABLE);

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
        doReturn(channelsContainer).when(channel).getChannelsContainer();
        doReturn(Optional.of(meter)).when(channelsContainer).getMeter();

        Map<String, Object> properties = new HashMap<>();
        properties.put(ValueFillEstimator.FILL_VALUE, BigDecimal.valueOf(5));
        properties.put(MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS, TimeDuration.seconds(50));

        Estimator estimator = new ValueFillEstimator(thesaurus, propertySpecService, properties);
        estimator.init(LOGGER);

        EstimationResult estimationResult = estimator.estimate(Collections.singletonList(estimationBlock), QualityCodeSystem.NOTAPPLICABLE);

        assertThat(estimationResult.estimated()).isEmpty();
        assertThat(estimationResult.remainingToBeEstimated()).containsExactly(estimationBlock);
        assertThat(logRecorder).hasRecordWithMessage(message -> message.startsWith("Failed estimation with rule:")
                && message.endsWith("since its size exceeds the maximum of 50 seconds")).atLevel(Level.INFO);
    }

    @Test(expected = LocalizedFieldValidationException.class)
    public void testInvalidPropertiesWhenConsecutiveIsZero() {
        EstimationRuleProperties property = estimationRuleProperty(MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS, TimeDuration.millis(0));

        Estimator estimator = new ValueFillEstimator(thesaurus, propertySpecService);

        estimator.validateProperties(Collections.singletonMap(property.getName(), property.getValue()));
    }

    @Test(expected = LocalizedFieldValidationException.class)
    public void testInvalidPropertiesWhenConsecutiveIsNegative() {
        EstimationRuleProperties property = estimationRuleProperty(MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS, TimeDuration.millis(-1));

        Estimator estimator = new ValueFillEstimator(thesaurus, propertySpecService);

        estimator.validateProperties(Collections.singletonMap(property.getName(), property.getValue()));
    }

    @Test
    public void testGetSupportedApplications() {
        assertThat(new ValueFillEstimator(thesaurus, propertySpecService).getSupportedQualityCodeSystems())
                .containsOnly(QualityCodeSystem.MDC, QualityCodeSystem.MDM);
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
            public String getDescription() {
                return "Description for " + name;
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
