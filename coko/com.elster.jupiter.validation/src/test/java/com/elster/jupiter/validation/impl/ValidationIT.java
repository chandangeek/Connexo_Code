/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationContextImpl;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetResolver;
import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.Validator;
import com.elster.jupiter.validation.ValidatorFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidationIT {

    private static final String RULE_SET_NAME = "MyRuleSet";
    private static final String RULE_NAME_1 = "MyRule1";
    private static final String RULE_NAME_2 = "MyRule2";
    private static final String READING_TYPE_1 = "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    private static final String READING_TYPE_2 = "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0";
    private static final String READING_TYPE_3 = "0.0.2.4.19.1.12.0.0.0.0.0.0.0.0.3.72.0";
    private static final String CONSECUTIVE_ZEROES = "consecutiveZeroes";
    private static final String MIN_MAX = "minMax";
    private static final Instant TIMESTAMP = ZonedDateTime.of(1983, 5, 31, 14, 0, 0, 0, ZoneId.systemDefault()).toInstant();

    private static ValidationInMemoryBootstrapModule inMemoryBootstrapModule = new ValidationInMemoryBootstrapModule(READING_TYPE_1, READING_TYPE_2, READING_TYPE_3);

    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.get(TransactionService.class));

    @Mock
    private ValidatorFactory validatorFactory;
    @Mock
    private Validator minMax;
    @Mock
    private Validator consecutiveZeros;
    @Mock
    private ValidationRuleSetResolver validationRuleSetResolver;

    private Meter meter;

    private MeterActivation meterActivation;

    private RangeSet<Instant> rangeSet = TreeRangeSet.create();

    @BeforeClass
    public static void beforeClass() {
        inMemoryBootstrapModule.activate();
    }

    @AfterClass
    public static void afterClass() {
        inMemoryBootstrapModule.deactivate();
    }

    @Before
    public void before() {
        rangeSet.add(Range.atLeast(Instant.EPOCH));
        MeteringService meteringService = inMemoryBootstrapModule.get(MeteringService.class);
        ReadingType readingType1 = meteringService.getReadingType(READING_TYPE_1).get();
        ReadingType readingType2 = meteringService.getReadingType(READING_TYPE_2).get();
        ReadingType readingType3 = meteringService.getReadingType(READING_TYPE_3).get();
        AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
        meter = amrSystem.newMeter("amrId", "meter").create();
        meterActivation = meter.activate(TIMESTAMP);
        meterActivation.getChannelsContainer().createChannel(readingType1, readingType2);
        meterActivation.getChannelsContainer().createChannel(readingType1, readingType3);

        mockValidators();
        ValidationServiceImpl validationService = getValidationService();
        validationService.addResource(validatorFactory);
        ValidationRuleSet validationRuleSet = createValidationRuleSet(readingType1, readingType2, readingType3, validationService);
        when(validationRuleSetResolver.resolve(any())).thenReturn(Collections.singletonMap(validationRuleSet, rangeSet));
        validationService.addValidationRuleSetResolver(validationRuleSetResolver);

        validationService.activateValidation(meter);
        validationService.enableValidationOnStorage(meter);
    }

    @After
    public void after() {
        getValidationService().removeValidationRuleSetResolver(validationRuleSetResolver);
    }

    private ValidationServiceImpl getValidationService() {
        return (ValidationServiceImpl) inMemoryBootstrapModule.get(ValidationService.class);
    }

    private void mockValidators() {
        when(validatorFactory.createTemplate(MIN_MAX)).thenReturn(minMax);
        when(validatorFactory.create(eq(MIN_MAX), anyMapOf(String.class, Object.class))).thenReturn(minMax);
        when(minMax.getReadingQualityCodeIndex()).thenReturn(Optional.empty());
        when(validatorFactory.createTemplate(CONSECUTIVE_ZEROES)).thenReturn(consecutiveZeros);
        when(validatorFactory.create(eq(CONSECUTIVE_ZEROES), anyMapOf(String.class, Object.class))).thenReturn(consecutiveZeros);
        when(consecutiveZeros.getReadingQualityCodeIndex()).thenReturn(Optional.empty());
        when(validatorFactory.available()).thenReturn(Arrays.asList(MIN_MAX, CONSECUTIVE_ZEROES));
    }

    private ValidationRuleSet createValidationRuleSet(ReadingType readingType1, ReadingType readingType2, ReadingType readingType3, ValidationServiceImpl validationService) {
        ValidationRuleSet validationRuleSet = validationService.createValidationRuleSet(RULE_SET_NAME, QualityCodeSystem.MDC);
        ValidationRuleSetVersion validationRuleSetVersion = validationRuleSet.addRuleSetVersion("description", Instant.EPOCH);
        validationRuleSetVersion.addRule(ValidationAction.WARN_ONLY, MIN_MAX, RULE_NAME_1)
                .withReadingType(readingType1, readingType2)
                .active(true)
                .create();
        validationRuleSetVersion.addRule(ValidationAction.FAIL, CONSECUTIVE_ZEROES, RULE_NAME_2)
                .withReadingType(readingType2, readingType3)
                .active(true)
                .create();
        return validationRuleSet;
    }

    @Test
    @Transactional
    public void testValidationOnStorage() {
        ReadingType readingType = inMemoryBootstrapModule.get(MeteringService.class).getReadingType(READING_TYPE_1).get();
        Channel channel = meterActivation.getChannelsContainer().getChannel(readingType).get();

        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        meterReading.addReading(ReadingImpl.of(READING_TYPE_1, BigDecimal.valueOf(10L), TIMESTAMP.plusSeconds(900)));
        meterReading.addReading(ReadingImpl.of(READING_TYPE_1, BigDecimal.valueOf(11L), TIMESTAMP.plusSeconds(900 * 2)));
        meterReading.addReading(ReadingImpl.of(READING_TYPE_1, BigDecimal.valueOf(12L), TIMESTAMP.plusSeconds(900 * 3)));

        // Business method
        meter.store(QualityCodeSystem.MDC, meterReading);

        // Asserts that all readings validated because "validation on store" enabled
        assertThat(getChannelValidation(channel).getLastChecked()).isEqualTo(TIMESTAMP.plusSeconds(900 * 3));

        // Edit some reading
        BaseReadingRecord editedReading = channel.getReadings(Range.openClosed(TIMESTAMP, TIMESTAMP.plusSeconds(900))).get(0);
        channel.editReadings(QualityCodeSystem.MDC, Collections.singletonList(editedReading));
        // Asserts that no last check reset
        assertThat(getChannelValidation(channel).getLastChecked()).isEqualTo(TIMESTAMP.plusSeconds(900 * 3));

        // Remove one reading, expecting no last check reset!
        BaseReadingRecord removedReading = channel.getReadings(Range.openClosed(TIMESTAMP, TIMESTAMP.plusSeconds(900))).get(0);
        channel.removeReadings(QualityCodeSystem.MDC, Collections.singletonList(removedReading));

        // Asserts that no last check rest
        assertThat(getChannelValidation(channel).getLastChecked()).isEqualTo(TIMESTAMP.plusSeconds(900 * 3));
    }

    @Test
    @Transactional
    public void testChannelContainerValidation() {
        // Business method
        getValidationService().validate(Collections.emptySet(), meterActivation.getChannelsContainer());

        // Asserts
        List<ChannelsContainerValidation> channelsContainerValidations = getChannelsContainerValidations(meterActivation.getChannelsContainer());
        assertThat(channelsContainerValidations).hasSize(1);
        assertThat(channelsContainerValidations.get(0).getRuleSet().getName()).isEqualTo(RULE_SET_NAME);
        assertThat(channelsContainerValidations.get(0).isObsolete()).isFalse();
        assertThat(channelsContainerValidations.get(0).getChannelValidations()).hasSize(2);
    }

    @Test
    @Transactional
    public void testValidateOnRange() {
        ReadingType readingType = inMemoryBootstrapModule.get(MeteringService.class).getReadingType(READING_TYPE_1).get();
        Channel channel = meterActivation.getChannelsContainer().getChannel(readingType).get();

        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        meterReading.addReading(ReadingImpl.of(READING_TYPE_1, BigDecimal.valueOf(10L), TIMESTAMP.plusSeconds(900)));
        meterReading.addReading(ReadingImpl.of(READING_TYPE_1, BigDecimal.valueOf(11L), TIMESTAMP.plusSeconds(900 * 2)));
        meterReading.addReading(ReadingImpl.of(READING_TYPE_1, BigDecimal.valueOf(12L), TIMESTAMP.plusSeconds(900 * 3)));

        // Store initial readings
        meter.store(QualityCodeSystem.MDC, meterReading);

        // Asserts that all readings validated because validation on store enabled
        assertThat(getChannelValidation(channel).getLastChecked()).isEqualTo(TIMESTAMP.plusSeconds(900 * 3));

        // Disable validation on storage and add more readings after
        getValidationService().disableValidationOnStorage(meter);
        meterReading = MeterReadingImpl.newInstance();
        meterReading.addReading(ReadingImpl.of(READING_TYPE_1, BigDecimal.valueOf(13L), TIMESTAMP.plusSeconds(900 * 4)));
        meterReading.addReading(ReadingImpl.of(READING_TYPE_1, BigDecimal.valueOf(14L), TIMESTAMP.plusSeconds(900 * 5)));
        meterReading.addReading(ReadingImpl.of(READING_TYPE_1, BigDecimal.valueOf(15L), TIMESTAMP.plusSeconds(900 * 6)));
        meter.store(QualityCodeSystem.MDC, meterReading);

        // Asserts that last checked date still the same
        assertThat(getChannelValidation(channel).getLastChecked()).isEqualTo(TIMESTAMP.plusSeconds(900 * 3));

        // Revalidate readings starting before last checked
        getValidationService().validate(new ValidationContextImpl(
                ImmutableSet.of(QualityCodeSystem.MDC), channel.getChannelsContainer(), readingType), Range.closed(TIMESTAMP, TIMESTAMP.plusSeconds(900 * 4)));
        // Asserts that last checked has been moved forward
        assertThat(getChannelValidation(channel).getLastChecked()).isEqualTo(TIMESTAMP.plusSeconds(900 * 4));

        // Validate readings after last checked
        getValidationService().validate(new ValidationContextImpl(
                ImmutableSet.of(QualityCodeSystem.MDC), channel.getChannelsContainer(), readingType), Range.singleton(TIMESTAMP.plusSeconds(900 * 5)));
        // Asserts that last checked has been moved forward
        assertThat(getChannelValidation(channel).getLastChecked()).isEqualTo(TIMESTAMP.plusSeconds(900 * 5));

        // Validate all readings until the end of data
        getValidationService().validate(
                new ValidationContextImpl(ImmutableSet.of(QualityCodeSystem.MDC), channel.getChannelsContainer(), readingType), Range.atLeast(TIMESTAMP));
        // Asserts that last checked is the same as last reading time
        assertThat(getChannelValidation(channel).getLastChecked()).isEqualTo(TIMESTAMP.plusSeconds(900 * 6));
    }

    private List<ChannelsContainerValidation> getChannelsContainerValidations(ChannelsContainer channelsContainer) {
        DataModel dataModel = inMemoryBootstrapModule.get(OrmService.class).getDataModel(ValidationService.COMPONENTNAME).get();
        return dataModel.mapper(ChannelsContainerValidation.class).find("channelsContainer", channelsContainer);
    }

    private ChannelValidation getChannelValidation(Channel channel) {
        return getChannelsContainerValidations(channel.getChannelsContainer()).stream()
                .map(channelsContainerValidation -> channelsContainerValidation.getChannelValidation(channel))
                .flatMap(Functions.asStream())
                .findAny()
                .get();
    }
}
