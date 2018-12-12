/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;

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
public class UniqueEstimationRuleSetNameValidatorTest {
    private static final String NAME = "name";
    @Mock
    private EstimationService estimationService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConstraintValidatorContext context;
    @Mock
    private EstimationRuleSet estimationRuleSet, estimationRuleSet2;
    @Mock
    private Query<EstimationRuleSet> ruleSetQuery;

    @Before
    public void setUp() {
        when(estimationRuleSet.getName()).thenReturn(NAME);
        when(estimationRuleSet2.getName()).thenReturn(NAME);
        when(estimationRuleSet.getId()).thenReturn(0L);
        when(estimationRuleSet2.getId()).thenReturn(15L);
        when(estimationService.getEstimationRuleSetQuery()).thenReturn(ruleSetQuery);
    }

    @Test
    public void testValidReturnsTrue() {
        when(ruleSetQuery.select(any())).thenReturn(Lists.emptyList());

        UniqueEstimationRuleSetNameValidator validator = new UniqueEstimationRuleSetNameValidator(estimationService);

        assertThat(validator.isValid(estimationRuleSet, context)).isTrue();
    }

    @Test
    public void testInvalidReturnsFalse() {
        when(ruleSetQuery.select(any())).thenReturn(Lists.newArrayList(estimationRuleSet2));

        UniqueEstimationRuleSetNameValidator validator = new UniqueEstimationRuleSetNameValidator(estimationService);

        assertThat(validator.isValid(estimationRuleSet, context)).isFalse();
    }

    @Test
    public void testIsValidWhenItselfIsInDb() {
        when(ruleSetQuery.select(any())).thenReturn(Lists.newArrayList(estimationRuleSet));

        UniqueEstimationRuleSetNameValidator validator = new UniqueEstimationRuleSetNameValidator(estimationService);

        assertThat(validator.isValid(estimationRuleSet, context)).isTrue();
    }

    @Test
    public void testValidDoesNotTouchContext() {
        when(ruleSetQuery.select(any())).thenReturn(Lists.emptyList());

        UniqueEstimationRuleSetNameValidator validator = new UniqueEstimationRuleSetNameValidator(estimationService);

        validator.isValid(estimationRuleSet, context);

        verifyZeroInteractions(context);
    }
}
