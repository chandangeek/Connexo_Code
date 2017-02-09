/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;

import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UniqueValidationRuleSetNameValidatorTest {
    private static final String NAME = "name";
    @Mock
    private ValidationService validationService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConstraintValidatorContext context;
    @Mock
    private ValidationRuleSet validationRuleSet, validationRuleSet2;

    @Before
    public void setUp() {
        when(validationRuleSet.getName()).thenReturn(NAME);
        when(validationRuleSet2.getName()).thenReturn(NAME);
        when(validationRuleSet.getId()).thenReturn(0L);
        when(validationRuleSet2.getId()).thenReturn(15L);
    }

    @Test
    public void testValidReturnsTrue() {
        when(validationService.getValidationRuleSet(NAME)).thenReturn(Optional.empty());

        UniqueValidationRuleSetNameValidator validator = new UniqueValidationRuleSetNameValidator(validationService);

        assertThat(validator.isValid(validationRuleSet, context)).isTrue();
    }

    @Test
    public void testInvalidReturnsFalse() {
        when(validationService.getValidationRuleSet(NAME)).thenReturn(Optional.of(validationRuleSet2));

        UniqueValidationRuleSetNameValidator validator = new UniqueValidationRuleSetNameValidator(validationService);

        assertThat(validator.isValid(validationRuleSet, context)).isFalse();
    }

    @Test
    public void testIsValidWhenItselfIsInDb() {
        when(validationService.getValidationRuleSet(NAME)).thenReturn(Optional.of(validationRuleSet));

        UniqueValidationRuleSetNameValidator validator = new UniqueValidationRuleSetNameValidator(validationService);

        assertThat(validator.isValid(validationRuleSet, context)).isTrue();
    }

    @Test
    public void testValidDoesNotTouchContext() {
        when(validationService.getValidationRuleSet(NAME)).thenReturn(Optional.empty());

        UniqueValidationRuleSetNameValidator validator = new UniqueValidationRuleSetNameValidator(validationService);

        validator.isValid(validationRuleSet, context);

        verifyZeroInteractions(context);
    }
}
