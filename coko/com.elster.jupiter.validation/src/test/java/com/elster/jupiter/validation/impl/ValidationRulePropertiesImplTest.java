/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.validation.ValidationRule;
import com.google.common.collect.ImmutableList;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidationRulePropertiesImplTest extends EqualsContractTest {

    public static final String MIN = "min";
    public static final BigDecimal VALUE = new BigDecimal(100);
    public static final String MAX = "max";
    public static final BigDecimal OTHER_VALUE = new BigDecimal(1000);

    private ValidationRulePropertiesImpl property;
    @Mock
    private ValidationRule rule;
    @Mock
    private PropertySpec propertySpec;
    private BigDecimalFactory valueFactory = new BigDecimalFactory();

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {

    }

    @Override
    protected Object getInstanceA() {
        when(propertySpec.getName()).thenReturn(MIN);
        when(propertySpec.getValueFactory()).thenReturn(valueFactory);
        if (property == null) {
            property = new ValidationRulePropertiesImpl();
            property.init(rule, propertySpec, VALUE);
        }
        return property;
    }

    @Override
    protected Object getInstanceEqualToA() {
        ValidationRulePropertiesImpl other = new ValidationRulePropertiesImpl();
        other.init(rule, propertySpec, OTHER_VALUE);
        return other;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        ValidationRulePropertiesImpl other = new ValidationRulePropertiesImpl();
        PropertySpec maxProp = mock(PropertySpec.class);
        when(maxProp.getName()).thenReturn(MAX);
        when(maxProp.getValueFactory()).thenReturn(valueFactory);
        other.init(rule, maxProp, OTHER_VALUE);
        return ImmutableList.of(other);
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }
}
