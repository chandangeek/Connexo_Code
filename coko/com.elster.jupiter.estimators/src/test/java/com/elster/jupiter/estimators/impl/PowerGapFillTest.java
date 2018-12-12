/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimators.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.tests.fakes.LogRecorder;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.estimation.Estimatable;
import com.elster.jupiter.estimation.EstimationBlock;
import com.elster.jupiter.estimation.EstimationResult;
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleProperties;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.ProtocolReadingQualities;
import com.elster.jupiter.metering.readings.ReadingQuality;
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
import com.elster.jupiter.util.units.Unit;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
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
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;
import static com.elster.jupiter.estimators.impl.PowerGapFill.MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.mockito.AdditionalMatchers.cmpEq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PowerGapFillTest {
    private static final Logger LOGGER = Logger.getLogger(PowerGapFillTest.class.getName());

    private static final ZonedDateTime BEFORE = ZonedDateTime.of(2015, 3, 11, 20, 0, 0, 0, TimeZoneNeutral.getMcMurdo());
    private static final ZonedDateTime ESTIMATABLE1 = BEFORE.plusHours(1);
    private static final ZonedDateTime ESTIMATABLE2 = BEFORE.plusHours(2);
    private static final ZonedDateTime ESTIMATABLE3 = BEFORE.plusHours(3);
    private static final ZonedDateTime AFTER = BEFORE.plusHours(4);

    @Rule
    public TestRule mcMurdo = Using.timeZoneOfMcMurdo();

    private LogRecorder logRecorder;

    private Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;

    @Mock
    private TimeService timeService;
    @Mock
    private OrmService ormService;
    @Mock
    private BeanService beanService;

    private PropertySpecService propertySpecService = new PropertySpecServiceImpl(timeService, ormService, beanService);
    @Mock
    private ReadingType deltaReadingType, bulkReadingType;
    @Mock
    private Channel channel;
    @Mock
    private EstimationBlock estimationBlock;
    @Mock
    private Estimatable estimatable1, estimatable2, estimatable3;
    @Mock
    private IntervalReadingRecord readingRecord1, readingRecord2;
    @Mock
    private CimChannel bulkCimChannel;
    @Mock
    private MeterActivation meterActivation;
    @Mock
    private ChannelsContainer channelsContainer;

    @Before
    public void setUp() {
        when(meterActivation.getChannelsContainer()).thenReturn(channelsContainer);
        doReturn(channelsContainer).when(channel).getChannelsContainer();
        doReturn(Optional.empty()).when(channelsContainer).getMeter();
        doReturn(Optional.empty()).when(channelsContainer).getUsagePoint();
        doReturn("deltaReadingType").when(deltaReadingType).getMRID();
        doReturn("bulkReadingType").when(bulkReadingType).getMRID();
        doReturn(Arrays.asList(estimatable1, estimatable2, estimatable3)).when(estimationBlock).estimatables();
        doReturn(channel).when(estimationBlock).getChannel();
        doReturn(Optional.of(bulkCimChannel)).when(channel).getCimChannel(bulkReadingType);
        doReturn(deltaReadingType).when(estimationBlock).getReadingType();
        doReturn(false).when(deltaReadingType).isCumulative();
        doReturn(true).when(deltaReadingType).isRegular();
        doReturn(true).when(bulkReadingType).isCumulative();
        doReturn(true).when(bulkReadingType).isRegular();
        doReturn(Optional.of(bulkReadingType)).when(deltaReadingType).getBulkReadingType();
        doReturn(ESTIMATABLE1.toInstant()).when(estimatable1).getTimestamp();
        doReturn(ESTIMATABLE2.toInstant()).when(estimatable2).getTimestamp();
        doReturn(ESTIMATABLE3.toInstant()).when(estimatable3).getTimestamp();
        doReturn(Optional.of(readingRecord1)).when(channel).getReading(BEFORE.toInstant());
        doReturn(Optional.of(readingRecord2)).when(channel).getReading(ESTIMATABLE3.toInstant());
        doReturn(Optional.of(readingRecord1)).when(bulkCimChannel).getReading(BEFORE.toInstant());
        doReturn(Optional.of(readingRecord2)).when(bulkCimChannel).getReading(ESTIMATABLE3.toInstant());
        doReturn(Optional.of(readingRecord2)).when(bulkCimChannel).getReading(AFTER.toInstant());
        doReturn(Collections.singletonList(mockReadingQuality(ProtocolReadingQualities.POWERDOWN.getCimCode()))).when(readingRecord1).getReadingQualities();
        doReturn(Collections.singletonList(mockReadingQuality(ProtocolReadingQualities.POWERUP.getCimCode()))).when(readingRecord2).getReadingQualities();

        when(readingRecord1.hasReadingQuality(Matchers.any(ReadingQualityType.class))).then(invocationOnMock -> {
            for (ReadingQuality readingQuality : readingRecord1.getReadingQualities()) {
                if (readingQuality.getTypeCode().equals(((ReadingQualityType) invocationOnMock.getArguments()[0]).getCode())) {
                    return true;
                }
            }
            return false;
        });

        when(readingRecord2.hasReadingQuality(Matchers.any(ReadingQualityType.class))).then(invocationOnMock -> {
            for (ReadingQuality readingQuality : readingRecord2.getReadingQualities()) {
                if (readingQuality.getTypeCode().equals(((ReadingQualityType) invocationOnMock.getArguments()[0]).getCode())) {
                    return true;
                }
            }
            return false;
        });

        doReturn(Unit.WATT_HOUR.amount(BigDecimal.valueOf(5014, 2))).when(readingRecord1).getQuantity(bulkReadingType);
        doReturn(Unit.WATT_HOUR.amount(BigDecimal.valueOf(54897, 3))).when(readingRecord2).getQuantity(bulkReadingType);
        doReturn(BigDecimal.valueOf(5014, 2)).when(readingRecord1).getValue();
        doReturn(BigDecimal.valueOf(54897, 3)).when(readingRecord2).getValue();

        doReturn(BEFORE.toInstant()).when(channel).getPreviousDateTime(ESTIMATABLE1.toInstant());
        doReturn(AFTER.toInstant()).when(channel).getNextDateTime(ESTIMATABLE3.toInstant());
        doReturn(BEFORE.toInstant()).when(bulkCimChannel).getPreviousDateTime(ESTIMATABLE1.toInstant());
        doReturn(AFTER.toInstant()).when(bulkCimChannel).getNextDateTime(ESTIMATABLE3.toInstant());

        logRecorder = new LogRecorder(Level.ALL);
        LOGGER.addHandler(logRecorder);

        LoggingContext.getCloseableContext().with("rule", "rule");
    }

    private ReadingQualityRecord mockReadingQuality(String code) {
        ReadingQualityRecord readingQuality = mock(ReadingQualityRecord.class);
        ReadingQualityType readingQualityType = new ReadingQualityType(code);
        when(readingQuality.getType()).thenReturn(readingQualityType);
        when(readingQuality.isActual()).thenReturn(true);
        when(readingQuality.getTypeCode()).thenReturn(code);
        return readingQuality;
    }

    @After
    public void tearDown() {
        LOGGER.removeHandler(logRecorder);
        LoggingContext.getCloseableContext().close();
    }

    @Test
    public void testPowerGapFillForDelta() {

        Map<String, Object> properties = new HashMap<>();
        properties.put(MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS, TimeDuration.hours(3));

        Estimator estimator = new PowerGapFill(thesaurus, propertySpecService, properties);
        estimator.init(LOGGER);

        EstimationResult estimationResult = estimator.estimate(singletonList(estimationBlock), QualityCodeSystem.OTHER);

        assertThat(estimationResult.remainingToBeEstimated()).isEmpty();
        assertThat(estimationResult.estimated()).containsExactly(estimationBlock);

        //ZERO value after estimation
        verify(estimatable1).setEstimation(cmpEq(BigDecimal.ZERO));
        verify(estimatable2).setEstimation(cmpEq(BigDecimal.ZERO));
        verify(estimatable3).setEstimation(cmpEq(BigDecimal.valueOf(4757, 3)));
    }

    @Test
    public void testPowerGapFillForBulk() {
        doReturn(bulkReadingType).when(estimationBlock).getReadingType();

        Map<String, Object> properties = new HashMap<>();
        properties.put(MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS, TimeDuration.hours(3));

        Estimator estimator = new PowerGapFill(thesaurus, propertySpecService, properties);
        estimator.init(LOGGER);

        EstimationResult estimationResult = estimator.estimate(singletonList(estimationBlock), QualityCodeSystem.OTHER);

        assertThat(estimationResult.remainingToBeEstimated()).isEmpty();
        assertThat(estimationResult.estimated()).containsExactly(estimationBlock);

        //ZERO value after estimation
        verify(estimatable1).setEstimation(cmpEq(BigDecimal.valueOf(5014, 2)));
        verify(estimatable2).setEstimation(cmpEq(BigDecimal.valueOf(5014, 2)));
        verify(estimatable3).setEstimation(cmpEq(BigDecimal.valueOf(5014, 2)));
    }

    @Test
    public void testPowerGapFillDoesNotEstimateWhenTooManySuspects() {

        Map<String, Object> properties = new HashMap<>();
        properties.put(MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS, TimeDuration.hours(2));

        Estimator estimator = new PowerGapFill(thesaurus, propertySpecService, properties);
        estimator.init(LOGGER);

        EstimationResult estimationResult = estimator.estimate(singletonList(estimationBlock), QualityCodeSystem.OTHER);

        assertThat(estimationResult.estimated()).isEmpty();
        assertThat(estimationResult.remainingToBeEstimated()).containsExactly(estimationBlock);
        assertThat(logRecorder).hasRecordWithMessage(message -> message.startsWith("Failed estimation with rule:")
                && message.endsWith("since its size exceeds the maximum of 2 hours")).atLevel(Level.INFO);
    }

    @Test
    public void testPowerGapFillDoesNotEstimateWhenBeforeReadingNotThere() {
        doReturn(Optional.empty()).when(channel).getReading(BEFORE.toInstant());
        doReturn(Optional.empty()).when(bulkCimChannel).getReading(BEFORE.toInstant());

        Map<String, Object> properties = new HashMap<>();
        properties.put(MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS, TimeDuration.hours(3));

        Estimator estimator = new PowerGapFill(thesaurus, propertySpecService, properties);
        estimator.init(LOGGER);

        EstimationResult estimationResult = estimator.estimate(singletonList(estimationBlock), QualityCodeSystem.OTHER);

        assertThat(estimationResult.estimated()).isEmpty();
        assertThat(estimationResult.remainingToBeEstimated()).containsExactly(estimationBlock);
        assertThat(logRecorder).hasRecordWithMessage(message -> message.startsWith("Failed estimation with rule:")).atLevel(Level.INFO);
    }

    @Test
    public void testPowerGapFillDoesNotEstimateWhenBeforeReadingHasNoBulkValue() {
        doReturn(null).when(readingRecord1).getValue();
        doReturn(null).when(readingRecord1).getQuantity(bulkReadingType);

        Map<String, Object> properties = new HashMap<>();
        properties.put(MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS, TimeDuration.hours(3));

        Estimator estimator = new PowerGapFill(thesaurus, propertySpecService, properties);
        estimator.init(LOGGER);

        EstimationResult estimationResult = estimator.estimate(singletonList(estimationBlock), QualityCodeSystem.OTHER);

        assertThat(estimationResult.estimated()).isEmpty();
        assertThat(estimationResult.remainingToBeEstimated()).containsExactly(estimationBlock);
        assertThat(logRecorder).hasRecordWithMessage(message -> message.startsWith("Failed estimation with rule:")).atLevel(Level.INFO);
    }

    @Test
    public void testPowerGapFillDoesNotEstimateWhenAfterReadingNotThere() {
        doReturn(Optional.empty()).when(channel).getReading(ESTIMATABLE3.toInstant());
        doReturn(Optional.empty()).when(bulkCimChannel).getReading(ESTIMATABLE3.toInstant());

        Map<String, Object> properties = new HashMap<>();
        properties.put(MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS, TimeDuration.hours(3));

        Estimator estimator = new PowerGapFill(thesaurus, propertySpecService, properties);
        estimator.init(LOGGER);

        EstimationResult estimationResult = estimator.estimate(singletonList(estimationBlock), QualityCodeSystem.OTHER);

        assertThat(estimationResult.estimated()).isEmpty();
        assertThat(estimationResult.remainingToBeEstimated()).containsExactly(estimationBlock);
        assertThat(logRecorder).hasRecordWithMessage(message -> message.startsWith("Failed estimation with rule:")).atLevel(Level.INFO);
    }

    @Test
    public void testPowerGapFillDoesNotEstimateWhenAfterReadingHasNoBulkValue() {
        doReturn(null).when(readingRecord2).getValue();
        doReturn(null).when(readingRecord2).getQuantity(bulkReadingType);

        Map<String, Object> properties = new HashMap<>();
        properties.put(MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS, TimeDuration.hours(3));

        Estimator estimator = new PowerGapFill(thesaurus, propertySpecService, properties);
        estimator.init(LOGGER);

        EstimationResult estimationResult = estimator.estimate(singletonList(estimationBlock), QualityCodeSystem.OTHER);

        assertThat(estimationResult.estimated()).isEmpty();
        assertThat(estimationResult.remainingToBeEstimated()).containsExactly(estimationBlock);
        assertThat(logRecorder).hasRecordWithMessage(message -> message.startsWith("Failed estimation with rule:")).atLevel(Level.INFO);
    }

    @Test
    public void testPowerGapFillDoesNotEstimateWhenDeltaHasNoBulkReadingType() {
        doReturn(Optional.empty()).when(deltaReadingType).getBulkReadingType();

        Map<String, Object> properties = new HashMap<>();
        properties.put(MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS, TimeDuration.hours(3));

        Estimator estimator = new PowerGapFill(thesaurus, propertySpecService, properties);
        estimator.init(LOGGER);

        EstimationResult estimationResult = estimator.estimate(singletonList(estimationBlock), QualityCodeSystem.OTHER);

        assertThat(estimationResult.estimated()).isEmpty();
        assertThat(estimationResult.remainingToBeEstimated()).containsExactly(estimationBlock);
        assertThat(logRecorder).hasRecordWithMessage(message -> message.startsWith("Failed estimation with rule:")).atLevel(Level.INFO);
    }

    @Test
    public void testPowerGapFillDoesNotEstimateWhenChannelDoesNotContainTheBulkReadingType() {
        doReturn(Optional.empty()).when(channel).getCimChannel(bulkReadingType);

        Map<String, Object> properties = new HashMap<>();
        properties.put(MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS, TimeDuration.hours(3));

        Estimator estimator = new PowerGapFill(thesaurus, propertySpecService, properties);
        estimator.init(LOGGER);

        EstimationResult estimationResult = estimator.estimate(singletonList(estimationBlock), QualityCodeSystem.OTHER);

        assertThat(estimationResult.estimated()).isEmpty();
        assertThat(estimationResult.remainingToBeEstimated()).containsExactly(estimationBlock);
        assertThat(logRecorder).hasRecordWithMessage(message -> message.startsWith("Failed estimation with rule:")).atLevel(Level.INFO);
    }

    @Test
    public void testPowerGapFillDoesNotEstimateWhenBeforeReadingDoesNotHavePowerDownFlag() {
        doReturn(Collections.emptyList()).when(readingRecord1).getReadingQualities();

        Map<String, Object> properties = new HashMap<>();
        properties.put(MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS, TimeDuration.hours(3));

        Estimator estimator = new PowerGapFill(thesaurus, propertySpecService, properties);
        estimator.init(LOGGER);

        EstimationResult estimationResult = estimator.estimate(singletonList(estimationBlock), QualityCodeSystem.OTHER);

        assertThat(estimationResult.estimated()).isEmpty();
        assertThat(estimationResult.remainingToBeEstimated()).containsExactly(estimationBlock);
        assertThat(logRecorder).hasRecordWithMessage(message -> message.startsWith("Failed estimation with rule:")).atLevel(Level.INFO);
    }

    @Test
    public void testPowerGapFillDoesNotEstimateWhenLastReadingDoesNotHavePowerUpFlag() {
        doReturn(Collections.emptyList()).when(readingRecord2).getReadingQualities();

        Map<String, Object> properties = new HashMap<>();
        properties.put(MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS, TimeDuration.hours(3));

        Estimator estimator = new PowerGapFill(thesaurus, propertySpecService, properties);
        estimator.init(LOGGER);

        EstimationResult estimationResult = estimator.estimate(singletonList(estimationBlock), QualityCodeSystem.OTHER);

        assertThat(estimationResult.estimated()).isEmpty();
        assertThat(estimationResult.remainingToBeEstimated()).containsExactly(estimationBlock);
        assertThat(logRecorder).hasRecordWithMessage(message -> message.startsWith("Failed estimation with rule:")).atLevel(Level.INFO);
    }

    @Test
    public void testPowerGapFillDoesNotEstimateBulkWhenBeforeReadingNotThere() {
        doReturn(bulkReadingType).when(estimationBlock).getReadingType();
        doReturn(Optional.empty()).when(channel).getReading(BEFORE.toInstant());
        doReturn(Optional.empty()).when(bulkCimChannel).getReading(BEFORE.toInstant());

        Map<String, Object> properties = new HashMap<>();
        properties.put(MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS, TimeDuration.hours(3));

        Estimator estimator = new PowerGapFill(thesaurus, propertySpecService, properties);
        estimator.init(LOGGER);

        EstimationResult estimationResult = estimator.estimate(singletonList(estimationBlock), QualityCodeSystem.OTHER);

        assertThat(estimationResult.estimated()).isEmpty();
        assertThat(estimationResult.remainingToBeEstimated()).containsExactly(estimationBlock);
        assertThat(logRecorder).hasRecordWithMessage(message -> message.startsWith("Failed estimation with rule:")).atLevel(Level.INFO);
    }

    @Test
    public void testPowerGapFillDoesNotEstimateBulkWhenBeforeReadingHasNoBulkValue() {
        doReturn(bulkReadingType).when(estimationBlock).getReadingType();
        doReturn(null).when(readingRecord1).getValue();
        doReturn(null).when(readingRecord1).getQuantity(bulkReadingType);

        Map<String, Object> properties = new HashMap<>();
        properties.put(MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS, TimeDuration.hours(3));

        Estimator estimator = new PowerGapFill(thesaurus, propertySpecService, properties);
        estimator.init(LOGGER);

        EstimationResult estimationResult = estimator.estimate(singletonList(estimationBlock), QualityCodeSystem.OTHER);

        assertThat(estimationResult.estimated()).isEmpty();
        assertThat(estimationResult.remainingToBeEstimated()).containsExactly(estimationBlock);
        assertThat(logRecorder).hasRecordWithMessage(message -> message.startsWith("Failed estimation with rule:")).atLevel(Level.INFO);
    }

    @Test
    public void testPowerGapFillDoesNotEstimateBulkWhenAfterReadingNotThere() {
        doReturn(bulkReadingType).when(estimationBlock).getReadingType();
        doReturn(Optional.empty()).when(channel).getReading(AFTER.toInstant());
        doReturn(Optional.empty()).when(bulkCimChannel).getReading(AFTER.toInstant());

        Map<String, Object> properties = new HashMap<>();
        properties.put(MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS, TimeDuration.hours(3));

        Estimator estimator = new PowerGapFill(thesaurus, propertySpecService, properties);
        estimator.init(LOGGER);

        EstimationResult estimationResult = estimator.estimate(singletonList(estimationBlock), QualityCodeSystem.OTHER);

        assertThat(estimationResult.estimated()).isEmpty();
        assertThat(estimationResult.remainingToBeEstimated()).containsExactly(estimationBlock);
        assertThat(logRecorder).hasRecordWithMessage(message -> message.startsWith("Failed estimation with rule:")).atLevel(Level.INFO);
    }

    @Test
    public void testPowerGapFillDoesNotEstimateBulkWhenAfterReadingHasNoBulkValue() {
        doReturn(bulkReadingType).when(estimationBlock).getReadingType();
        doReturn(null).when(readingRecord2).getValue();
        doReturn(null).when(readingRecord2).getQuantity(bulkReadingType);

        Map<String, Object> properties = new HashMap<>();
        properties.put(MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS, TimeDuration.hours(3));

        Estimator estimator = new PowerGapFill(thesaurus, propertySpecService, properties);
        estimator.init(LOGGER);

        EstimationResult estimationResult = estimator.estimate(singletonList(estimationBlock), QualityCodeSystem.OTHER);

        assertThat(estimationResult.estimated()).isEmpty();
        assertThat(estimationResult.remainingToBeEstimated()).containsExactly(estimationBlock);
        assertThat(logRecorder).hasRecordWithMessage(message -> message.startsWith("Failed estimation with rule:")).atLevel(Level.INFO);

    }

    @Test
    public void testPowerGapFillDoesNotEstimateBulkWhenBeforeReadingDoesNotHavePowerDownFlag() {
        doReturn(bulkReadingType).when(estimationBlock).getReadingType();
        doReturn(Collections.emptyList()).when(readingRecord1).getReadingQualities();

        Map<String, Object> properties = new HashMap<>();
        properties.put(MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS, TimeDuration.hours(3));

        Estimator estimator = new PowerGapFill(thesaurus, propertySpecService, properties);
        estimator.init(LOGGER);

        EstimationResult estimationResult = estimator.estimate(singletonList(estimationBlock), QualityCodeSystem.OTHER);

        assertThat(estimationResult.estimated()).isEmpty();
        assertThat(estimationResult.remainingToBeEstimated()).containsExactly(estimationBlock);
        assertThat(logRecorder).hasRecordWithMessage(message -> message.startsWith("Failed estimation with rule:")).atLevel(Level.INFO);

    }

    @Test
    public void testPowerGapFillDoesNotEstimateBulkWhenLastReadingDoesNotHavePowerUpFlag() {
        doReturn(bulkReadingType).when(estimationBlock).getReadingType();
        doReturn(Collections.emptyList()).when(readingRecord2).getReadingQualities();

        Map<String, Object> properties = new HashMap<>();
        properties.put(MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS, TimeDuration.hours(3));

        Estimator estimator = new PowerGapFill(thesaurus, propertySpecService, properties);
        estimator.init(LOGGER);

        EstimationResult estimationResult = estimator.estimate(singletonList(estimationBlock), QualityCodeSystem.OTHER);

        assertThat(estimationResult.estimated()).isEmpty();
        assertThat(estimationResult.remainingToBeEstimated()).containsExactly(estimationBlock);
        assertThat(logRecorder).hasRecordWithMessage(message -> message.startsWith("Failed estimation with rule:")).atLevel(Level.INFO);
    }

    @Test
    public void testPowerGapFillDoesNotEstimateWhenReadingTypeHasNoInterval() {
        doReturn(false).when(deltaReadingType).isRegular();
        doReturn(false).when(bulkReadingType).isRegular();

        Map<String, Object> properties = new HashMap<>();
        properties.put(MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS, TimeDuration.hours(3));

        Estimator estimator = new PowerGapFill(thesaurus, propertySpecService, properties);
        estimator.init(LOGGER);

        EstimationResult estimationResult = estimator.estimate(singletonList(estimationBlock), QualityCodeSystem.OTHER);

        assertThat(estimationResult.estimated()).isEmpty();
        assertThat(estimationResult.remainingToBeEstimated()).containsExactly(estimationBlock);
        assertThat(logRecorder).hasRecordWithMessage(message -> message.startsWith("Failed estimation with rule:")).atLevel(Level.INFO);
    }


    @Test(expected = LocalizedFieldValidationException.class)
    public void testInvalidPropertiesWhenConsecutiveIsZero() {
        EstimationRuleProperties property = estimationRuleProperty(MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS, TimeDuration.months(0));

        Estimator estimator = new PowerGapFill(thesaurus, propertySpecService);

        estimator.validateProperties(singletonMap(property.getName(), property.getValue()));
    }

    @Test(expected = LocalizedFieldValidationException.class)
    public void testInvalidPropertiesWhenConsecutiveIsNegative() {
        EstimationRuleProperties property = estimationRuleProperty(MAX_PERIOD_OF_CONSECUTIVE_SUSPECTS, TimeDuration.months(-1));

        Estimator estimator = new PowerGapFill(thesaurus, propertySpecService);

        estimator.validateProperties(singletonMap(property.getName(), property.getValue()));
    }

    @Test
    public void testGetSupportedApplications() {
        assertThat(new PowerGapFill(thesaurus, propertySpecService).getSupportedQualityCodeSystems())
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
