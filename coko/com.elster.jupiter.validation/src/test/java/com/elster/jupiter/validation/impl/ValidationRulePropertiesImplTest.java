package com.elster.jupiter.validation.impl;

import com.elster.jupiter.validation.ValidationRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;

@RunWith(MockitoJUnitRunner.class)
public class ValidationRulePropertiesImplTest extends EqualsContractTest {

    public static final String MIN = "min";
    public static final BigDecimal VALUE = new BigDecimal(100);
    public static final String MAX = "max";
    public static final BigDecimal OTHER_VALUE = new BigDecimal(1000);
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
    protected Object getInstanceNotEqualToA() {
        return new ValidationRulePropertiesImpl(rule, MAX, OTHER_VALUE);
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
