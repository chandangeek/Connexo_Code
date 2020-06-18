/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationPropertyDefinitionLevel;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.Validator;
import com.elster.jupiter.validation.ValidatorFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidationRuleSetImplIT {
    private static final String CONSEC_ZEROS_VALIDATOR_CLASS = "com.elster.jupiter.validators.ConsecutiveZerosValidator";
    private static final String MAX_NUMBER_IN_SEQUENCE = "maxNumberInSequence";
    private static final String MIN_MAX = "minMax";
    private static final String MIN = "min";
    private static final String MAX = "max";

    private static ValidationInMemoryBootstrapModule inMemoryBootstrapModule = new ValidationInMemoryBootstrapModule("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
    private static ValidationService validationService;
    private static ReadingType readingType;

    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.get(TransactionService.class));

    @BeforeClass
    public static void classSetUp() throws SQLException {
        inMemoryBootstrapModule.activate();
        validationService = inMemoryBootstrapModule.get(ValidationService.class);
        ValidatorFactory validatorFactory = mock(ValidatorFactory.class);
        Validator minMax = mock(Validator.class);
        Validator consecZero = mock(Validator.class);
        PropertySpec min = mock(PropertySpec.class);
        PropertySpec max = mock(PropertySpec.class);
        PropertySpec consZero = mock(PropertySpec.class);
        when(validatorFactory.available()).thenReturn(Arrays.asList(MIN_MAX, CONSEC_ZEROS_VALIDATOR_CLASS));
        when(validatorFactory.createTemplate(eq(MIN_MAX))).thenReturn(minMax);
        when(validatorFactory.createTemplate(eq(CONSEC_ZEROS_VALIDATOR_CLASS))).thenReturn(consecZero);
        validationService.addValidatorFactory(validatorFactory);
        BigDecimalFactory valueFactory = new BigDecimalFactory();
        when(minMax.getPropertySpecs()).thenReturn(Arrays.asList(min, max));
        when(minMax.getPropertySpecs(ValidationPropertyDefinitionLevel.VALIDATION_RULE)).thenReturn(Arrays.asList(min, max));
        when(min.getName()).thenReturn(MIN);
        when(min.getValueFactory()).thenReturn(valueFactory);
        when(max.getName()).thenReturn(MAX);
        when(max.getValueFactory()).thenReturn(valueFactory);
        when(consecZero.getPropertySpecs()).thenReturn(Collections.singletonList(consZero));
        when(consecZero.getPropertySpecs(ValidationPropertyDefinitionLevel.VALIDATION_RULE)).thenReturn(Collections.singletonList(consZero));
        when(consZero.getName()).thenReturn(MAX_NUMBER_IN_SEQUENCE);
        when(consZero.getValueFactory()).thenReturn(valueFactory);
        readingType = inMemoryBootstrapModule.get(MeteringService.class)
                .getReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();
    }

    @AfterClass
    public static void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    @Transactional
    public void testPersist() {
        ValidationRuleSet validationRuleSet = validationService.createValidationRuleSet("myRuleSet", QualityCodeSystem.MDC);
        ValidationRuleSetVersion validationRuleSetVersion = validationRuleSet.addRuleSetVersion("description", Instant.EPOCH);
        validationRuleSetVersion.addRule(ValidationAction.FAIL, CONSEC_ZEROS_VALIDATOR_CLASS, "consecutiveZeroes")
                .withReadingType(readingType)
                .havingProperty(MAX_NUMBER_IN_SEQUENCE).withValue(BigDecimal.valueOf(20))
                .active(true)
                .create();
        validationRuleSetVersion.addRule(ValidationAction.WARN_ONLY, MIN_MAX, "minmax")
                .withReadingType(readingType)
                .havingProperty(MIN).withValue(BigDecimal.valueOf(1))
                .havingProperty(MAX).withValue(BigDecimal.valueOf(100))
                .active(true)
                .create();

        Optional<? extends ValidationRuleSet> found = validationService.getValidationRuleSet(validationRuleSet.getId());
        assertThat(found.isPresent()).isTrue();
        assertThat(found.get().getRuleSetVersions().get(0).getRules()).hasSize(2);
    }

    @Test
    @Transactional
    public void testAddSecondRuleSeeIfReadingTypesAreNotLost() {
        ValidationRuleSet validationRuleSet;
        validationRuleSet = validationService.createValidationRuleSet("myRuleSet", QualityCodeSystem.MDC);
        ValidationRuleSetVersion validationRuleSetVersion = validationRuleSet.addRuleSetVersion("description", Instant.EPOCH);
        validationRuleSetVersion.addRule(ValidationAction.FAIL, CONSEC_ZEROS_VALIDATOR_CLASS, "consecutiveZeroes")
                .withReadingType(readingType)
                .havingProperty(MAX_NUMBER_IN_SEQUENCE).withValue(BigDecimal.valueOf(20))
                .active(true)
                .create();
        validationRuleSet = validationService.getValidationRuleSet(validationRuleSet.getId()).get();
        validationRuleSetVersion = validationRuleSet.getRuleSetVersions().get(0);
        validationRuleSetVersion.addRule(ValidationAction.WARN_ONLY, MIN_MAX, "minmax")
                .withReadingType(readingType)
                .havingProperty(MIN).withValue(BigDecimal.valueOf(1))
                .havingProperty(MAX).withValue(BigDecimal.valueOf(100))
                .active(true)
                .create();
        validationRuleSet = validationService.getValidationRuleSet(validationRuleSet.getId()).get();
        assertThat(validationRuleSet.getRuleSetVersions().get(0).getRules()).hasSize(2);
        ValidationRule validationRule = validationRuleSet.getRuleSetVersions().get(0).getRules().get(0);
        assertThat(validationRule.getReadingTypes()).hasSize(1);
    }

    @Test
    @Transactional
    public void testAddSecondRuleSeeIfPropertiesAreNotLost() {
        ValidationRuleSet validationRuleSet;
        validationRuleSet = validationService.createValidationRuleSet("myRuleSet", QualityCodeSystem.MDC);
        ValidationRuleSetVersion validationRuleSetVersion = validationRuleSet.addRuleSetVersion("description", Instant.EPOCH);
        validationRuleSetVersion.addRule(ValidationAction.FAIL, CONSEC_ZEROS_VALIDATOR_CLASS, "consecutiveZeroes")
                .withReadingType(readingType)
                .havingProperty(MAX_NUMBER_IN_SEQUENCE).withValue(BigDecimal.valueOf(20))
                .active(true)
                .create();
        validationRuleSet = validationService.getValidationRuleSet(validationRuleSet.getId()).get();
        validationRuleSetVersion = validationRuleSet.getRuleSetVersions().get(0);
        validationRuleSetVersion.addRule(ValidationAction.WARN_ONLY, MIN_MAX, "minmax")
                .withReadingType(readingType)
                .havingProperty(MIN).withValue(BigDecimal.valueOf(1))
                .havingProperty(MAX).withValue(BigDecimal.valueOf(100))
                .active(true)
                .create();
        validationRuleSet = validationService.getValidationRuleSet(validationRuleSet.getId()).get();
        assertThat(validationRuleSet.getRuleSetVersions().get(0).getRules()).hasSize(2);
        ValidationRule validationRule = validationRuleSet.getRuleSetVersions().get(0).getRules().get(0);
        assertThat(validationRule.getProperties()).hasSize(1);
    }
}
