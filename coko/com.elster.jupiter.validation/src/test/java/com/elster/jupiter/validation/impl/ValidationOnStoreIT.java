/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.beans.IntervalBlockImpl;
import com.elster.jupiter.metering.readings.beans.IntervalReadingImpl;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationContext;
import com.elster.jupiter.validation.ValidationPropertyDefinitionLevel;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetResolver;
import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.Validator;
import com.elster.jupiter.validation.ValidatorFactory;

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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests integration of all internal components involved in validation.
 * Only interfaces of outside the bundle and implementations of Validator (since there are no internal implementations by design) have been mocked.
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidationOnStoreIT {
    private static final String MIN_MAX = "minMax";
    private static final String CONSECUTIVE_ZEROES = "consecutiveZeroes";
    private static final String MY_RULE_SET = "MyRuleSet";
    private static final String MAX_NUMBER_IN_SEQUENCE = "maxNumberInSequence";
    private static final String MIN = "min";
    private static final String MAX = "max";
    private static final ZonedDateTime ZONED_DATE_TIME = ZonedDateTime.of(1983, 5, 31, 14, 0, 0, 0, ZoneId.systemDefault());
    private static final Instant date1 = ZONED_DATE_TIME.toInstant();

    private static ValidationInMemoryBootstrapModule inMemoryBootstrapModule = new ValidationInMemoryBootstrapModule(
            "0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0", "0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
    private static ValidationService validationService;
    private static ReadingType deltaReadingType, bulkReadingType;
    private static Meter meter;
    private static MeterActivation meterActivation;
    private static ValidationRule minMaxRule, zeroesRule;

    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.get(TransactionService.class));

    @BeforeClass
    public static void setUp() {
        inMemoryBootstrapModule.activate();
        validationService = inMemoryBootstrapModule.get(ValidationService.class);

        ValidatorFactory validatorFactory = mock(ValidatorFactory.class);
        Validator minMax = mock(Validator.class);
        Validator conseqZero = mock(Validator.class);
        PropertySpec min = mock(PropertySpec.class);
        PropertySpec max = mock(PropertySpec.class);
        PropertySpec conZero = mock(PropertySpec.class);
        BigDecimalFactory valueFactory = new BigDecimalFactory();
        when(validatorFactory.available()).thenReturn(Arrays.asList(MIN_MAX, CONSECUTIVE_ZEROES));
        when(validatorFactory.createTemplate(eq(MIN_MAX))).thenReturn(minMax);
        when(validatorFactory.createTemplate(eq(CONSECUTIVE_ZEROES))).thenReturn(conseqZero);
        when(validatorFactory.create(eq(CONSECUTIVE_ZEROES), anyMapOf(String.class, Object.class))).thenReturn(conseqZero);
        when(validatorFactory.create(eq(MIN_MAX), anyMapOf(String.class, Object.class))).thenReturn(minMax);
        when(minMax.getReadingQualityCodeIndex()).thenReturn(Optional.empty());
        when(minMax.getPropertySpecs()).thenReturn(Arrays.asList(min, max));
        when(minMax.getPropertySpecs(ValidationPropertyDefinitionLevel.VALIDATION_RULE)).thenReturn(Arrays.asList(min, max));
        when(minMax.validate(any(IntervalReadingRecord.class))).thenReturn(ValidationResult.SUSPECT);
        when(min.getName()).thenReturn(MIN);
        when(min.getValueFactory()).thenReturn(valueFactory);
        when(max.getName()).thenReturn(MAX);
        when(max.getValueFactory()).thenReturn(valueFactory);
        when(conseqZero.getReadingQualityCodeIndex()).thenReturn(Optional.empty());
        when(conseqZero.getPropertySpecs()).thenReturn(Collections.singletonList(conZero));
        when(conseqZero.getPropertySpecs(ValidationPropertyDefinitionLevel.VALIDATION_RULE)).thenReturn(Collections.singletonList(conZero));
        when(conZero.getName()).thenReturn(MAX_NUMBER_IN_SEQUENCE);
        when(conZero.getValueFactory()).thenReturn(valueFactory);
        validationService.addValidatorFactory(validatorFactory);

        MeteringService meteringService = inMemoryBootstrapModule.get(MeteringService.class);
        deltaReadingType = meteringService.getReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
        bulkReadingType = meteringService.getReadingType("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();

        inMemoryBootstrapModule.get(TransactionService.class).run(() -> {
            AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
            meter = amrSystem.newMeter("2331", "myName").create();
            meterActivation = meter.activate(date1);
            meterActivation.getChannelsContainer().createChannel(bulkReadingType);

            final ValidationRuleSet validationRuleSet = validationService.createValidationRuleSet(MY_RULE_SET, QualityCodeSystem.MDC);
            ValidationRuleSetVersion validationRuleSetVersion = validationRuleSet.addRuleSetVersion("description", Instant.EPOCH);
            zeroesRule = validationRuleSetVersion.addRule(ValidationAction.WARN_ONLY, CONSECUTIVE_ZEROES, "consecutivezeros")
                    .withReadingType(deltaReadingType)
                    .withReadingType(bulkReadingType)
                    .havingProperty(MAX_NUMBER_IN_SEQUENCE).withValue(BigDecimal.valueOf(20))
                    .active(true)
                    .create();
            minMaxRule = validationRuleSetVersion.addRule(ValidationAction.FAIL, MIN_MAX, "minmax")
                    .withReadingType(bulkReadingType)
                    .havingProperty(MIN).withValue(BigDecimal.valueOf(1))
                    .havingProperty(MAX).withValue(BigDecimal.valueOf(100))
                    .active(true)
                    .create();

            validationService.addValidationRuleSetResolver(new ValidationRuleSetResolver() {
                @Override
                public Map<ValidationRuleSet, RangeSet<Instant>> resolve(ValidationContext validationContext) {
                    RangeSet<Instant> rangeSet = TreeRangeSet.create();
                    rangeSet.add(Range.atLeast(date1));
                    return Collections.singletonMap(validationRuleSet, rangeSet);
                }

                @Override
                public boolean isValidationRuleSetInUse(ValidationRuleSet ruleset) {
                    return false;
                }

                @Override
                public boolean isValidationRuleSetActiveOnDeviceConfig(long validationRuleSetId, long deviceConfigId) {
                    return true;
                }

                @Override
                public boolean canHandleRuleSetStatus() {
                    return true;
                }
            });

            validationService.activateValidation(meter);
            validationService.enableValidationOnStorage(meter);
        });
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    @Transactional
    public void testValidationFailsOnMinMax() {
        IntervalReadingImpl intervalReading = IntervalReadingImpl.of(ZONED_DATE_TIME.plusHours(1).toInstant(), BigDecimal.valueOf(400), Collections.emptySet());
        MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
        IntervalBlockImpl intervalBlock = IntervalBlockImpl.of(bulkReadingType.getMRID());
        intervalBlock.addIntervalReading(intervalReading);
        meterReading.addIntervalBlock(intervalBlock);
        meter.store(QualityCodeSystem.MDC, meterReading);

        List<ReadingQualityType> readingQualityTypes = meter.getReadingQualities(Range.singleton(ZONED_DATE_TIME.plusHours(1).toInstant())).stream()
                .map(ReadingQualityRecord::getType)
                .collect(Collectors.toList());

        assertThat(readingQualityTypes).containsOnly(ReadingQualityType.of(QualityCodeSystem.MDC, QualityCodeIndex.SUSPECT),
                ReadingQualityType.defaultCodeForRuleId(QualityCodeSystem.MDC, minMaxRule.getId()));
    }
}
