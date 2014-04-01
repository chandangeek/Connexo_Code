package com.elster.jupiter.validation.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Unit;
import com.elster.jupiter.validation.ReadingTypeInValidationRule;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleProperties;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.ValidationStats;
import com.elster.jupiter.validation.Validator;
import com.elster.jupiter.validation.ValidatorFactory;
import com.google.common.base.Optional;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.osgi.framework.BundleContext;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;

/**
 * Tests integration of all internal components involved in validation.
 * Only interfaces of outside the bundle and implementations of Validator (since there are no internal implementations by design) have been mocked.
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidationIT {

    private static final String MIN_MAX = "minMax";
    private static final String CONSECUTIVE_ZEROES = "consecutiveZeroes";
    private static final String MY_RULE_SET = "MyRuleSet";
    private static final String MAX_NUMBER_IN_SEQUENCE = "maxNumberInSequence";
    private static final String MIN = "min";
    private static final String MAX = "max";
    private static final long METERACTIVATION_ID = 101L;
    private static final long CHANNEL1_ID = 1001L;
    private static final long CHANNEL2_ID = 1002L;
    private static final Date date1 = new DateTime(1983, 5, 31, 14, 0, 0).toDate();
    private static final Date date2 = new DateTime(1983, 5, 31, 15, 0, 0).toDate();
    private static final Date date3 = new DateTime(1983, 5, 31, 16, 0, 0).toDate();
    private static final Date date4 = new DateTime(1983, 5, 31, 17, 0, 0).toDate();
    private static final Date date5 = new DateTime(1983, 5, 31, 18, 0, 0).toDate();
    private static final Date date6 = new DateTime(1983, 5, 31, 19, 0, 0).toDate();


    private AtomicLong ids = new AtomicLong(0);

    private DataMapper<MeterActivationValidation> meterActivationValidationFactory;
    private DataMapper<ChannelValidation> channelValidationFactory;
    private ValidationStats validationStats;


    @Mock
    private OrmService ormService;
    @Mock
    private Clock clock;
    @Mock
    private EventService eventService;
    @Mock
    private MeteringService meteringService;
    @Mock
    private ValidatorFactory validatorFactory;
    @Mock
    private Validator consecutiveZeroesValidator, minMaxValidator;
    @Mock
    private ReadingType readingType1, readingType2, readingType3;
    @Mock
    private DataModel dataModel;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Table table;
    @Mock
    private BundleContext bundleContext;
    @Mock
    private DataMapper<IValidationRule> ruleFactory;
    @Mock
    private DataMapper<ReadingTypeInValidationRule> readingTypeInRuleFactory;
    @Mock
    private DataMapper<ValidationRuleProperties> validationRulePropertyFactory;
    @Mock
    private DataMapper<IValidationRuleSet> ruleSetFactory;
    @Mock
    private MeterActivation meterActivation;
    @Mock
    private Channel channel1, channel2;
    @Mock
    private EventType eventType;
    @Mock
    private LocalEvent localEvent;
    @Mock
    private ReadingStorer readingStorer;
    @Mock
    private IntervalReadingRecord reading1_1, reading1_2, reading1_3, reading1_4, reading1_5, reading2_1, reading2_2, reading2_3, reading2_4, reading2_5;
    @Mock
    private ValidatorCreator validatorCreator;
    @Mock
    private Thesaurus thesaurus;

    @Before
    public void setUp() {
        meterActivationValidationFactory = new Fakes.MeterActivationValidationFactory();
        channelValidationFactory = new Fakes.ChannelValidationFactory();
        validationStats = new ValidationStats(new DateTime(2012, 12, 4, 17, 15, 0, 0).toDate(), 0);

        ids = new AtomicLong(0);

        when(clock.now()).thenReturn(new DateTime(2012, 12, 24, 17, 14, 22, 124).toDate());

//        when(validatorFactory.available()).thenReturn(Arrays.asList(CONSECUTIVE_ZEROES, MIN_MAX));
        when(validatorCreator.getValidator(eq(CONSECUTIVE_ZEROES), anyMap())).thenReturn(consecutiveZeroesValidator);
        when(validatorCreator.getValidator(eq(MIN_MAX), anyMap())).thenReturn(minMaxValidator);
        when(consecutiveZeroesValidator.getRequiredKeys()).thenReturn(Arrays.asList(MAX_NUMBER_IN_SEQUENCE));
        when(consecutiveZeroesValidator.validate(any(IntervalReadingRecord.class))).thenReturn(ValidationResult.PASS);
        when(consecutiveZeroesValidator.getReadingQualityTypeCode()).thenReturn(Optional.<ReadingQualityType>absent());
        when(minMaxValidator.getRequiredKeys()).thenReturn(Arrays.asList(MIN, MAX));
        when(minMaxValidator.validate(any(IntervalReadingRecord.class))).thenReturn(ValidationResult.PASS);
        when(minMaxValidator.getReadingQualityTypeCode()).thenReturn(Optional.<ReadingQualityType>absent());

        when(ormService.newDataModel(ValidationService.COMPONENTNAME, "Validation")).thenReturn(dataModel);
        when(dataModel.mapper(IValidationRule.class)).thenReturn(ruleFactory);
        when(dataModel.mapper(ValidationRuleProperties.class)).thenReturn(validationRulePropertyFactory);
        when(dataModel.mapper(IValidationRuleSet.class)).thenReturn(ruleSetFactory);
        when(dataModel.addTable(anyString(), any(Class.class))).thenReturn(table);
        when(dataModel.mapper(ReadingTypeInValidationRule.class)).thenReturn(readingTypeInRuleFactory);
        when(dataModel.mapper(MeterActivationValidation.class)).thenReturn(meterActivationValidationFactory);
        when(dataModel.mapper(ChannelValidation.class)).thenReturn(channelValidationFactory);
        when(dataModel.getInstance(ValidationRuleSetImpl.class)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new ValidationRuleSetImpl(dataModel, eventService);
            }
        });
        when(dataModel.getInstance(ValidationRuleImpl.class)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new ValidationRuleImpl(dataModel, validatorCreator, thesaurus, meteringService);
            }
        });
        when(dataModel.getInstance(ReadingTypeInValidationRuleImpl.class)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new ReadingTypeInValidationRuleImpl(dataModel, meteringService);
            }
        });
        when(dataModel.getInstance(ValidationRulePropertiesImpl.class)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new ValidationRulePropertiesImpl();
            }
        });
        when(dataModel.getInstance(MeterActivationValidationImpl.class)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new MeterActivationValidationImpl(dataModel, meteringService, clock);
            }
        });
        when(dataModel.getInstance(ChannelValidationImpl.class)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new ChannelValidationImpl(dataModel, meteringService);
            }
        });

        doAnswer(new AssignId()).when(ruleSetFactory).persist(isA(ValidationRuleSetImpl.class));

        when(meterActivation.getId()).thenReturn(METERACTIVATION_ID);
        when(meterActivation.getChannels()).thenReturn(Arrays.asList(channel1, channel2));
        when(channel1.getId()).thenReturn(CHANNEL1_ID);
        when(channel2.getId()).thenReturn(CHANNEL2_ID);
        when(meteringService.findChannel(CHANNEL1_ID)).thenReturn(Optional.of(channel1));
        when(meteringService.findChannel(CHANNEL2_ID)).thenReturn(Optional.of(channel2));

        when(channel1.getMeterActivation()).thenReturn(meterActivation);
        // using doReturn as when().thenReturn stumbles on List<? extends xxx> return type
        doReturn(Arrays.asList(readingType1,readingType2)).when(channel1).getReadingTypes();
        when(channel2.getMeterActivation()).thenReturn(meterActivation);
        doReturn(Arrays.asList(readingType1,readingType3)).when(channel2).getReadingTypes();

        when(eventType.getTopic()).thenReturn("com/elster/jupiter/metering/reading/CREATED");
        Map<Channel, Interval> map = new HashMap<>();
        map.put(channel1, interval(date1, date2));
        map.put(channel2, interval(date3, date5));

        when(localEvent.getSource()).thenReturn(readingStorer);
        when(localEvent.getType()).thenReturn(eventType);

        when(readingStorer.getScope()).thenReturn(map);

    }

    private Interval interval(Date from, Date to) {
        return new Interval(from, to);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testValidation() {
        when(channel1.isRegular()).thenReturn(true);
        when(channel2.isRegular()).thenReturn(true);
        when(channel1.getIntervalReadings(readingType1, interval(date1, date5))).thenReturn(Arrays.asList(reading1_1, reading1_2, reading1_3, reading1_4, reading1_5));
        when(channel1.getIntervalReadings(readingType2, interval(date1, date5))).thenReturn(Arrays.asList(reading1_1, reading1_2, reading1_3, reading1_4, reading1_5));
        when(channel2.getIntervalReadings(readingType1, interval(date1, date5))).thenReturn(Arrays.asList(reading2_1, reading2_2, reading2_3, reading2_4, reading2_5));
        when(channel2.getIntervalReadings(readingType3, interval(date1, date5))).thenReturn(Arrays.asList(reading2_1, reading2_2, reading2_3, reading2_4, reading2_5));

        ValidationServiceImpl validationService = new ValidationServiceImpl();
        validationService.setOrmService(ormService);
        validationService.setClock(clock);
        validationService.setEventService(eventService);
        validationService.setMeteringService(meteringService);

        validationService.activate();

        validationService.addResource(validatorFactory);

        ValidationEventHandler validationEventHandler = new ValidationEventHandler();
        validationEventHandler.setValidationService(validationService);

        ValidationRuleSet validationRuleSet = validationService.createValidationRuleSet(MY_RULE_SET);
        ValidationRule zeroesRule = validationRuleSet.addRule(ValidationAction.FAIL, CONSECUTIVE_ZEROES, "consecutivezeros");
        zeroesRule.addReadingType(readingType1);
        zeroesRule.addReadingType(readingType2);
        zeroesRule.addProperty(MAX_NUMBER_IN_SEQUENCE, Unit.UNITLESS.amount(BigDecimal.valueOf(20)));
        zeroesRule.activate();
        ValidationRule minMaxRule = validationRuleSet.addRule(ValidationAction.WARN_ONLY, MIN_MAX, "minmax");
        minMaxRule.addReadingType(readingType3);
        minMaxRule.addReadingType(readingType2);
        minMaxRule.addProperty(MIN, Unit.WATT_HOUR.amount(BigDecimal.valueOf(1), 3));
        minMaxRule.addProperty(MAX, Unit.WATT_HOUR.amount(BigDecimal.valueOf(100), 3));
        minMaxRule.activate();
        validationRuleSet.save();

        assertThat(validationRuleSet.getId()).isNotEqualTo(0L);

        validationService.applyRuleSet(validationRuleSet, meterActivation);

        validationEventHandler.handle(localEvent);

        verify(consecutiveZeroesValidator, times(2)).validate(reading1_1);
        verify(consecutiveZeroesValidator, times(2)).validate(reading1_2);
        verify(consecutiveZeroesValidator, times(2)).validate(reading1_3);
        verify(consecutiveZeroesValidator, times(2)).validate(reading1_4);
        verify(consecutiveZeroesValidator, times(2)).validate(reading1_5);
        verify(consecutiveZeroesValidator).validate(reading2_1);
        verify(consecutiveZeroesValidator).validate(reading2_2);
        verify(consecutiveZeroesValidator).validate(reading2_3);
        verify(consecutiveZeroesValidator).validate(reading2_4);
        verify(consecutiveZeroesValidator).validate(reading2_5);
        verify(minMaxValidator).validate(reading1_1);
        verify(minMaxValidator).validate(reading1_2);
        verify(minMaxValidator).validate(reading1_3);
        verify(minMaxValidator).validate(reading1_4);
        verify(minMaxValidator).validate(reading1_5);
        verify(minMaxValidator).validate(reading2_1);
        verify(minMaxValidator).validate(reading2_2);
        verify(minMaxValidator).validate(reading2_3);
        verify(minMaxValidator).validate(reading2_4);
        verify(minMaxValidator).validate(reading2_5);
    }

    private class AssignId implements Answer<Void> {

        @Override
        public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
            field("id").ofType(Long.TYPE).in(invocationOnMock.getArguments()[0]).set(ids.incrementAndGet());
            return null;
        }
    }
}
