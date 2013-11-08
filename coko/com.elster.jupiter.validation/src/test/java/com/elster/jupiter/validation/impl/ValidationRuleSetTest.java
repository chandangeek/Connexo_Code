package com.elster.jupiter.validation.impl;

import com.elster.jupiter.orm.cache.TypeCache;
import com.elster.jupiter.validation.ValidationRuleSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidationRuleSetTest extends EqualsContractTest {

    private static final long ID = 651L;
    private static final long OTHER_ID = 426294L;
    private static final String NAME = "name";
    private ValidationRuleSetImpl validationRuleSet;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ServiceLocator serviceLocator;
    @Mock
    private TypeCache<ValidationRuleSet> factory;

    @Override
    protected Object getInstanceA() {
        if (validationRuleSet == null) {
            validationRuleSet = new ValidationRuleSetImpl(NAME);
            setId(validationRuleSet, ID);
        }
        return validationRuleSet;
    }

    private void setId(ValidationRuleSetImpl entity, long id) {
        field("id").ofType(Long.TYPE).in(entity).set(id);
    }

    @Override
    protected Object getInstanceEqualToA() {
        ValidationRuleSetImpl set = new ValidationRuleSetImpl(NAME);
        setId(set, ID);
        return set;
    }

    @Override
    protected Object getInstanceNotEqualToA() {
        ValidationRuleSetImpl set = new ValidationRuleSetImpl(NAME);
        setId(set, OTHER_ID);
        return set;
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }

    @Before
    public void setUp() {
        validationRuleSet = new ValidationRuleSetImpl(NAME);

        Bus.setServiceLocator(serviceLocator);
    }

    @After
    public void tearDown() {
        Bus.clearServiceLocator(serviceLocator);
    }

    @Test
    public void testGetNameAfterCreation() {
        assertThat(validationRuleSet.getName()).isEqualTo(NAME);
    }

    @Test
    public void testPersist() {
        when(serviceLocator.getOrmClient().getValidationRuleSetFactory()).thenReturn(factory);

        validationRuleSet.save();

        verify(factory).persist(validationRuleSet);
    }

    @Test
    public void testUpdate() {
        when(serviceLocator.getOrmClient().getValidationRuleSetFactory()).thenReturn(factory);
        simulateSaved();

        validationRuleSet.save();

        verify(factory).update(validationRuleSet);

    }

    private void simulateSaved() {
        field("id").ofType(Long.TYPE).in(validationRuleSet).set(ID);
    }
}
