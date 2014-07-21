package com.elster.jupiter.validation.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ProcesStatus;
import com.elster.jupiter.metering.ReadingQuality;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
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
    private ReadingQuality readingQuality;
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

    @Before
    public void setUp() {
        when(channel.findReadingQuality(new ReadingQualityType("3.6." + ID), DATE1)).thenReturn(Optional.<ReadingQuality>absent(), Optional.of(readingQuality));
        when(dataModel.getInstance(ValidationRuleImpl.class)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new ValidationRuleImpl(dataModel, validatorCreator, thesaurus, meteringService, eventService);
            }
        });
        when(dataModel.getInstance(ValidationRulePropertiesImpl.class)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new ValidationRulePropertiesImpl();
            }
        });
        when(dataModel.getInstance(ReadingTypeInValidationRuleImpl.class)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new ReadingTypeInValidationRuleImpl(meteringService);
            }
        });
        when(dataModel.mapper(IValidationRule.class)).thenReturn(ruleFactory);
        when(dataModel.mapper(ReadingTypeInValidationRule.class)).thenReturn(readingTypesInRuleFactory);
        when(dataModel.mapper(ValidationRuleProperties.class)).thenReturn(rulePropertiesFactory);
        when(validatorCreator.getValidator(eq(IMPLEMENTATION), any(Map.class))).thenReturn(validator);
        when(validatorCreator.getTemplateValidator(eq(IMPLEMENTATION))).thenReturn(validator);
        when(validator.getPropertySpecs()).thenReturn(Arrays.asList(properySpec));
        when(properySpec.getName()).thenReturn(PROPERTY_NAME);
        when(properySpec.getValueFactory()).thenReturn(valueFactory);
        when(validator.getReadingQualityTypeCode()).thenReturn(Optional.<ReadingQualityType>absent());
        when(channel.getIntervalReadings(readingType2, INTERVAL.withStart(new Date(INTERVAL.dbStart() - 1)))).thenReturn(Arrays.asList(intervalReadingRecord));
        when(channel.getRegisterReadings(readingType2, INTERVAL)).thenReturn(Arrays.asList(readingRecord));
    }

    @Override
    protected Object getInstanceA() {
        if (validationRule == null) {
            when(dataModel.getInstance(ValidationRuleImpl.class)).thenAnswer(new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                    return new ValidationRuleImpl(dataModel, validatorCreator, thesaurus, meteringService, eventService);
                }
            });
            validationRule = ValidationRuleImpl.from(dataModel, ruleSet, ValidationAction.FAIL, IMPLEMENTATION, POSITION, "rulename");
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
                return new ValidationRuleImpl(dataModel, validatorCreator, thesaurus, meteringService, eventService);
            }
        });
        return setId(ValidationRuleImpl.from(dataModel, ruleSet, ValidationAction.FAIL, IMPLEMENTATION, POSITION + 1, "rulename"), ID);
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        final DataModel dataModel = mock(DataModel.class);
        when(dataModel.getInstance(ValidationRuleImpl.class)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new ValidationRuleImpl(dataModel, validatorCreator, thesaurus, meteringService, eventService);
            }
        });
        return ImmutableList.of(setId(ValidationRuleImpl.from(dataModel, ruleSet, ValidationAction.FAIL, IMPLEMENTATION, POSITION, "rulename"), OTHER_ID));
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
        ValidationRuleImpl testPersistValidationRule = ValidationRuleImpl.from(dataModel, ruleSet, ValidationAction.FAIL, IMPLEMENTATION, POSITION, "rulename");
        testPersistValidationRule.save();
        verify(ruleFactory).persist(testPersistValidationRule);
    }

    @Test
    public void testUpdate() {
        ValidationRuleImpl testUpdateValidationRule = ValidationRuleImpl.from(dataModel, ruleSet, ValidationAction.FAIL, IMPLEMENTATION, POSITION, "rulename");
        field("id").ofType(Long.TYPE).in(testUpdateValidationRule).set(ID);
        testUpdateValidationRule.save();
        verify(ruleFactory).update(testUpdateValidationRule);
    }

    @Test
    public void testPersistWithProperties() {
        ValidationRuleImpl testPersistValidationRule =
                ValidationRuleImpl.from(dataModel, ruleSet, ValidationAction.FAIL, IMPLEMENTATION, POSITION, "rulename");
        ValidationRuleProperties property1 = testPersistValidationRule.addProperty(PROPERTY_NAME, PROPERTY_VALUE);

        testPersistValidationRule.save();

        verify(ruleFactory).persist(testPersistValidationRule);
    }

    @Test
    public void testDeleteWithProperties() {
        ValidationRuleImpl rule = ValidationRuleImpl.from(dataModel, ruleSet, ValidationAction.FAIL, IMPLEMENTATION, POSITION, "rulename");
        ValidationRuleProperties property1 = rule.addProperty(PROPERTY_NAME, PROPERTY_VALUE);
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
        ValidationRuleProperties property3 = validationRule.addProperty(PROPERTY_NAME_3, PROPERTY_VALUE);

        validationRule.save();

        verify(ruleFactory).update(validationRule);
    }

    @Test
    public void testPersistWithReadingTypes() {
        ValidationRuleImpl newRule = ValidationRuleImpl.from(dataModel, ruleSet, ValidationAction.FAIL, IMPLEMENTATION, POSITION, "rulename");
        ReadingTypeInValidationRule readingTypeInValidationRule = newRule.addReadingType(readingType1);

        newRule.save();

        verify(ruleFactory).persist(newRule);
    }

    @Test
    public void testDeleteWithReadingTypes() {
        ValidationRuleImpl newRule = ValidationRuleImpl.from(dataModel, ruleSet, ValidationAction.FAIL, IMPLEMENTATION, POSITION, "rulename");
        ReadingTypeInValidationRule readingTypeInValidationRule = newRule.addReadingType(readingType1);
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
//        when(validationRule.getReadingTypeInRule(readingType1)).thenReturn(type1);
//        when(validationRule.getReadingTypeInRule(readingType2)).thenReturn(type2);

        ReadingTypeInValidationRule type1 = validationRule.addReadingType(readingType1);
        ReadingTypeInValidationRule type2 = validationRule.addReadingType(readingType2);

        when(readingTypesInRuleFactory.find("ruleId", validationRule.getId())).thenReturn(Arrays.asList(type1, type2));

        validationRule.deleteReadingType(readingType1);
        ReadingTypeInValidationRule type3 = validationRule.addReadingType(readingType3);

        validationRule.save();

        verify(ruleFactory).update(validationRule);

    }

    @Test
    public void testValidateChannelWhenNoReadingTypesMatch() {
        validationRule.addReadingType(readingType1);
        validationRule.addReadingType(readingType2);

        doReturn(Arrays.asList(readingType3)).when(channel).getReadingTypes();

        assertThat(validationRule.validateChannel(channel, INTERVAL)).isNull();
    }

    @Test
    public void testValidateChannelWhenOneReadingTypeMatches() {
        field("id").ofType(Long.TYPE).in(validationRule).set(ID);

        when(intervalReadingRecord.getTimeStamp()).thenReturn(DATE1);
        when(readingRecord.getTimeStamp()).thenReturn(DATE1);
        when(validator.validate(intervalReadingRecord)).thenReturn(ValidationResult.SUSPECT);

        when(channel.createReadingQuality(new ReadingQualityType("3.6." + ID), intervalReadingRecord)).thenReturn(readingQuality);
        when(channel.isRegular()).thenReturn(true);
        validationRule.addReadingType(readingType1);
        validationRule.addReadingType(readingType2);
        validationRule.activate();

        doReturn(Arrays.asList(readingType2, readingType3)).when(channel).getReadingTypes();

        assertThat(validationRule.validateChannel(channel, INTERVAL)).isEqualTo(DATE1);

        verify(validator).init(channel, readingType2, INTERVAL);
        verify(channel).createReadingQuality(new ReadingQualityType("3.6." + ID), intervalReadingRecord);
        verify(readingQuality).save();
        verify(intervalReadingRecord).setProcessingFlags(ProcesStatus.Flag.SUSPECT);
        verify(validator).finish();
    }

    @Test
    public void testValidateChannelWhenOneReadingTypeMatchesForRegisterValues() {
        field("id").ofType(Long.TYPE).in(validationRule).set(ID);

        when(channel.getIntervalReadings(readingType2, INTERVAL)).thenReturn(Collections.<IntervalReadingRecord>emptyList());
        when(channel.getRegisterReadings(readingType2, INTERVAL)).thenReturn(Arrays.asList(readingRecord));
        when(channel.isRegular()).thenReturn(false);
        when(intervalReadingRecord.getTimeStamp()).thenReturn(DATE1);
        when(readingRecord.getTimeStamp()).thenReturn(DATE1);
        when(validator.validate(readingRecord)).thenReturn(ValidationResult.SUSPECT);
        when(channel.createReadingQuality(new ReadingQualityType("3.6." + ID), readingRecord)).thenReturn(readingQuality);
        validationRule.addReadingType(readingType1);
        validationRule.addReadingType(readingType2);
        validationRule.activate();

        doReturn(Arrays.asList(readingType2, readingType3)).when(channel).getReadingTypes();

        assertThat(validationRule.validateChannel(channel, INTERVAL)).isEqualTo(DATE1);

        verify(validator).init(channel, readingType2, INTERVAL);
        verify(channel).createReadingQuality(new ReadingQualityType("3.6." + ID), readingRecord);
        verify(readingQuality).save();
        verify(readingRecord).setProcessingFlags(ProcesStatus.Flag.SUSPECT);
        verify(validator).finish();
    }

    @Test
    public void testValidateWhenNotActive() {
        field("id").ofType(Long.TYPE).in(validationRule).set(ID);

        when(intervalReadingRecord.getTimeStamp()).thenReturn(DATE1);
        when(validator.validate(intervalReadingRecord)).thenReturn(ValidationResult.SUSPECT);
        when(channel.createReadingQuality(new ReadingQualityType("3.6." + ID), intervalReadingRecord)).thenReturn(readingQuality);
        when(channel.isRegular()).thenReturn(true);
        validationRule.addReadingType(readingType1);
        validationRule.addReadingType(readingType2);

        doReturn(Arrays.asList(readingType2, readingType3)).when(channel).getReadingTypes();

        assertThat(validationRule.validateChannel(channel, INTERVAL)).isNull();

        verify(validator, never()).init(channel, readingType2, INTERVAL);
        verify(channel, never()).createReadingQuality(new ReadingQualityType("3.6." + ID), intervalReadingRecord);
        verify(intervalReadingRecord, never()).setProcessingFlags(any(ProcesStatus.Flag.class));
    }

    @Test
    public void testValidateChannelWhenAllReadingTypesMatch() {
        field("id").ofType(Long.TYPE).in(validationRule).set(ID);

        when(channel.getIntervalReadings(readingType1, INTERVAL)).thenReturn(Collections.<IntervalReadingRecord>emptyList());
        when(channel.getRegisterReadings(readingType1, INTERVAL)).thenReturn(Arrays.asList(readingRecord));
        when(channel.getIntervalReadings(readingType2, INTERVAL)).thenReturn(Collections.<IntervalReadingRecord>emptyList());
        when(channel.getRegisterReadings(readingType2, INTERVAL)).thenReturn(Arrays.asList(readingRecord));
        when(channel.isRegular()).thenReturn(false);
        when(intervalReadingRecord.getTimeStamp()).thenReturn(DATE1);
        when(readingRecord.getTimeStamp()).thenReturn(DATE1);
        when(validator.validate(readingRecord)).thenReturn(ValidationResult.SUSPECT);
        when(channel.createReadingQuality(new ReadingQualityType("3.6." + ID), readingRecord)).thenReturn(readingQuality);
        validationRule.addReadingType(readingType1);
        validationRule.addReadingType(readingType2);
        validationRule.activate();

        doReturn(Arrays.asList(readingType1, readingType2)).when(channel).getReadingTypes();
        //
        assertThat(validationRule.validateChannel(channel, INTERVAL)).isEqualTo(DATE1);
        verify(validator).init(channel, readingType1, INTERVAL);
        verify(validator).init(channel, readingType2, INTERVAL);

        // @Todo check with Karel if this is ok.
        verify(channel, times(2)).createReadingQuality(new ReadingQualityType("3.6." + ID), readingRecord);
        verify(readingQuality, times(2)).save();
        verify(readingRecord, times(2)).setProcessingFlags(ProcesStatus.Flag.SUSPECT);

        verify(validator, times(2)).finish();
    }

}
