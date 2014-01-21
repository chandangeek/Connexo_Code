package com.elster.jupiter.validation.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.validation.ReadingTypeInValidationRule;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleProperties;
import com.google.common.collect.ImmutableList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidationRuleSetTest extends EqualsContractTest {

    private static final long ID = 651L;
    private static final long OTHER_ID = 426294L;
    private static final String NAME = "name";
    private ValidationRuleSetImpl validationRuleSet;

    @Mock
    private DataMapper<IValidationRule> ruleFactory;
    @Mock
    private DataMapper<IValidationRuleSet> setFactory;
    @Mock
    private DataModel dataModel;
    @Mock
    private EventService eventService;
    @Mock
    private ValidatorCreator validatorCreator;
    @Mock
    private DataMapper<ValidationRuleProperties> rulePropertiesSet;
    @Mock
    private DataMapper<ReadingTypeInValidationRule> readingTypeInValidationFactory;
    @Mock
    private Thesaurus thesaurus;

    @Before
    public void setUp() {
        when(dataModel.mapper(IValidationRule.class)).thenReturn(ruleFactory);
        when(dataModel.mapper(IValidationRuleSet.class)).thenReturn(setFactory);
        when(dataModel.mapper(ValidationRuleProperties.class)).thenReturn(rulePropertiesSet);
        when(dataModel.mapper(ReadingTypeInValidationRule.class)).thenReturn(readingTypeInValidationFactory);
        when(dataModel.getInstance(ValidationRuleImpl.class)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new ValidationRuleImpl(dataModel, validatorCreator, thesaurus);
            }
        });

        validationRuleSet = new ValidationRuleSetImpl(dataModel, eventService).init(NAME, null);
    }
    @After
    public void tearDown() {
    }

    @Override
    protected Object getInstanceA() {
        if (validationRuleSet == null) {
            validationRuleSet = new ValidationRuleSetImpl(dataModel, eventService).init(NAME, null);
            setId(validationRuleSet, ID);
        }
        return validationRuleSet;
    }

    private void setId(Object entity, long id) {
        field("id").ofType(Long.TYPE).in(entity).set(id);
    }

    @Override
    protected Object getInstanceEqualToA() {
        ValidationRuleSetImpl set = new ValidationRuleSetImpl(dataModel, eventService).init(NAME, null);
        setId(set, ID);
        return set;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        ValidationRuleSetImpl set = new ValidationRuleSetImpl(dataModel, eventService).init(NAME, null);
        setId(set, OTHER_ID);
        return ImmutableList.of(set);
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
    public void testGetNameAfterCreation() {
        assertThat(validationRuleSet.getName()).isEqualTo(NAME);
    }

    @Test
    public void testPersist() {
        validationRuleSet.save();

        verify(setFactory).persist(validationRuleSet);
    }

    @Test
    public void testUpdate() {
        setId(validationRuleSet, ID);

        validationRuleSet.save();

        verify(setFactory).update(validationRuleSet);

    }

    @Test
    public void testPersistWithRules() {
        IValidationRule rule1 = validationRuleSet.addRule(ValidationAction.FAIL, "A");

        validationRuleSet.save();

        verify(setFactory).persist(validationRuleSet);
        verify(ruleFactory).persist(rule1);
    }

    @Test
    public void testDeleteWithRules() {
        ValidationRule rule1 = validationRuleSet.addRule(ValidationAction.FAIL, "A");
        setId(validationRuleSet, ID);
        setId(rule1, 1001L);

        validationRuleSet.delete();

        verify(setFactory).remove(validationRuleSet);
    }

    @Test
    public void testUpdateWithRulesPerformsNecessaryDBOperations() {
        setId(validationRuleSet, ID);
        IValidationRule rule1 = validationRuleSet.addRule(ValidationAction.FAIL, "A");
        setId(rule1, 1001L);
        IValidationRule rule2 = validationRuleSet.addRule(ValidationAction.FAIL, "B");
        setId(rule2, 1002L);
        when(ruleFactory.find()).thenReturn(Arrays.asList(rule1, rule2));

        validationRuleSet.deleteRule(rule1);
        IValidationRule rule3 = validationRuleSet.addRule(ValidationAction.FAIL, "C");

        validationRuleSet.save();

        verify(setFactory).update(validationRuleSet);
        assertThat(validationRuleSet.getRules()).hasSize(2).contains(rule2, rule3);

    }

    @Test
    public void testUpdateWithRulesProperlyUpdatesPositions() {
        setId(validationRuleSet, ID);
        IValidationRule rule1 = validationRuleSet.addRule(ValidationAction.FAIL, "A");
        setId(rule1, 1001L);
        IValidationRule rule2 = validationRuleSet.addRule(ValidationAction.FAIL, "B");
        setId(rule2, 1002L);
        when(ruleFactory.find()).thenReturn(Arrays.asList(rule1, rule2));

        validationRuleSet.deleteRule(rule1);
        IValidationRule rule3 = validationRuleSet.addRule(ValidationAction.FAIL, "C");

        validationRuleSet.save();

        assertThat(field("position").ofType(Integer.TYPE).in(rule2).get()).isEqualTo(1);
        assertThat(field("position").ofType(Integer.TYPE).in(rule3).get()).isEqualTo(2);

    }


}
