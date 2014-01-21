package com.elster.jupiter.validation.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQuality;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.util.units.Unit;
import com.elster.jupiter.validation.ReadingTypeInValidationRule;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationRuleProperties;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.Validator;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.joda.time.DateMidnight;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Mockito.*;

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
    private static final Interval INTERVAL = new Interval(new DateMidnight(2012, 12, 1).toDate(), new DateMidnight(2012, 12, 5).toDate());
    private static final Date DATE1 = new DateMidnight(2012, 12, 3).toDate();
    private static final Date DATE2 = new DateMidnight(2012, 12, 4).toDate();

    private ValidationRuleImpl validationRule;

    @Mock
    private ValidationRuleSet ruleSet;
    @Mock
    private DataMapper<IValidationRule> ruleFactory;
    @Mock
    private DataMapper<ValidationRuleProperties>  rulePropertiesFactory;
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
    private ReadingQuality readingQuality;
    @Mock
    private ReadingRecord readingRecord;
    @Mock
    private DataModel dataModel;
    @Mock
    private ValidatorCreator validatorCreator;
    @Mock
    private Thesaurus thesaurus;

    @Before
    public void setUp() {
        when(channel.findReadingQuality(new ReadingQualityType("3.6." + ID), DATE1)).thenReturn(Optional.<ReadingQuality>absent(), Optional.of(readingQuality));
        when(dataModel.getInstance(ValidationRuleImpl.class)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new ValidationRuleImpl(dataModel, validatorCreator, thesaurus);
            }
        });
        when(dataModel.getInstance(ValidationRulePropertiesImpl.class)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new ValidationRulePropertiesImpl(dataModel);
            }
        });
        when(dataModel.getInstance(ReadingTypeInValidationRuleImpl.class)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new ReadingTypeInValidationRuleImpl(dataModel, meteringService);
            }
        });
        when(dataModel.mapper(IValidationRule.class)).thenReturn(ruleFactory);
        when(dataModel.mapper(ReadingTypeInValidationRule.class)).thenReturn(readingTypesInRuleFactory);
        when(dataModel.mapper(ValidationRuleProperties.class)).thenReturn(rulePropertiesFactory);
        when(validatorCreator.getValidator(eq(IMPLEMENTATION), any(Map.class))).thenReturn(validator);
        when(validator.getReadingQualityTypeCode()).thenReturn(Optional.<ReadingQualityType>absent());
        when(channel.getIntervalReadings(readingType2, INTERVAL)).thenReturn(Arrays.asList(intervalReadingRecord));
        when(channel.getRegisterReadings(readingType2, INTERVAL)).thenReturn(Arrays.asList(readingRecord));
    }

    @Override
    protected Object getInstanceA() {
        if (validationRule == null) {
            when(dataModel.getInstance(ValidationRuleImpl.class)).thenAnswer(new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                    return new ValidationRuleImpl(dataModel, validatorCreator, thesaurus);
                }
            });
            validationRule = ValidationRuleImpl.from(dataModel, ruleSet, ValidationAction.FAIL, IMPLEMENTATION, POSITION);
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
        final DataModel dataModel = mock(DataModel.class);
        when(dataModel.getInstance(ValidationRuleImpl.class)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new ValidationRuleImpl(dataModel, validatorCreator, thesaurus);
            }
        });
        return setId(ValidationRuleImpl.from(dataModel, ruleSet, ValidationAction.FAIL, IMPLEMENTATION, POSITION + 1), ID);
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        final DataModel dataModel = mock(DataModel.class);
        when(dataModel.getInstance(ValidationRuleImpl.class)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new ValidationRuleImpl(dataModel, validatorCreator, thesaurus);
            }
        });
        return ImmutableList.of(setId(ValidationRuleImpl.from(dataModel, ruleSet, ValidationAction.FAIL, IMPLEMENTATION, POSITION), OTHER_ID));
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
        ValidationRuleImpl testPersistValidationRule = ValidationRuleImpl.from(dataModel, ruleSet, ValidationAction.FAIL, IMPLEMENTATION, POSITION);
        testPersistValidationRule.save();
        verify(ruleFactory).persist(testPersistValidationRule);
    }

    @Test
    public void testUpdate() {
        ValidationRuleImpl testUpdateValidationRule = ValidationRuleImpl.from(dataModel, ruleSet, ValidationAction.FAIL, IMPLEMENTATION, POSITION);
        field("id").ofType(Long.TYPE).in(testUpdateValidationRule).set(ID);
        testUpdateValidationRule.save();
        verify(ruleFactory).update(testUpdateValidationRule);
    }

    @Test
    public void testPersistWithProperties() {
        ValidationRuleImpl testPersistValidationRule =
                ValidationRuleImpl.from(dataModel, ruleSet, ValidationAction.FAIL, IMPLEMENTATION, POSITION);
        ValidationRuleProperties property1 = testPersistValidationRule.addProperty(PROPERTY_NAME, PROPERTY_VALUE);

        testPersistValidationRule.save();

        verify(ruleFactory).persist(testPersistValidationRule);
        verify(rulePropertiesFactory).persist(property1);
    }

    @Test
    public void testDeleteWithProperties() {
        ValidationRuleImpl rule = ValidationRuleImpl.from(dataModel, ruleSet, ValidationAction.FAIL, IMPLEMENTATION, POSITION);
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
        ValidationRuleImpl newRule = ValidationRuleImpl.from(dataModel, ruleSet, ValidationAction.FAIL, IMPLEMENTATION, POSITION);
        ReadingTypeInValidationRule readingTypeInValidationRule = newRule.addReadingType(readingType1);

        newRule.save();

        verify(ruleFactory).persist(newRule);
        verify(readingTypesInRuleFactory).persist(readingTypeInValidationRule);
    }

    @Test
    public void testDeleteWithReadingTypes() {
        ValidationRuleImpl newRule = ValidationRuleImpl.from(dataModel, ruleSet, ValidationAction.FAIL, IMPLEMENTATION, POSITION);
        ReadingTypeInValidationRule readingTypeInValidationRule = newRule.addReadingType(readingType1);
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

    @Test
    public void testValidateChannelWhenNoReadingTypesMatch() {
        validationRule.addReadingType(readingType1);
        validationRule.addReadingType(readingType2);

        when((Object) channel.getReadingTypes()).thenReturn(Arrays.asList(readingType3));

        assertThat(validationRule.validateChannel(channel, INTERVAL)).isNull();
    }

    @Test
    public void testValidateChannelWhenOneReadingTypeMatches() {
        field("id").ofType(Long.TYPE).in(validationRule).set(ID);

        when(intervalReadingRecord.getTimeStamp()).thenReturn(DATE1);
        when(readingRecord.getTimeStamp()).thenReturn(DATE1);
        when(validator.validate(intervalReadingRecord)).thenReturn(ValidationResult.SUSPECT);
        when((Object) channel.createReadingQuality(new ReadingQualityType("3.6."+ID), intervalReadingRecord)).thenReturn(readingQuality);
        when(channel.isRegular()).thenReturn(true);
        validationRule.addReadingType(readingType1);
        validationRule.addReadingType(readingType2);
        validationRule.activate();

        when((Object) channel.getReadingTypes()).thenReturn(Arrays.asList(readingType2, readingType3));

        assertThat(validationRule.validateChannel(channel, INTERVAL)).isEqualTo(DATE1);

        verify(validator).init(channel, readingType2, INTERVAL);
        verify(channel).createReadingQuality(new ReadingQualityType("3.6."+ID), intervalReadingRecord);
        verify(readingQuality).save();
    }

    @Test
    public void testValidateChannelWhenOneReadingTypeMatchesForRegisterValues() {
        field("id").ofType(Long.TYPE).in(validationRule).set(ID);

        when(channel.getIntervalReadings(readingType2, INTERVAL)).thenReturn(Collections.<IntervalReadingRecord>emptyList());
        when(channel.getRegisterReadings(readingType2, INTERVAL)).thenReturn(Arrays.asList(readingRecord));
        when(intervalReadingRecord.getTimeStamp()).thenReturn(DATE1);
        when(readingRecord.getTimeStamp()).thenReturn(DATE1);
        when(validator.validate(readingRecord)).thenReturn(ValidationResult.SUSPECT);
        when(channel.createReadingQuality(new ReadingQualityType("3.6."+ID), readingRecord)).thenReturn(readingQuality);
        validationRule.addReadingType(readingType1);
        validationRule.addReadingType(readingType2);
        validationRule.activate();

        when((Object) channel.getReadingTypes()).thenReturn(Arrays.asList(readingType2, readingType3));

        assertThat(validationRule.validateChannel(channel, INTERVAL)).isEqualTo(DATE1);

        verify(validator).init(channel, readingType2, INTERVAL);
        verify(channel).createReadingQuality(new ReadingQualityType("3.6."+ID), readingRecord);
        verify(readingQuality).save();
    }

    @Test
    public void testValidateWhenNotActive() {
        field("id").ofType(Long.TYPE).in(validationRule).set(ID);

        when(intervalReadingRecord.getTimeStamp()).thenReturn(DATE1);
        when(validator.validate(intervalReadingRecord)).thenReturn(ValidationResult.SUSPECT);
        when(channel.createReadingQuality(new ReadingQualityType("3.6."+ID), intervalReadingRecord)).thenReturn(readingQuality);
        validationRule.addReadingType(readingType1);
        validationRule.addReadingType(readingType2);

        when((Object) channel.getReadingTypes()).thenReturn(Arrays.asList(readingType2, readingType3));

        assertThat(validationRule.validateChannel(channel, INTERVAL)).isNull();

        verify(validator, never()).init(channel, readingType2, INTERVAL);
        verify(channel, never()).createReadingQuality(new ReadingQualityType("3.6."+ID), intervalReadingRecord);
    }

    @Test
    public void testValidateChannelWhenAllReadingTypesMatch() {
        field("id").ofType(Long.TYPE).in(validationRule).set(ID);

        when(channel.getIntervalReadings(readingType1, INTERVAL)).thenReturn(Collections.<IntervalReadingRecord>emptyList());
        when(channel.getRegisterReadings(readingType1, INTERVAL)).thenReturn(Arrays.asList(readingRecord));
        when(channel.getIntervalReadings(readingType2, INTERVAL)).thenReturn(Collections.<IntervalReadingRecord>emptyList());
        when(channel.getRegisterReadings(readingType2, INTERVAL)).thenReturn(Arrays.asList(readingRecord));
        when(intervalReadingRecord.getTimeStamp()).thenReturn(DATE1);
        when(readingRecord.getTimeStamp()).thenReturn(DATE1);
        when(validator.validate(readingRecord)).thenReturn(ValidationResult.SUSPECT);
        when(channel.createReadingQuality(new ReadingQualityType("3.6." + ID), readingRecord)).thenReturn(readingQuality);
        validationRule.addReadingType(readingType1);
        validationRule.addReadingType(readingType2);
        validationRule.activate();

        when((Object) channel.getReadingTypes()).thenReturn(Arrays.asList(readingType1, readingType2));

        assertThat(validationRule.validateChannel(channel, INTERVAL)).isEqualTo(DATE1);
        verify(validator).init(channel, readingType2, INTERVAL);
        verify(channel).createReadingQuality(new ReadingQualityType("3.6."+ID), readingRecord);
        verify(readingQuality).save();
    }

}
