/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.Validator;
import com.elster.jupiter.validation.ValidatorNotFoundException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExistingValidatorValidatorTest {

    @Mock
    private ValidationService validationService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConstraintValidatorContext context;
    @Mock
    private Validator validator;

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testValidReturnsTrue() {
        String willBeFound = "willBeFound";

        when(validationService.getValidator(willBeFound)).thenReturn(validator);

        assertThat(new ExistingValidatorValidator(validationService).isValid(willBeFound, context)).isTrue();
    }

    @Test
    public void testValidDoesNotTouchTheContext() {
        String willBeFound = "willBeFound";

        when(validationService.getValidator(willBeFound)).thenReturn(validator);

        new ExistingValidatorValidator(validationService).isValid(willBeFound, context);

        verifyZeroInteractions(context);
    }

    @Test
    public void testInvalidReturnsFalse() {
        String willBeFound = "willBeFound";

        when(validationService.getValidator(willBeFound)).thenThrow(ValidatorNotFoundException.class);

        assertThat(new ExistingValidatorValidator(validationService).isValid(willBeFound, context)).isFalse();
    }


}