package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.cache.TypeCache;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.util.units.Unit;
import com.elster.jupiter.validation.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidationRuleImplTest extends EqualsContractTest {


    public static final int POSITION = 1;
    public static final String IMPLEMENTATION = "test";
    public static final long ID = 2415151L;
    public static final long OTHER_ID = 615585L;

    public static final String PROPERTY_NAME = "min";
    public static final String PROPERTY_NAME_2 = "max";
    public static final String PROPERTY_NAME_3 = "other_property";
    public static final Quantity PROPERTY_VALUE = Unit.UNITLESS.amount(new BigDecimal(100));

    private ValidationRuleImpl validationRule;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ServiceLocator serviceLocator;

    @Mock
    private ValidationRuleSet ruleSet;

    @Mock
    private TypeCache<ValidationRule> ruleFactory;

    @Mock
    private TypeCache<ValidationRuleProperties>  rulePropertiesFactory;

    @Mock
    private DataMapper<ReadingTypeInValidationRule> readingTypesInRuleFactory;

    @Mock
    private ReadingType readingType1;

    @Mock
    private ReadingType readingType2;

    @Mock
    private ReadingType readingType3;

    @Before
    public void setUp() {
        when(serviceLocator.getOrmClient().getValidationRuleFactory()).thenReturn(ruleFactory);
        when(serviceLocator.getOrmClient().getValidationRulePropertiesFactory()).thenReturn(rulePropertiesFactory);
        when(serviceLocator.getOrmClient().getReadingTypesInValidationRuleFactory()).thenReturn(readingTypesInRuleFactory);
        Bus.setServiceLocator(serviceLocator);
    }

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

    @Test
    public void testPersist() {
        ValidationRuleImpl testPersistValidationRule =
                new ValidationRuleImpl(ruleSet, ValidationAction.FAIL, IMPLEMENTATION, POSITION);
        testPersistValidationRule.save();
        verify(ruleFactory).persist(testPersistValidationRule);
    }

    @Test
    public void testUpdate() {
        ValidationRuleImpl testUpdateValidationRule =
                new ValidationRuleImpl(ruleSet, ValidationAction.FAIL, IMPLEMENTATION, POSITION);
        field("id").ofType(Long.TYPE).in(testUpdateValidationRule).set(ID);
        testUpdateValidationRule.save();
        verify(ruleFactory).update(testUpdateValidationRule);
    }

    @Test
    public void testPersistWithProperties() {
        ValidationRuleImpl testPersistValidationRule =
                new ValidationRuleImpl(ruleSet, ValidationAction.FAIL, IMPLEMENTATION, POSITION);
        ValidationRuleProperties property1 = testPersistValidationRule.addProperty(PROPERTY_NAME, PROPERTY_VALUE);

        testPersistValidationRule.save();

        verify(ruleFactory).persist(testPersistValidationRule);
        verify(rulePropertiesFactory).persist(property1);
    }

    @Test
    public void testDeleteWithProperties() {
        ValidationRuleImpl rule =
                new ValidationRuleImpl(ruleSet, ValidationAction.FAIL, IMPLEMENTATION, POSITION);
        ValidationRuleProperties property1 = rule.addProperty(PROPERTY_NAME, PROPERTY_VALUE);
        field("id").ofType(Long.TYPE).in(rule).set(ID);

        rule.delete();

        verify(ruleFactory).remove(rule);
    }

    @Test
    public void testUpdateWithRulesPerformsNecessaryDBOperations() {
        ValidationRuleProperties property1 = validationRule.addProperty(PROPERTY_NAME, PROPERTY_VALUE);
        ValidationRuleProperties property2 = validationRule.addProperty(PROPERTY_NAME_2, PROPERTY_VALUE);
        when(rulePropertiesFactory.find()).thenReturn(Arrays.asList(property1, property2));

        validationRule.deleteProperty(property1);
        ValidationRuleProperties property3 = validationRule.addProperty(PROPERTY_NAME_3, PROPERTY_VALUE);

        validationRule.save();

        verify(ruleFactory).update(validationRule);
        verify(rulePropertiesFactory).remove(property1);
        verify(rulePropertiesFactory).remove(property2);
        verify(rulePropertiesFactory).persist(property2);
        verify(rulePropertiesFactory).persist(property3);

    }

    @Test
    public void testPersistWithReadingTypes() {
        ValidationRuleImpl newRule =
                new ValidationRuleImpl(ruleSet, ValidationAction.FAIL, IMPLEMENTATION, POSITION);
        ReadingTypeInValidationRule readingTypeInValidationRule =
                newRule.addReadingType(readingType1);

        newRule.save();

        verify(ruleFactory).persist(newRule);
        verify(readingTypesInRuleFactory).persist(readingTypeInValidationRule);
    }

    @Test
    public void testDeleteWithReadingTypes() {
        ValidationRuleImpl newRule =
                new ValidationRuleImpl(ruleSet, ValidationAction.FAIL, IMPLEMENTATION, POSITION);
        ReadingTypeInValidationRule readingTypeInValidationRule =
                newRule.addReadingType(readingType1);
        field("id").ofType(Long.TYPE).in(newRule).set(ID);

        newRule.delete();

        verify(ruleFactory).remove(newRule);
    }

    @Test
    public void testUpdateWithRulesAndReadingTypesPerformsNecessaryDBOperations() {
        when(readingType1.getMRID()).thenReturn("1");
        when(readingType2.getMRID()).thenReturn("2");
        when(readingType3.getMRID()).thenReturn("3");
//        when(validationRule.getReadingTypeInRule(readingType1)).thenReturn(type1);
//        when(validationRule.getReadingTypeInRule(readingType2)).thenReturn(type2);

        ReadingTypeInValidationRule type1 = validationRule.addReadingType(readingType1);
        ReadingTypeInValidationRule type2 = validationRule.addReadingType(readingType2);

        when(readingTypesInRuleFactory.find("ruleId", validationRule.getId())).thenReturn(Arrays.asList(type1, type2));

        validationRule.deleteReadingType(readingType1);
        ReadingTypeInValidationRule type3 = validationRule.addReadingType(readingType3);

        validationRule.save();

        verify(ruleFactory).update(validationRule);
        verify(readingTypesInRuleFactory).remove(type1);
        verify(readingTypesInRuleFactory).remove(type2);
        verify(readingTypesInRuleFactory).persist(type2);
        verify(readingTypesInRuleFactory).persist(type3);

    }

}
