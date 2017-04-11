/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.SimpleNlsKey;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.impl.PropertySpecServiceImpl;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.validation.ValidationResult;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConsecutiveValidatorTest {

    public static final BigDecimal MINIMUM_THRESHOLD = BigDecimal.valueOf(0.8);
    public static final BigDecimal BELOW_MINIMUM = BigDecimal.valueOf(0.5);
    public static final BigDecimal ABOVE_MINIMUM = BigDecimal.ONE;
    public static final TimeDuration MINIMUM_PERIOD = TimeDuration.hours(2);
    public static final TimeDuration MAXIMUM_PERIOD = TimeDuration.days(1);
    public static final String CONSECUTIVE_ZERO = "Consecutive zero's";

    @Mock
    private Thesaurus thesaurus;

    private PropertySpecService propertySpecService = new PropertySpecServiceImpl();
    @Mock
    private IntervalReadingRecord intervalReadingRecord;
    @Mock
    private ReadingRecord readingRecord;
    @Mock
    private Channel channel;
    @Mock
    private ReadingType readingType;

    private ConsecutiveValidator consecutiveValidator;

    @Before
    public void setUp() {
        when(readingType.getAccumulation()).thenReturn(Accumulation.DELTADELTA);
        ImmutableMap<String, Object> properties = ImmutableMap.of(ConsecutiveValidator.MINIMUM_PERIOD, MINIMUM_PERIOD,
                ConsecutiveValidator.MAXIMUM_PERIOD, MAXIMUM_PERIOD,
                ConsecutiveValidator.MINIMUM_THRESHOLD, MINIMUM_THRESHOLD);
        consecutiveValidator = new ConsecutiveValidator(thesaurus, propertySpecService, properties);

        //Range<Instant> validationInterval = Range.openClosed(Instant.ofEpochSecond(10000L), Instant.ofEpochSecond(25000L));
        //Range<Instant> zeroInterval = Range.openClosed(Instant.ofEpochSecond(5000L), Instant.ofEpochSecond(15000L));
        //consecutiveValidator.init(channel, readingType, validationInterval);
    }

    @Test
    public void testValidationOk() {
    }





    @Test
    public void testGetDefaultFormat() {
        assertThat(consecutiveValidator.getDefaultFormat()).isEqualTo(CONSECUTIVE_ZERO);
    }

    @Test
    public void testNlsKey() {
        assertThat(consecutiveValidator.getNlsKey()).isEqualTo(SimpleNlsKey.key(MessageSeeds.COMPONENT_NAME, Layer.DOMAIN, ConsecutiveValidator.class.getName()));
    }

    @Test
    public void testDisplayName() {
        when(thesaurus.getString(ConsecutiveValidator.class.getName(), CONSECUTIVE_ZERO)).thenReturn(CONSECUTIVE_ZERO + " en français");

        assertThat(consecutiveValidator.getDisplayName()).isEqualTo(CONSECUTIVE_ZERO + " en français");
    }

    @Test
    public void testPropertyDisplayName() {
        NlsMessageFormat minPeriodMessageFormat = mock(NlsMessageFormat.class);
        when(minPeriodMessageFormat.format()).thenReturn("Minimum period");
        when(thesaurus.getFormat(TranslationKeys.CONSECUTIVE_VALIDATOR_MIN_PERIOD)).thenReturn(minPeriodMessageFormat);
        NlsMessageFormat maxPeriodMessageFormat = mock(NlsMessageFormat.class);
        when(maxPeriodMessageFormat.format()).thenReturn("Maximum period");
        when(thesaurus.getFormat(TranslationKeys.CONSECUTIVE_VALIDATOR_MAX_PERIOD)).thenReturn(maxPeriodMessageFormat);
        NlsMessageFormat minThresholdMessageFormat = mock(NlsMessageFormat.class);
        when(minThresholdMessageFormat.format()).thenReturn("Minimum threshold");
        when(thesaurus.getFormat(TranslationKeys.CONSECUTIVE_VALIDATOR_MAX_PERIOD)).thenReturn(minThresholdMessageFormat);

        // Business method
        String minPeriodDisplayName = consecutiveValidator.getDisplayName(ConsecutiveValidator.MINIMUM_PERIOD);
        String maxPeriodDisplayName = consecutiveValidator.getDisplayName(ConsecutiveValidator.MAXIMUM_PERIOD);
        String minThresholdDisplayName = consecutiveValidator.getDisplayName(ConsecutiveValidator.MINIMUM_THRESHOLD);

        // Asserts
        assertThat(minPeriodDisplayName).isEqualTo("Minimum period");
        assertThat(maxPeriodDisplayName).isEqualTo("Maximum period");
        assertThat(minThresholdDisplayName).isEqualTo("Minimum threshold");
    }

    @Test
    public void testFinish() {
        assertThat(consecutiveValidator.finish()).isEmpty();
    }

    @Test
    public void testGetReadingQualityTypeCode() {
        assertThat(consecutiveValidator.getReadingQualityCodeIndex().isPresent()).isFalse();
    }

    @Test
    public void testGetSupportedApplications() {
        assertThat(consecutiveValidator.getSupportedQualityCodeSystems()).containsOnly(QualityCodeSystem.MDC, QualityCodeSystem.MDM);
    }
}
