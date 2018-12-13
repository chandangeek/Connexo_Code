/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;

import javax.validation.ConstraintValidatorContext;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
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
    @Mock
    private Query<ValidationRuleSet> ruleSetQuery;

    @Before
    public void setUp() {
        when(validationRuleSet.getName()).thenReturn(NAME);
        when(validationRuleSet2.getName()).thenReturn(NAME);
        when(validationRuleSet.getId()).thenReturn(0L);
        when(validationRuleSet2.getId()).thenReturn(15L);
        when(validationService.getRuleSetQuery()).thenReturn(ruleSetQuery);
    }

    @Test
    public void testValidReturnsTrue() {
        when(ruleSetQuery.select(any())).thenReturn(Lists.emptyList());

        UniqueValidationRuleSetNameValidator validator = new UniqueValidationRuleSetNameValidator(validationService);

        assertThat(validator.isValid(validationRuleSet, context)).isTrue();
    }

    @Test
    public void testInvalidReturnsFalse() {
        when(ruleSetQuery.select(any())).thenReturn(Lists.newArrayList(validationRuleSet2));

        UniqueValidationRuleSetNameValidator validator = new UniqueValidationRuleSetNameValidator(validationService);

        assertThat(validator.isValid(validationRuleSet, context)).isFalse();
    }

    @Test
    public void testIsValidWhenItselfIsInDb() {
        when(ruleSetQuery.select(any())).thenReturn(Lists.newArrayList(validationRuleSet));

        UniqueValidationRuleSetNameValidator validator = new UniqueValidationRuleSetNameValidator(validationService);

        assertThat(validator.isValid(validationRuleSet, context)).isTrue();
    }

    @Test
    public void testValidDoesNotTouchContext() {
        when(ruleSetQuery.select(any())).thenReturn(Lists.emptyList());

        UniqueValidationRuleSetNameValidator validator = new UniqueValidationRuleSetNameValidator(validationService);

        validator.isValid(validationRuleSet, context);

        verifyZeroInteractions(context);
    }
}
