package com.elster.jupiter.validation.impl;

import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSetVersion;

import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UniqueValidationRuleNameValidatorTest {
    private static final String NAME = "name";
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConstraintValidatorContext context;
    @Mock
    private ValidationRule rule, rule1, rule2;
    @Mock
    private ValidationRuleSetVersion ruleSetVersion;

    @Before
    public void setUp() {
        when(rule.getRuleSetVersion()).thenReturn(ruleSetVersion);
        when(rule.getName()).thenReturn(NAME);
        when(rule1.getName()).thenReturn("anotherName");
        when(rule2.getName()).thenReturn("yetAnotherName");
        doReturn(Arrays.asList(rule1, rule, rule2)).when(ruleSetVersion).getRules();
        when(rule.getId()).thenReturn(0L);
        when(rule1.getId()).thenReturn(1L);
        when(rule2.getId()).thenReturn(2L);
    }

    @Test
    public void testIsValid() throws Exception {
        assertThat(new UniqueValidationRuleNameValidator().isValid(rule, context)).isTrue();
    }

    @Test
    public void testInvalid() throws Exception {
        when(rule2.getName()).thenReturn(NAME);

        assertThat(new UniqueValidationRuleNameValidator().isValid(rule, context)).isFalse();
    }
}
