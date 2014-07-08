package com.elster.jupiter.validation.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.util.units.Unit;
import com.elster.jupiter.validation.ValidationRule;
import com.google.common.collect.ImmutableList;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidationRulePropertiesImplTest extends EqualsContractTest {

    public static final String MIN = "min";
    public static final Quantity VALUE = Unit.UNITLESS.amount(new BigDecimal(100)) ;
    public static final String MAX = "max";
    public static final Quantity OTHER_VALUE = Unit.UNITLESS.amount(new BigDecimal(1000));
    private ValidationRulePropertiesImpl property;

    @Mock
    private ValidationRule rule;
    @Mock
    private DataModel dataModel;
    @Mock
    private Thesaurus thesaurus;

    @Before
    public void setUp() {
        when(dataModel.getInstance(ValidationRulePropertiesImpl.class)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new ValidationRulePropertiesImpl();
            }
        });
    }

    @After
    public void tearDown() {

    }

    @Override
    protected Object getInstanceA() {
        when(dataModel.getInstance(ValidationRulePropertiesImpl.class)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new ValidationRulePropertiesImpl();
            }
        });
        if (property == null) {
            property = ValidationRulePropertiesImpl.from(dataModel, rule, MIN, VALUE);
        }
        return property;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return ValidationRulePropertiesImpl.from(dataModel, rule, MIN, OTHER_VALUE);
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return ImmutableList.of(ValidationRulePropertiesImpl.from(dataModel, rule, MAX, OTHER_VALUE));
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
