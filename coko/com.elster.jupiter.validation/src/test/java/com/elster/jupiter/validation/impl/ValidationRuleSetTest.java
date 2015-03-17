package com.elster.jupiter.validation.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.inject.Provider;
import javax.validation.ValidatorFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.validation.ReadingTypeInValidationRule;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleProperties;
import com.google.common.collect.ImmutableList;

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
    private MeteringService meteringService;
    @Mock
    private ValidatorCreator validatorCreator;
    @Mock
    private DataMapper<ValidationRuleProperties> rulePropertiesSet;
    @Mock
    private DataMapper<ReadingTypeInValidationRule> readingTypeInValidationFactory;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private ValidatorFactory validatorFactory;
    @Mock
    private javax.validation.Validator validator;
    @Mock
    private QueryExecutor<IValidationRule> queryExecutor;
    private Provider<ReadingTypeInValidationRuleImpl> readingTypeInRuleProvider = () -> new ReadingTypeInValidationRuleImpl(meteringService);
    private Provider<ValidationRuleImpl> provider = () -> new ValidationRuleImpl(dataModel, validatorCreator, thesaurus, meteringService, eventService, readingTypeInRuleProvider);

    @Before
    public void setUp() {
        when(dataModel.mapper(IValidationRule.class)).thenReturn(ruleFactory);
        when(dataModel.mapper(IValidationRuleSet.class)).thenReturn(setFactory);
        when(dataModel.mapper(ValidationRuleProperties.class)).thenReturn(rulePropertiesSet);
        when(dataModel.mapper(ReadingTypeInValidationRule.class)).thenReturn(readingTypeInValidationFactory);
        when(dataModel.query(IValidationRule.class, IValidationRuleSet.class, ValidationRuleProperties.class)).thenReturn(queryExecutor);
        when(dataModel.getValidatorFactory()).thenReturn(validatorFactory);
        when(dataModel.getValidatorFactory().getValidator()).thenReturn(validator);
        validationRuleSet = new ValidationRuleSetImpl(dataModel, eventService, provider).init(NAME, null);
    }
    @After
    public void tearDown() {
    }

    @Override
    protected Object getInstanceA() {
        if (validationRuleSet == null) {
            validationRuleSet = new ValidationRuleSetImpl(dataModel, eventService, provider).init(NAME, null);
            setId(validationRuleSet, ID);
        }
        return validationRuleSet;
    }

    private void setId(Object entity, long id) {
        field("id").ofType(Long.TYPE).in(entity).set(id);
    }

    @Override
    protected Object getInstanceEqualToA() {
        ValidationRuleSetImpl set = new ValidationRuleSetImpl(dataModel, eventService, provider).init(NAME, null);
        setId(set, ID);
        return set;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        ValidationRuleSetImpl set = new ValidationRuleSetImpl(dataModel, eventService, provider).init(NAME, null);
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

        verify(dataModel).persist(validationRuleSet);
    }

    @Test
    public void testUpdate() {
        setId(validationRuleSet, ID);

        validationRuleSet.save();

        verify(dataModel).update(validationRuleSet);

    }

    @Test
    public void testPersistWithRules() {
        validationRuleSet.addRule(ValidationAction.FAIL, "A", "rulename");

        validationRuleSet.save();

        verify(dataModel).persist(validationRuleSet);        
    }

    @Test
    public void testPersistWithRulesWarnOnly() {
        validationRuleSet.addRule(ValidationAction.FAIL, "A", "rulename");
        validationRuleSet.addRule(ValidationAction.WARN_ONLY, "B", "rulename2");

        validationRuleSet.save();

        verify(dataModel).persist(validationRuleSet);
    }

    @Test
    public void testDeleteWithRules() {
        ValidationRule rule1 = validationRuleSet.addRule(ValidationAction.FAIL, "A", "rulename");
        validationRuleSet.save();
        setId(validationRuleSet, ID);
        setId(rule1, 1001L);

        validationRuleSet.delete();

        verify(setFactory).update(validationRuleSet);
        verify(dataModel).update(rule1);
        assertThat(validationRuleSet.getObsoleteDate()).isNotNull();
        assertThat(rule1.getObsoleteDate()).isNotNull();
    }

    @Test
    public void testUpdateWithRulesPerformsNecessaryDBOperations() {
        IValidationRule rule1 = validationRuleSet.addRule(ValidationAction.FAIL, "A", "rulename");
        IValidationRule rule2 = validationRuleSet.addRule(ValidationAction.FAIL, "B", "rulename");
        validationRuleSet.save();
        setId(validationRuleSet, ID);
        setId(rule1, 1001L);
        setId(rule2, 1002L);
        when(ruleFactory.find()).thenReturn(Arrays.asList(rule1, rule2));

        validationRuleSet.deleteRule(rule1);
        IValidationRule rule3 = validationRuleSet.addRule(ValidationAction.FAIL, "C", "rulename");

        validationRuleSet.save();

        verify(dataModel).update(validationRuleSet);
        assertThat(validationRuleSet.getRules()).hasSize(2).contains(rule2, rule3);

    }

    @Test
    public void testUpdateRuleAction() {
        IValidationRule rule1 = validationRuleSet.addRule(ValidationAction.FAIL, "A", "rulename");
        validationRuleSet.save();
        setId(validationRuleSet, ID);
        setId(rule1, 1001L);
        when(ruleFactory.find()).thenReturn(Arrays.asList(rule1));
        validationRuleSet.save();
        assertThat(rule1.getAction()).isEqualTo(ValidationAction.FAIL);

        rule1 = validationRuleSet.updateRule(1001L, "rulename2", true, ValidationAction.WARN_ONLY, Collections.emptyList(), rule1.getProps());
        validationRuleSet.save();

        assertThat(validationRuleSet.getRules()).hasSize(1).contains(rule1);
        assertThat(rule1.getName()).isEqualTo("rulename2");
        assertThat(rule1.getAction()).isEqualTo(ValidationAction.WARN_ONLY);

    }

}
