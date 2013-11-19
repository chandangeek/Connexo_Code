package com.elster.jupiter.validation.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.util.units.Unit;
import com.elster.jupiter.validation.ValidationRule;
import com.google.common.collect.ImmutableList;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;

@RunWith(MockitoJUnitRunner.class)
public class ValidationRulePropertiesImplTest extends EqualsContractTest {

    public static final String MIN = "min";
    public static final Quantity VALUE = Unit.UNITLESS.amount(new BigDecimal(100)) ;
    public static final String MAX = "max";
    public static final Quantity OTHER_VALUE = Unit.UNITLESS.amount(new BigDecimal(1000));
    private ValidationRulePropertiesImpl property;

    @Mock
    private ValidationRule rule;

    @Override
    protected Object getInstanceA() {
        if (property == null) {
            property = new ValidationRulePropertiesImpl(rule, MIN, VALUE);
        }
        return property;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return new ValidationRulePropertiesImpl(rule, MIN, OTHER_VALUE);
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return ImmutableList.of(new ValidationRulePropertiesImpl(rule, MAX, OTHER_VALUE));
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
