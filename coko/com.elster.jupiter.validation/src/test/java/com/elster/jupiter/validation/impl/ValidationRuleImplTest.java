package com.elster.jupiter.validation.impl;

import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationRuleSet;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.reflect.core.Reflection.field;

@RunWith(MockitoJUnitRunner.class)
public class ValidationRuleImplTest extends EqualsContractTest {


    public static final int POSITION = 1;
    public static final String IMPLEMENTATION = "test";
    public static final long ID = 2415151L;
    public static final long OTHER_ID = 615585L;

    private ValidationRuleImpl validationRule;

    @Mock
    private ValidationRuleSet ruleSet;

    @Override
    protected Object getInstanceA() {
        if (validationRule == null) {
            validationRule = new ValidationRuleImpl(ruleSet, ValidationAction.FAIL, IMPLEMENTATION, POSITION);
            setId(validationRule, ID);
        }
        return validationRule;
    }

    private ValidationRuleImpl setId(ValidationRuleImpl entity, long id) {
        field("id").ofType(Long.TYPE).in(entity).set(id);
        return entity;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return setId(new ValidationRuleImpl(ruleSet, ValidationAction.FAIL, IMPLEMENTATION, POSITION + 1), ID);
    }

    @Override
    protected Object getInstanceNotEqualToA() {
        return setId(new ValidationRuleImpl(ruleSet, ValidationAction.FAIL, IMPLEMENTATION, POSITION), OTHER_ID);
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
