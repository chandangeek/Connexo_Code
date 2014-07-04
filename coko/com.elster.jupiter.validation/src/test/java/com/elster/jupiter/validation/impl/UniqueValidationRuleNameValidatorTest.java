package com.elster.jupiter.validation.impl;

import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.validation.ConstraintValidatorContext;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UniqueValidationRuleNameValidatorTest {

    public static final String NAME = "name";
    @Mock
    private ValidationService validationService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConstraintValidatorContext context;
    @Mock
    private ValidationRule rule, rule1, rule2;
    @Mock
    private ValidationRuleSet ruleSet;

    @Before
    public void setUp() {
        when(rule.getRuleSet()).thenReturn(ruleSet);
        when(rule.getName()).thenReturn(NAME);
        when(rule1.getName()).thenReturn("anotherName");
        when(rule2.getName()).thenReturn("yetAnotherName");
        doReturn(Arrays.asList(rule1, rule, rule2)).when(ruleSet).getRules();
        when(rule.getId()).thenReturn(0L);
        when(rule1.getId()).thenReturn(1L);
        when(rule2.getId()).thenReturn(2L);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testIsValid() throws Exception {
        assertThat(new UniqueValidationRuleNameValidator(validationService).isValid(rule, context)).isTrue();
    }

    @Test
    public void testInvalid() throws Exception {
        when(rule2.getName()).thenReturn(NAME);

        assertThat(new UniqueValidationRuleNameValidator(validationService).isValid(rule, context)).isFalse();
    }


}