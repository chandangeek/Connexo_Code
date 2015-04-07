package com.elster.jupiter.validation.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.validation.*;
import com.google.common.collect.ImmutableList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.inject.Provider;
import javax.validation.ValidatorFactory;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidationRuleSetVersionTest extends EqualsContractTest {

    private static final long ID = 651L;
    private static final long OTHER_ID = 426294L;
    private static final String NAME = "name";
    private static final String VERSION_NAME = "versionName";
    private ValidationRuleSetImpl validationRuleSet;
    private ValidationRuleSetVersionImpl validationRuleSetVersion;

    @Mock
    private DataMapper<IValidationRule> ruleFactory;
    @Mock
    private DataMapper<IValidationRuleSet> setFactory;
    @Mock
    private DataMapper<IValidationRuleSetVersion> ruleSetVersionFactory;
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
    private Provider<ValidationRuleImpl> ruleProvider = () -> new ValidationRuleImpl(dataModel, validatorCreator, thesaurus, meteringService, eventService, readingTypeInRuleProvider);
    private Provider<ValidationRuleSetVersionImpl> versionProvider = () -> new ValidationRuleSetVersionImpl(dataModel, eventService, ruleProvider);

    @Before
    public void setUp() {
        when(dataModel.mapper(IValidationRule.class)).thenReturn(ruleFactory);
        when(dataModel.mapper(IValidationRuleSet.class)).thenReturn(setFactory);
        when(dataModel.mapper(IValidationRuleSetVersion.class)).thenReturn(ruleSetVersionFactory);
        when(dataModel.mapper(ValidationRuleProperties.class)).thenReturn(rulePropertiesSet);
        when(dataModel.mapper(ReadingTypeInValidationRule.class)).thenReturn(readingTypeInValidationFactory);
        when(dataModel.query(IValidationRule.class, IValidationRuleSet.class, ValidationRuleProperties.class)).thenReturn(queryExecutor);
        when(dataModel.getValidatorFactory()).thenReturn(validatorFactory);
        when(dataModel.getValidatorFactory().getValidator()).thenReturn(validator);
        validationRuleSet = new ValidationRuleSetImpl(dataModel, eventService, versionProvider).init(NAME, null);
        validationRuleSetVersion = new ValidationRuleSetVersionImpl(dataModel, eventService, ruleProvider).init(validationRuleSet, VERSION_NAME);
    }
    @After
    public void tearDown() {
    }

    @Override
    protected Object getInstanceA() {
        if (validationRuleSetVersion == null) {
            validationRuleSetVersion = new ValidationRuleSetVersionImpl(dataModel, eventService, ruleProvider).init(validationRuleSet, VERSION_NAME);
            setId(validationRuleSetVersion, ID);
        }
        return validationRuleSetVersion;
    }

    private void setId(Object entity, long id) {
        field("id").ofType(Long.TYPE).in(entity).set(id);
    }

    @Override
    protected Object getInstanceEqualToA() {
        ValidationRuleSetVersionImpl set = new ValidationRuleSetVersionImpl(dataModel, eventService, ruleProvider).init(validationRuleSet, VERSION_NAME);
        setId(set, ID);
        return set;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        ValidationRuleSetVersionImpl set = new ValidationRuleSetVersionImpl(dataModel, eventService, ruleProvider).init(validationRuleSet, VERSION_NAME);
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
        assertThat(validationRuleSetVersion.getName()).isEqualTo(VERSION_NAME);
    }

    @Test
    public void testPersist() {
        validationRuleSetVersion.save();

        verify(dataModel).persist(validationRuleSetVersion);
    }

    @Test
    public void testUpdate() {
        setId(validationRuleSetVersion, ID);

        validationRuleSetVersion.save();

        verify(dataModel).update(validationRuleSetVersion);

    }

    @Test
    public void testPersistWithRules() {
        validationRuleSetVersion.addRule(ValidationAction.FAIL, "A", "rulename");

        validationRuleSetVersion.save();

        verify(dataModel).persist(validationRuleSetVersion);
    }

    @Test
    public void testPersistWithRulesWarnOnly() {
        validationRuleSetVersion.addRule(ValidationAction.FAIL, "A", "rulename");
        validationRuleSetVersion.addRule(ValidationAction.WARN_ONLY, "B", "rulename2");

        validationRuleSetVersion.save();

        verify(dataModel).persist(validationRuleSetVersion);
    }

    @Test
    public void testDeleteWithRules() {
        ValidationRule rule1 = validationRuleSetVersion.addRule(ValidationAction.FAIL, "A", "rulename");
        validationRuleSetVersion.save();
        setId(validationRuleSetVersion, ID);
        setId(rule1, 1001L);

        validationRuleSetVersion.delete();

        verify(ruleSetVersionFactory).update(validationRuleSetVersion);
        verify(dataModel).update(rule1);
        assertThat(validationRuleSetVersion.getObsoleteDate()).isNotNull();
        assertThat(rule1.getObsoleteDate()).isNotNull();
    }

    @Test
    public void testUpdateWithRulesPerformsNecessaryDBOperations() {
        IValidationRule rule1 = validationRuleSetVersion.addRule(ValidationAction.FAIL, "A", "rulename");
        IValidationRule rule2 = validationRuleSetVersion.addRule(ValidationAction.FAIL, "B", "rulename");
        validationRuleSetVersion.save();
        setId(validationRuleSetVersion, ID);
        setId(rule1, 1001L);
        setId(rule2, 1002L);
        when(ruleFactory.find()).thenReturn(Arrays.asList(rule1, rule2));

        validationRuleSetVersion.deleteRule(rule1);
        IValidationRule rule3 = validationRuleSetVersion.addRule(ValidationAction.FAIL, "C", "rulename");

        validationRuleSetVersion.save();

        verify(dataModel).update(validationRuleSetVersion);
        assertThat(validationRuleSetVersion.getRules()).hasSize(2).contains(rule2, rule3);
    }

    @Test
    public void testUpdateRuleAction() {
        IValidationRule rule1 = validationRuleSetVersion.addRule(ValidationAction.FAIL, "A", "rulename");
        validationRuleSetVersion.save();
        setId(validationRuleSetVersion, ID);
        setId(rule1, 1001L);
        when(ruleFactory.find()).thenReturn(Arrays.asList(rule1));
        validationRuleSetVersion.save();
        assertThat(rule1.getAction()).isEqualTo(ValidationAction.FAIL);

        rule1 = validationRuleSetVersion.updateRule(1001L, "rulename2", true, ValidationAction.WARN_ONLY, Collections.emptyList(), rule1.getProps());
        validationRuleSetVersion.save();

        assertThat(validationRuleSetVersion.getRules()).hasSize(1).contains(rule1);
        assertThat(rule1.getName()).isEqualTo("rulename2");
        assertThat(rule1.getAction()).isEqualTo(ValidationAction.WARN_ONLY);

    }

}
