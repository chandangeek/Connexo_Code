package com.elster.jupiter.validation.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.cache.CacheService;
import com.elster.jupiter.orm.cache.ComponentCache;
import com.elster.jupiter.orm.cache.TypeCache;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Unit;
import com.elster.jupiter.validation.ReadingTypeInValidationRule;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleProperties;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationStats;
import com.elster.jupiter.validation.Validator;
import com.elster.jupiter.validation.ValidatorFactory;
import com.google.common.base.Optional;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
    private CacheService cacheService;
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
    @Mock
    private Table table;
    @Mock
    private BundleContext bundleContext;
    @Mock
    private ComponentCache componentCache;
    @Mock
    private TypeCache<IValidationRule> ruleFactory;
    @Mock
    private DataMapper<ReadingTypeInValidationRule> readingTypeInRuleFactory;
    @Mock
    private TypeCache<ValidationRuleProperties> validationRulePropertyFactory;
    @Mock
    private TypeCache<IValidationRuleSet> ruleSetFactory;
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

    @Before
    public void setUp() {
        meterActivationValidationFactory = new Fakes.MeterActivationValidationFactory();
        channelValidationFactory = new Fakes.ChannelValidationFactory();
        validationStats = new ValidationStats(new DateTime(2012, 12, 4, 17, 15, 0, 0).toDate(), 0);

        ids = new AtomicLong(0);

        when(clock.now()).thenReturn(new DateTime(2012, 12, 24, 17, 14, 22, 124).toDate());

        when(validatorFactory.available()).thenReturn(Arrays.asList(CONSECUTIVE_ZEROES, MIN_MAX));
        when(validatorFactory.create(eq(CONSECUTIVE_ZEROES), anyMap())).thenReturn(consecutiveZeroesValidator);
        when(validatorFactory.create(eq(MIN_MAX), anyMap())).thenReturn(minMaxValidator);
        when(consecutiveZeroesValidator.getRequiredKeys()).thenReturn(Arrays.asList(MAX_NUMBER_IN_SEQUENCE));
        when(consecutiveZeroesValidator.validate(any(Channel.class), any(ReadingType.class), eq(interval(date1, date5)))).thenReturn(validationStats);
        when(minMaxValidator.getRequiredKeys()).thenReturn(Arrays.asList(MIN, MAX));
        when(minMaxValidator.validate(any(Channel.class), any(ReadingType.class), eq(interval(date1, date5)))).thenReturn(validationStats);

        when(ormService.newDataModel(Bus.COMPONENTNAME, "Validation")).thenReturn(dataModel);
        when(cacheService.createComponentCache(dataModel)).thenReturn(componentCache);
        when(componentCache.getTypeCache(IValidationRule.class, ValidationRuleImpl.class, TableSpecs.VAL_VALIDATIONRULE.name())).thenReturn(ruleFactory);
        when(componentCache.getTypeCache(ValidationRuleProperties.class, ValidationRulePropertiesImpl.class, TableSpecs.VAL_VALIDATIONRULEPROPS.name())).thenReturn(validationRulePropertyFactory);
        when(componentCache.getTypeCache(IValidationRuleSet.class, ValidationRuleSetImpl.class, TableSpecs.VAL_VALIDATIONRULESET.name())).thenReturn(ruleSetFactory);
        when(dataModel.addTable(anyString())).thenReturn(table);
        when(dataModel.getDataMapper(ReadingTypeInValidationRule.class, ReadingTypeInValidationRuleImpl.class, TableSpecs.VAL_READINGTYPEINVALRULE.name())).thenReturn(readingTypeInRuleFactory);
        when(dataModel.getDataMapper(MeterActivationValidation.class, MeterActivationValidationImpl.class, TableSpecs.VAL_MA_VALIDATION.name())).thenReturn(meterActivationValidationFactory);
        when(dataModel.getDataMapper(ChannelValidation.class, ChannelValidationImpl.class, TableSpecs.VAL_CH_VALIDATION.name())).thenReturn(channelValidationFactory);

        doAnswer(new AssignId()).when(ruleSetFactory).persist(isA(ValidationRuleSetImpl.class));

        when(meterActivation.getId()).thenReturn(METERACTIVATION_ID);
        when(meterActivation.getChannels()).thenReturn(Arrays.asList(channel1, channel2));
        when(channel1.getId()).thenReturn(CHANNEL1_ID);
        when(channel2.getId()).thenReturn(CHANNEL2_ID);
        when(meteringService.findChannel(CHANNEL1_ID)).thenReturn(Optional.of(channel1));
        when(meteringService.findChannel(CHANNEL2_ID)).thenReturn(Optional.of(channel2));

        when(channel1.getMeterActivation()).thenReturn(meterActivation);
        when(channel1.getReadingTypes()).thenReturn(Arrays.asList(readingType1, readingType2));
        when(channel2.getMeterActivation()).thenReturn(meterActivation);
        when(channel2.getReadingTypes()).thenReturn(Arrays.asList(readingType1, readingType3));

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
        ValidationServiceImpl validationService = new ValidationServiceImpl();
        validationService.setOrmService(ormService);
        validationService.setCacheService(cacheService);
        validationService.setClock(clock);
        validationService.setEventService(eventService);
        validationService.setMeteringService(meteringService);

        validationService.activate(bundleContext);

        validationService.addResource(validatorFactory);

        ValidationEventHandler validationEventHandler = new ValidationEventHandler();
        validationEventHandler.setValidationService(validationService);

        ValidationRuleSet validationRuleSet = validationService.createValidationRuleSet(MY_RULE_SET);
        ValidationRule zeroesRule = validationRuleSet.addRule(ValidationAction.FAIL, CONSECUTIVE_ZEROES);
        zeroesRule.addReadingType(readingType1);
        zeroesRule.addReadingType(readingType2);
        zeroesRule.addProperty(MAX_NUMBER_IN_SEQUENCE, Unit.UNITLESS.amount(BigDecimal.valueOf(20)));
        ValidationRule minMaxRule = validationRuleSet.addRule(ValidationAction.WARN_ONLY, MIN_MAX);
        minMaxRule.addReadingType(readingType3);
        minMaxRule.addReadingType(readingType2);
        minMaxRule.addProperty(MIN, Unit.WATT_HOUR.amount(BigDecimal.valueOf(1), 3));
        minMaxRule.addProperty(MAX, Unit.WATT_HOUR.amount(BigDecimal.valueOf(100), 3));
        validationRuleSet.save();

        assertThat(validationRuleSet.getId()).isNotEqualTo(0L);

        validationService.applyRuleSet(validationRuleSet, meterActivation);

        validationEventHandler.handle(localEvent);

        verify(consecutiveZeroesValidator).validate(channel1, readingType1, interval(date1, date5));
        verify(consecutiveZeroesValidator).validate(channel1, readingType2, interval(date1, date5));
        verify(consecutiveZeroesValidator).validate(channel2, readingType1, interval(date1, date5));
        verify(minMaxValidator).validate(channel1, readingType2, interval(date1, date5));
        verify(minMaxValidator).validate(channel2, readingType3, interval(date1, date5));
    }

    private class AssignId implements Answer<Void> {

        @Override
        public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
            field("id").ofType(Long.TYPE).in(invocationOnMock.getArguments()[0]).set(ids.incrementAndGet());
            return null;
        }
    }
}
