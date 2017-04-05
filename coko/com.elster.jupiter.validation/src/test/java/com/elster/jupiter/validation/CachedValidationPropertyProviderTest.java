/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.metering.ReadingType;

import com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.Map;

import org.assertj.core.data.MapEntry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CachedValidationPropertyProviderTest extends EqualsContractTest {

    private static final String RULE_NAME = "vr01";
    private static final String VALIDATOR = "com...validator";
    private static final ValidationAction ACTION = ValidationAction.FAIL;
    @Mock
    private ReadingType READINGTYPE;

    private static final String ANOTHER_RULE_NAME = "vr02";
    private static final String ANOTHER_VALIDATOR = "com...another.validator";
    private static final ValidationAction ANOTHER_ACTION = ValidationAction.WARN_ONLY;
    @Mock
    private ReadingType ANOTHER_READINGTYPE;

    private CachedValidationPropertyProvider.Key key;

    @Override
    protected Object getInstanceA() {
        if (key == null) {
            key = new CachedValidationPropertyProvider.Key(READINGTYPE, RULE_NAME, VALIDATOR, ACTION);
        }
        return key;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return new CachedValidationPropertyProvider.Key(READINGTYPE, RULE_NAME, VALIDATOR, ACTION);
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return Arrays.asList(
                new CachedValidationPropertyProvider.Key(ANOTHER_READINGTYPE, RULE_NAME, VALIDATOR, ACTION),
                new CachedValidationPropertyProvider.Key(READINGTYPE, ANOTHER_RULE_NAME, VALIDATOR, ACTION),
                new CachedValidationPropertyProvider.Key(READINGTYPE, RULE_NAME, ANOTHER_VALIDATOR, ACTION),
                new CachedValidationPropertyProvider.Key(READINGTYPE, RULE_NAME, ANOTHER_VALIDATOR, ANOTHER_ACTION)
        );
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }

    @Test
    public void nominalCase() {
        CachedValidationPropertyProvider cachedValidationPropertyProvider = new CachedValidationPropertyProvider();

        // Business method
        cachedValidationPropertyProvider.setProperties(READINGTYPE, RULE_NAME, VALIDATOR, ACTION, ImmutableMap.of("prop1", "value1"));
        cachedValidationPropertyProvider.setProperties(READINGTYPE, RULE_NAME, VALIDATOR, ACTION, ImmutableMap.of("prop2", "value2"));
        cachedValidationPropertyProvider.setProperties(ANOTHER_READINGTYPE, ANOTHER_RULE_NAME, ANOTHER_VALIDATOR, ANOTHER_ACTION, ImmutableMap.of("prop3", "value3"));

        ValidationRule rule = mockValidationRule(RULE_NAME, VALIDATOR, ACTION);
        ValidationRule anotherRule = mockValidationRule(ANOTHER_RULE_NAME, ANOTHER_VALIDATOR, ANOTHER_ACTION);

        // Asserts
        Map<String, Object> properties;
        properties = cachedValidationPropertyProvider.getProperties(rule, READINGTYPE);
        assertThat(properties).isEqualTo(ImmutableMap.of("prop1", "value1", "prop2", "value2"));

        properties = cachedValidationPropertyProvider.getProperties(anotherRule, ANOTHER_READINGTYPE);
        assertThat(properties).containsExactly(MapEntry.entry("prop3", "value3"));

        properties = cachedValidationPropertyProvider.getProperties(rule, ANOTHER_READINGTYPE);
        assertThat(properties).isEmpty();
    }

    private ValidationRule mockValidationRule(String ruleName, String ruleImpl, ValidationAction validationAction) {
        ValidationRule validationRule = mock(ValidationRule.class);
        when(validationRule.getName()).thenReturn(ruleName);
        when(validationRule.getImplementation()).thenReturn(ruleImpl);
        when(validationRule.getAction()).thenReturn(validationAction);
        return validationRule;
    }
}
