package com.elster.jupiter.validation.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import javax.inject.Provider;
import javax.validation.ConstraintViolation;
import javax.validation.ValidatorFactory;

import com.elster.jupiter.validation.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.util.units.Unit;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

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
    public static final Instant START = ZonedDateTime.of(2012, 12, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
    public static final Instant END = ZonedDateTime.of(2012, 12, 5, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
    private static final Range<Instant> INTERVAL = Ranges.closed(START, END);
    private static final Instant DATE1 = ZonedDateTime.of(2012, 12, 3, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();

    private ValidationRuleImpl validationRule;

    @Mock
    private ValidationRuleSet ruleSet;
    @Mock
    private ValidationRuleSetVersion ruleSetVersion;
    @Mock
    private ValidatorFactory validatorFactory;
    @Mock
    private javax.validation.Validator javaxValidator;
    @Mock
    private DataMapper<ValidationRuleProperties> rulePropertiesFactory;
    @Mock
    private DataMapper<ReadingTypeInValidationRule> readingTypesInRuleFactory;
    @Mock
    private ReadingType readingType1;
    @Mock
    private ReadingType readingType2;
    @Mock
    private ReadingType readingType3;
    @Mock
    private Channel channel;
    @Mock
    private Validator validator;
    @Mock
    private IntervalReadingRecord intervalReadingRecord;
    @Mock
    private MeteringService meteringService;
    @Mock
    private ReadingQualityRecord readingQuality, readingQuality1;
    @Mock
    private ReadingRecord readingRecord;
    @Mock
    private DataModel dataModel;
    @Mock
    private ValidatorCreator validatorCreator;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private EventService eventService;
    @Mock
    private PropertySpec properySpec;
    @Mock
    private ValueFactory valueFactory;
    private Provider<ReadingTypeInValidationRuleImpl> provider = () -> new ReadingTypeInValidationRuleImpl(meteringService);

    @Before
    public void setUp() {
        when(channel.findReadingQuality(new ReadingQualityType("3.6." + ID), DATE1)).thenReturn(Optional.<ReadingQualityRecord>empty(), Optional.of(readingQuality));        
        when(dataModel.getValidatorFactory()).thenReturn(validatorFactory);
        when(validatorFactory.getValidator()).thenReturn(javaxValidator);
        when(javaxValidator.validate(any(javax.validation.Validator.class), any(), any())).thenReturn(new HashSet<ConstraintViolation<javax.validation.Validator>>());
        when(dataModel.mapper(ReadingTypeInValidationRule.class)).thenReturn(readingTypesInRuleFactory);
        when(dataModel.mapper(ValidationRuleProperties.class)).thenReturn(rulePropertiesFactory);
        when(validatorCreator.getValidator(eq(IMPLEMENTATION), any(Map.class))).thenReturn(validator);
        when(validatorCreator.getTemplateValidator(eq(IMPLEMENTATION))).thenReturn(validator);
        when(validator.getPropertySpecs()).thenReturn(Arrays.asList(properySpec));
        when(properySpec.getName()).thenReturn(PROPERTY_NAME);
        when(properySpec.getValueFactory()).thenReturn(valueFactory);
        doReturn(Optional.empty()).when(validator).getReadingQualityTypeCode();
        when(channel.getIntervalReadings(readingType2, INTERVAL)).thenReturn(Arrays.asList(intervalReadingRecord));
        when(channel.getRegisterReadings(readingType2, INTERVAL)).thenReturn(Arrays.asList(readingRecord));        
    }

    private ValidationRuleImpl newRule() {
    	return new ValidationRuleImpl(dataModel, validatorCreator, thesaurus, meteringService, eventService, provider);
    }
    
    @Override
    protected Object getInstanceA() {
        if (validationRule == null) {
        	validationRule = setId(newRule().init(ruleSet, ruleSetVersion, ValidationAction.FAIL, IMPLEMENTATION, "rulename"),ID);
        }
        return validationRule;
    }

    private ValidationRuleImpl setId(ValidationRuleImpl entity, long id) {
        field("id").ofType(Long.TYPE).in(entity).set(id);
        return entity;
    }

    @Override
    protected Object getInstanceEqualToA() {        
        return setId(newRule().init(ruleSet, ruleSetVersion, ValidationAction.FAIL, IMPLEMENTATION, "rulename"),ID);
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return ImmutableList.of(setId(newRule().init(ruleSet, ruleSetVersion, ValidationAction.FAIL, IMPLEMENTATION, "rulename"),OTHER_ID));
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
        ValidationRuleImpl testPersistValidationRule = newRule().init(ruleSet, ruleSetVersion, ValidationAction.FAIL, IMPLEMENTATION, "rulename");
        testPersistValidationRule.save();
        verify(dataModel).persist(testPersistValidationRule);
    }

    @Test
    public void testUpdate() {
        ValidationRuleImpl testUpdateValidationRule = newRule().init(ruleSet, ruleSetVersion, ValidationAction.FAIL, IMPLEMENTATION, "rulename");
        field("id").ofType(Long.TYPE).in(testUpdateValidationRule).set(ID);
        testUpdateValidationRule.save();
        verify(dataModel).update(testUpdateValidationRule);
    }

    @Test
    public void testPersistWithProperties() {
        ValidationRuleImpl testPersistValidationRule = newRule().init(ruleSet, ruleSetVersion, ValidationAction.FAIL, IMPLEMENTATION, "rulename");
        testPersistValidationRule.addProperty(PROPERTY_NAME, PROPERTY_VALUE);

        testPersistValidationRule.save();

        verify(dataModel).persist(testPersistValidationRule);
    }

    @Test
    public void testDeleteWithProperties() {
        ValidationRuleImpl rule = newRule().init(ruleSet, ruleSetVersion, ValidationAction.FAIL, IMPLEMENTATION, "rulename");
        rule.addProperty(PROPERTY_NAME, PROPERTY_VALUE);
        field("id").ofType(Long.TYPE).in(rule).set(ID);
        rule.save();
        rule.delete();
        assertThat(rule.getObsoleteDate()).isNotNull();
    }

    @Test
    public void testUpdateWithRulesPerformsNecessaryDBOperations() {
        PropertySpec propertySpec2 = mock(PropertySpec.class);
        when(propertySpec2.getName()).thenReturn(PROPERTY_NAME_2);
        when(propertySpec2.getValueFactory()).thenReturn(valueFactory);
        PropertySpec propertySpec3 = mock(PropertySpec.class);
        when(propertySpec3.getName()).thenReturn(PROPERTY_NAME_3);
        when(propertySpec3.getValueFactory()).thenReturn(valueFactory);
        when(validator.getPropertySpecs()).thenReturn(Arrays.asList(properySpec, propertySpec2, propertySpec3));
        ValidationRuleProperties property1 = validationRule.addProperty(PROPERTY_NAME, PROPERTY_VALUE);
        ValidationRuleProperties property2 = validationRule.addProperty(PROPERTY_NAME_2, PROPERTY_VALUE);
        when(rulePropertiesFactory.find()).thenReturn(Arrays.asList(property1, property2));

        validationRule.deleteProperty(property1);
        validationRule.addProperty(PROPERTY_NAME_3, PROPERTY_VALUE);

        validationRule.save();

        verify(dataModel).update(validationRule);
    }

    @Test
    public void testPersistWithReadingTypes() {
        ValidationRuleImpl newRule = newRule().init(ruleSet, ruleSetVersion, ValidationAction.FAIL, IMPLEMENTATION, "rulename");
        newRule.addReadingType(readingType1);

        newRule.save();

        verify(dataModel).persist(newRule);
    }

    @Test
    public void testDeleteWithReadingTypes() {
        ValidationRuleImpl newRule = newRule().init(ruleSet, ruleSetVersion, ValidationAction.FAIL, IMPLEMENTATION, "rulename");
        newRule.addReadingType(readingType1);
        field("id").ofType(Long.TYPE).in(newRule).set(ID);
        newRule.save();
        newRule.delete();
        assertThat(newRule.getObsoleteDate()).isNotNull();
    }

    @Test
    public void testUpdateWithRulesAndReadingTypesPerformsNecessaryDBOperations() {
        when(readingType1.getMRID()).thenReturn("1");
        when(readingType2.getMRID()).thenReturn("2");
        when(readingType3.getMRID()).thenReturn("3");

        ReadingTypeInValidationRule type1 = validationRule.addReadingType(readingType1);
        ReadingTypeInValidationRule type2 = validationRule.addReadingType(readingType2);

        when(readingTypesInRuleFactory.find("ruleId", validationRule.getId())).thenReturn(Arrays.asList(type1, type2));

        validationRule.deleteReadingType(readingType1);
        validationRule.addReadingType(readingType3);

        validationRule.save();

        verify(dataModel).update(validationRule);

    }




}
