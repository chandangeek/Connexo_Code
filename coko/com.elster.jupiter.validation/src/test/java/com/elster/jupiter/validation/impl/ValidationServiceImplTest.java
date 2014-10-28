package com.elster.jupiter.validation.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.validation.ChannelValidation;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.MeterActivationValidation;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleProperties;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetResolver;
import com.elster.jupiter.validation.Validator;
import com.elster.jupiter.validation.ValidatorFactory;
import com.elster.jupiter.validation.ValidatorNotFoundException;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ValidationServiceImplTest {

    private static final String NAME = "name";
    private static final long ID = 561651L;
    private ValidationServiceImpl validationService;

    @Mock
    private EventService eventService;
    @Mock
    private ValidatorFactory factory;
    @Mock
    private Validator validator;
    @Mock
    private DataMapper<IValidationRuleSet> validationRuleSetFactory;
    @Mock
    private DataMapper<IValidationRule> validationRuleFactory;
    @Mock
    private DataMapper<MeterValidationImpl> meterValidationFactory;
    @Mock
    private MeterActivation meterActivation;
    @Mock
    private Meter meter;
    @Mock
    private Channel channel1, channel2;
    @Mock
    private DataMapper<ChannelValidation> channelValidationFactory;
    @Mock
    private MeterValidationImpl meterValidation;
    @Mock
    private DataModel dataModel;
    @Mock
    private OrmService ormService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Table table;
    private Clock clock = Clock.systemDefaultZone();
    @Mock
    private MeteringService meteringService;
    @Mock
    private NlsService nlsService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private NlsMessageFormat nlsMessageFormat;
    @Mock
    private javax.validation.ValidatorFactory validatorFactory;
    @Mock
    private javax.validation.Validator javaxValidator;
    @Mock
    private QueryExecutor<IValidationRule> validationRuleQueryExecutor;
    @Mock
    private ValidationRuleSetResolver validationRuleSetResolver;
    @Mock
    private QueryExecutor<IMeterActivationValidation> queryExecutor;
    @Mock
    private QueryService queryService;
    @Mock
    private Query<IValidationRule> allValidationRuleQuery;
    @Mock
    private IMeterActivationValidation meterActivationValidation;
    @Mock
    private IChannelValidation channelValidation1, channelValidation2;
    @Mock
    private ReadingQualityRecord readingQuality1, readingQuality2, readingQuality3;

    @Before
    public void setUp() {
        when(ormService.newDataModel(anyString(), anyString())).thenReturn(dataModel);
        when(dataModel.addTable(anyString(), any())).thenReturn(table);
        when(dataModel.mapper(IValidationRuleSet.class)).thenReturn(validationRuleSetFactory);
        when(dataModel.mapper(IValidationRule.class)).thenReturn(validationRuleFactory);        
        when(dataModel.mapper(ChannelValidation.class)).thenReturn(channelValidationFactory);
        when(dataModel.mapper(MeterValidationImpl.class)).thenReturn(meterValidationFactory);
        when(nlsService.getThesaurus(anyString(), any(Layer.class))).thenReturn(thesaurus);
        when(dataModel.query(IValidationRule.class, IValidationRuleSet.class, ValidationRuleProperties.class)).thenReturn(validationRuleQueryExecutor);
        when(dataModel.query(IValidationRule.class)).thenReturn(validationRuleQueryExecutor);
        when(queryService.wrap(eq(validationRuleQueryExecutor))).thenReturn(allValidationRuleQuery);

        validationService = new ValidationServiceImpl();
        validationService.setOrmService(ormService);
        validationService.setNlsService(nlsService);
        validationService.setQueryService(queryService);
        validationService.addValidationRuleSetResolver(validationRuleSetResolver);

        when(factory.available()).thenReturn(Arrays.asList(validator.getClass().getName()));
        when(factory.create(validator.getClass().getName(), null)).thenReturn(validator);
        when(dataModel.getInstance(ValidationRuleSetImpl.class)).thenAnswer(invocationOnMock -> new ValidationRuleSetImpl(dataModel, eventService));
        when(dataModel.getInstance(MeterActivationValidationImpl.class)).thenAnswer(invocationOnMock -> new MeterActivationValidationImpl(dataModel, clock));
        when(dataModel.getInstance(ChannelValidationImpl.class)).thenAnswer(invocationOnMock -> new ChannelValidationImpl());
        when(dataModel.query(IMeterActivationValidation.class, ChannelValidation.class)).thenReturn(queryExecutor);
        when(queryExecutor.select(any())).thenReturn(Collections.emptyList());
        when(thesaurus.getFormat(any())).thenReturn(nlsMessageFormat);
        when(dataModel.getValidatorFactory()).thenReturn(validatorFactory);
        when(dataModel.getValidatorFactory().getValidator()).thenReturn(javaxValidator);
    }

    private void setupValidationRuleSet(ChannelValidation channelValidation, Channel channel, boolean activeRules, ReadingQualityType... qualities) {
        MeterActivationValidation meterActivationValidation = mock(MeterActivationValidation.class);
        when(channelValidation.getMeterActivationValidation()).thenReturn(meterActivationValidation);
        ValidationRuleSet validationRuleSet = mock(ValidationRuleSet.class);
        when(meterActivationValidation.getRuleSet()).thenReturn(validationRuleSet);
        when(channelValidation.getChannel()).thenReturn(channel);

        ReadingType readingType = channel.getMainReadingType();
        List<IValidationRule> validationRules = new ArrayList<>();
        for (ReadingQualityType quality : qualities) {
            IValidationRule rule = mock(IValidationRule.class);
            when(rule.getReadingQualityType()).thenReturn(quality);
            when(rule.getReadingTypes()).thenReturn(Collections.singleton(readingType));
            validationRules.add(rule);
        }
        if (!validationRules.isEmpty()) {
            when(allValidationRuleQuery.select(any(Condition.class))).thenReturn(validationRules);
            doReturn(validationRules).when(validationRuleSet).getRules();
        }
        when(channelValidation.hasActiveRules()).thenReturn(activeRules);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetImplementation() {
        validationService.addResource(factory);

        Validator found = validationService.new DefaultValidatorCreator().getValidator(validator.getClass().getName(), null);

        assertThat(found).isNotNull().isEqualTo(validator);
    }

    @Test(expected = ValidatorNotFoundException.class)
    public void testGetValidatorThrowsNotFoundExceptionIfNoFactoryProvidesImplementation() {
        validationService.new DefaultValidatorCreator().getValidator(validator.getClass().getName(), null);
    }

    @Test
    public void testCreateRuleSet() {
        ValidationRuleSet validationRuleSet = validationService.createValidationRuleSet(NAME);

        assertThat(validationRuleSet.getName()).isEqualTo(NAME);
    }

    @Test
    public void testApplyRuleSetWithChannels() {
        when(meterActivation.getId()).thenReturn(ID);
        when(meterActivation.getMeter()).thenReturn(Optional.of(meter));
        when(meter.getId()).thenReturn(ID);
        when(meterActivation.getChannels()).thenReturn(Arrays.asList(channel1, channel2));
        when(channel1.getId()).thenReturn(1001L);
        ReadingType readingType = mock(ReadingType.class);
        doReturn(Arrays.asList(readingType)).when(channel1).getReadingTypes();
        when(channel2.getId()).thenReturn(1002L);
        when(channel1.getMeterActivation()).thenReturn(meterActivation);
        when(channel2.getMeterActivation()).thenReturn(meterActivation);
        when(meterValidationFactory.getOptional(ID)).thenReturn(Optional.of(meterValidation));
        when(meterValidation.getActivationStatus()).thenReturn(true);
        doAnswer((invocation) -> {
        		Object meterActivationValidation = invocation.getArguments()[0];        		
        		Field field = meterActivationValidation.getClass().getDeclaredField("id");
    			field.setAccessible(true);
    			field.set(meterActivationValidation, 1L);
        		return null;
    	 	}).when(dataModel).persist(any(IMeterActivationValidation.class));
        	
        ValidationRuleSet validationRuleSet = mock(IValidationRuleSet.class);
        ValidationRule validationRule = mock(IValidationRule.class);
        doReturn(Collections.singleton(readingType)).when(validationRule).getReadingTypes();
        doReturn(Arrays.asList(validationRule)).when(validationRuleSet).getRules(anyList());
        when(validationRuleSetResolver.resolve(eq(meterActivation))).thenReturn(Arrays.asList(validationRuleSet));
        validationService.validate(meterActivation, Range.atLeast(Instant.EPOCH));

        ArgumentCaptor<IMeterActivationValidation> meterActivationValidationCapture = ArgumentCaptor.forClass(IMeterActivationValidation.class);
        verify(dataModel).persist(meterActivationValidationCapture.capture());

        final IMeterActivationValidation meterActivationValidationCaptureValue = meterActivationValidationCapture.getValue();
        final Set<ChannelValidation> channelValidations = meterActivationValidationCaptureValue.getChannelValidations();
        assertThat(FluentIterable.from(channelValidations).allMatch(new Predicate<ChannelValidation>() {
            @Override
            public boolean apply(ChannelValidation input) {
                return input.getMeterActivationValidation().equals(meterActivationValidationCaptureValue);
            }
        })).isTrue();
        assertThat(FluentIterable.from(channelValidations).transform(new Function<ChannelValidation, Channel>() {

            @Override
            public Channel apply(ChannelValidation input) {
                return input.getChannel();
            }
        }).toSet()).contains(channel1);

    }

    @Test
    public void testApplyRuleSetWithChannelsAndOverwrite() {
        when(meterActivation.getId()).thenReturn(ID);
        when(meterActivation.getMeter()).thenReturn(Optional.of(meter));
        when(meter.getId()).thenReturn(ID);
        when(meterActivation.getChannels()).thenReturn(Arrays.asList(channel1, channel2));
        when(channel1.getId()).thenReturn(1001L);
        ReadingType readingType = mock(ReadingType.class);
        doReturn(Arrays.asList(readingType)).when(channel1).getReadingTypes();
        when(channel2.getId()).thenReturn(1002L);
        when(channel1.getMeterActivation()).thenReturn(meterActivation);
        when(channel2.getMeterActivation()).thenReturn(meterActivation);
        when(meterValidationFactory.getOptional(ID)).thenReturn(Optional.of(meterValidation));
        when(meterValidation.getActivationStatus()).thenReturn(false);
        when(queryExecutor.select(any(Condition.class))).thenReturn(Arrays.asList(meterActivationValidation));
        when(meterActivationValidation.getMaxLastChecked()).thenReturn(Instant.ofEpochMilli(5000L));
        when(meterActivationValidation.getChannelValidations()).thenReturn(ImmutableSet.of(channelValidation1, channelValidation2));
        when(channelValidation1.getLastChecked()).thenReturn(Instant.ofEpochMilli(-5000));
        when(channelValidation2.getLastChecked()).thenReturn(Instant.ofEpochMilli(5000L));
        when(channelValidation1.getChannel()).thenReturn(channel1);
        when(channelValidation2.getChannel()).thenReturn(channel2);
        when(channel1.findReadingQuality(Range.atLeast(Instant.EPOCH))).thenReturn(Arrays.asList(readingQuality1));
        when(channel2.findReadingQuality(Range.atLeast(Instant.EPOCH))).thenReturn(Arrays.asList(readingQuality2, readingQuality3));
        when(readingQuality1.getTypeCode()).thenReturn("3.6.1");
        when(readingQuality2.getTypeCode()).thenReturn("1.0.0");
        when(readingQuality3.getTypeCode()).thenReturn("3.6.2");
        when(readingQuality1.getType()).thenReturn(new ReadingQualityType("3.6.1"));
        when(readingQuality2.getType()).thenReturn(new ReadingQualityType("1.0.0"));
        when(readingQuality3.getType()).thenReturn(new ReadingQualityType("3.6.2"));

        ValidationRuleSet validationRuleSet = mock(IValidationRuleSet.class);

        when(meterActivationValidation.getRuleSet()).thenReturn(validationRuleSet);

        ValidationRule validationRule = mock(IValidationRule.class);
        doReturn(Collections.singleton(readingType)).when(validationRule).getReadingTypes();
        doReturn(Arrays.asList(validationRule)).when(validationRuleSet).getRules(anyList());
        when(validationRuleSetResolver.resolve(eq(meterActivation))).thenReturn(Arrays.asList(validationRuleSet));
        validationService.validateForNewData(meterActivation, Range.atLeast(Instant.EPOCH));
        verify(channelValidation2).updateLastChecked(Instant.ofEpochMilli(0L));
        verify(meterActivationValidation).save();
    }

    @Test
    public void testManageValidationActivations() {
        when(meterActivation.getId()).thenReturn(ID);
        when(meterActivation.getMeter()).thenReturn(Optional.of(meter));
        when(meter.getId()).thenReturn(ID);
        when(meterActivation.getChannels()).thenReturn(Arrays.asList(channel1, channel2));
        when(channel1.getId()).thenReturn(1001L);
        when(channel2.getId()).thenReturn(1002L);
        when(channel1.getMeterActivation()).thenReturn(meterActivation);
        when(channel2.getMeterActivation()).thenReturn(meterActivation);
        when(meterValidationFactory.getOptional(ID)).thenReturn(Optional.of(meterValidation));
        when(meterValidation.getActivationStatus()).thenReturn(true);

        ValidationRuleSet validationRuleSet = validationService.createValidationRuleSet(NAME);
        validationRuleSet.save();

        when(validationRuleSetResolver.resolve(eq(meterActivation))).thenReturn(Arrays.asList(validationRuleSet));
        validationService.validate(meterActivation, Range.atLeast(Instant.EPOCH));

        List<IMeterActivationValidation> meterActivationValidations = validationService.getUpdatedMeterActivationValidations(meterActivation);
        assertThat(meterActivationValidations).hasSize(1);
        assertThat(meterActivationValidations.get(0).getMeterActivation()).isEqualTo(meterActivation);
        assertThat(meterActivationValidations.get(0).getRuleSet()).isEqualTo(validationRuleSet);
        MeterActivationValidation activationRuleSet1 = meterActivationValidations.get(0);

        ValidationRuleSet validationRuleSet2 = validationService.createValidationRuleSet(NAME);
        validationRuleSet2.save();

        when(validationRuleSetResolver.resolve(eq(meterActivation))).thenReturn(Arrays.asList(validationRuleSet, validationRuleSet2));
        validationService.validate(meterActivation, Range.atLeast(Instant.EPOCH));
        meterActivationValidations = validationService.getUpdatedMeterActivationValidations(meterActivation);
        assertThat(meterActivationValidations).hasSize(2);
        assertThat(meterActivationValidations.get(0).getMeterActivation()).isEqualTo(meterActivation);
        assertThat(meterActivationValidations.get(1).getMeterActivation()).isEqualTo(meterActivation);
        assertThat(FluentIterable.from(meterActivationValidations).transform(MeterActivationValidation::getRuleSet).toSet()).contains(validationRuleSet, validationRuleSet2);

        when(validationRuleSetResolver.resolve(eq(meterActivation))).thenReturn(Arrays.asList(validationRuleSet2));
        validationService.validate(meterActivation, Range.atLeast(Instant.EPOCH));
        meterActivationValidations = validationService.getUpdatedMeterActivationValidations(meterActivation);
        assertThat(meterActivationValidations).hasSize(1);
        assertThat(meterActivationValidations.get(0).getMeterActivation()).isEqualTo(meterActivation);
        assertThat(meterActivationValidations.get(0).getRuleSet()).isEqualTo(validationRuleSet2);
        assertThat(activationRuleSet1.isObsolete());
    }

    @Test
    public void testGetValidationStatusOnEmptyList() {
        assertThat(validationService.getEvaluator().getValidationStatus(channel1, Collections.<BaseReading>emptyList())).isEmpty();
    }

    @Test
    public void testGetValidationStatusOnNonValidated() {
        Instant readingDate = Instant.now();
        BaseReading reading = mock(BaseReading.class);
        when(reading.getTimeStamp()).thenReturn(readingDate);

        when(channel1.createReadingQuality(any(ReadingQualityType.class), any(Instant.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                ReadingQualityRecord record = mock(ReadingQualityRecord.class);
                ReadingQualityType readingQualityType = (ReadingQualityType) invocationOnMock.getArguments()[0];
                when(record.getType()).thenReturn(readingQualityType);
                when(record.getTypeCode()).thenReturn(readingQualityType.getCode());
                return record;
            }
        });

        when(channelValidationFactory.find(eq("channel"), eq(channel1))).thenReturn(Collections.<ChannelValidation>emptyList());

        List<DataValidationStatus> validationStatus = validationService.getEvaluator().getValidationStatus(channel1, Arrays.asList(reading));
        assertThat(validationStatus).hasSize(1);
        assertThat(validationStatus.get(0).getReadingTimestamp()).isEqualTo(readingDate);
        assertThat(validationStatus.get(0).completelyValidated()).isFalse();
        assertThat(validationStatus.get(0).getReadingQualities()).hasSize(0);

        ChannelValidation channelValidation = mock(ChannelValidation.class);
        when(channelValidationFactory.find(eq("channel"), eq(channel1))).thenReturn(Arrays.asList(channelValidation));
        when(channelValidation.getLastChecked()).thenReturn(Instant.ofEpochMilli(0));
        setupValidationRuleSet(channelValidation, channel1, true);

        validationStatus = validationService.getEvaluator().getValidationStatus(channel1, Arrays.asList(reading));
        assertThat(validationStatus).hasSize(1);
        assertThat(validationStatus.get(0).getReadingTimestamp()).isEqualTo(readingDate);
        assertThat(validationStatus.get(0).completelyValidated()).isFalse();
        assertThat(validationStatus.get(0).getReadingQualities()).isEmpty();

    }

    @Test
    public void testGetValidationStatus() {
        Instant readingDate1 = ZonedDateTime.of(2012, 1, 2, 3, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        Instant readingDate2 = ZonedDateTime.of(2012, 1, 2, 5, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        BaseReading reading1 = mock(BaseReading.class);
        when(reading1.getTimeStamp()).thenReturn(readingDate1);
        BaseReading reading2 = mock(BaseReading.class);
        when(reading2.getTimeStamp()).thenReturn(readingDate2);


        ChannelValidation channelValidation = mock(ChannelValidation.class);
        when(channelValidationFactory.find(eq("channel"), eq(channel1))).thenReturn(Arrays.asList(channelValidation));
        when(channelValidation.getLastChecked()).thenReturn(readingDate1);
        when(channel1.findReadingQuality(eq(Range.closed(readingDate1, readingDate2)))).thenReturn(Collections.<ReadingQualityRecord>emptyList());
        ReadingQualityRecord readingQualityRecord = mock(ReadingQualityRecord.class);
        when(channel1.createReadingQuality(any(ReadingQualityType.class), eq(readingDate1))).thenReturn(readingQualityRecord);

        setupValidationRuleSet(channelValidation, channel1, true);

// !! remark that the order of the reading is by purpose not chronological !!
        List<DataValidationStatus> validationStatus = validationService.getEvaluator().getValidationStatus(channel1, Arrays.asList(reading2, reading1));
        assertThat(validationStatus).hasSize(2);
        // reading2 has not be validated yet
        assertThat(validationStatus.get(0).getReadingTimestamp()).isEqualTo(readingDate2);
        assertThat(validationStatus.get(0).completelyValidated()).isFalse();
        assertThat(validationStatus.get(0).getReadingQualities()).isEmpty();
        // reading1 is ok
        assertThat(validationStatus.get(1).getReadingTimestamp()).isEqualTo(readingDate1);
        assertThat(validationStatus.get(1).completelyValidated()).isTrue();
        assertThat((Collection<ReadingQuality>) validationStatus.get(1).getReadingQualities()).containsOnly(readingQualityRecord);
        assertThat(validationStatus.get(1).getOffendedValidationRule(readingQualityRecord)).isEmpty();
        ArgumentCaptor<ReadingQualityType> readingQualitypCapture = ArgumentCaptor.forClass(ReadingQualityType.class);
        verify(channel1).createReadingQuality(readingQualitypCapture.capture(), eq(readingDate1));
        assertThat(readingQualitypCapture.getValue().getCode()).isEqualTo(ReadingQualityType.MDM_VALIDATED_OK_CODE);
    }

    @Test
    public void testGetValidationStatusMultipleChannelValidations() {
        Instant readingDate1 = ZonedDateTime.of(2012, 1, 2, 3, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        Instant readingDate2 = ZonedDateTime.of(2012, 1, 2, 5, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        BaseReading reading1 = mock(BaseReading.class);
        when(reading1.getTimeStamp()).thenReturn(readingDate1);
        BaseReading reading2 = mock(BaseReading.class);
        when(reading2.getTimeStamp()).thenReturn(readingDate2);


        ChannelValidation channelValidation1 = mock(ChannelValidation.class);
        when(channelValidation1.getLastChecked()).thenReturn(readingDate1);
        ChannelValidation channelValidation2 = mock(ChannelValidation.class);
        when(channelValidation2.getLastChecked()).thenReturn(readingDate2);
        when(channelValidationFactory.find(eq("channel"), eq(channel1))).thenReturn(Arrays.asList(channelValidation1, channelValidation2));
        when(channel1.findReadingQuality(eq(Range.closed(readingDate1, readingDate2)))).thenReturn(Collections.<ReadingQualityRecord>emptyList());
        ReadingQualityRecord readingQualityRecord = mock(ReadingQualityRecord.class);
        when(channel1.createReadingQuality(any(ReadingQualityType.class), eq(readingDate1))).thenReturn(readingQualityRecord);
        setupValidationRuleSet(channelValidation1, channel1, true);
        setupValidationRuleSet(channelValidation2, channel1, true);

// !! remark that the order of the reading is by purpose not chronological !!
        List<DataValidationStatus> validationStatus = validationService.getEvaluator().getValidationStatus(channel1, Arrays.asList(reading2, reading1));
        assertThat(validationStatus).hasSize(2);
        // reading2 has not be validated yet
        assertThat(validationStatus.get(0).getReadingTimestamp()).isEqualTo(readingDate2);
        assertThat(validationStatus.get(0).completelyValidated()).isFalse();
        assertThat(validationStatus.get(0).getReadingQualities()).isEmpty();
        // reading1 is ok
        assertThat(validationStatus.get(1).getReadingTimestamp()).isEqualTo(readingDate1);
        assertThat(validationStatus.get(1).completelyValidated()).isTrue();
        assertThat((Collection<ReadingQuality>) validationStatus.get(1).getReadingQualities()).containsOnly(readingQualityRecord);
        assertThat(validationStatus.get(1).getOffendedValidationRule(readingQualityRecord)).isEmpty();
        ArgumentCaptor<ReadingQualityType> readingQualitypCapture = ArgumentCaptor.forClass(ReadingQualityType.class);
        verify(channel1).createReadingQuality(readingQualitypCapture.capture(), eq(readingDate1));
        assertThat(readingQualitypCapture.getValue().getCode()).isEqualTo(ReadingQualityType.MDM_VALIDATED_OK_CODE);
    }

    @Test
    public void testGetValidationStatusNewChannelValidations() {
        Instant readingDate1 = ZonedDateTime.of(2012, 1, 2, 3, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        Instant readingDate2 = ZonedDateTime.of(2012, 1, 2, 5, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        BaseReading reading1 = mock(BaseReading.class);
        when(reading1.getTimeStamp()).thenReturn(readingDate1);
        BaseReading reading2 = mock(BaseReading.class);
        when(reading2.getTimeStamp()).thenReturn(readingDate2);


        ChannelValidation channelValidation1 = mock(ChannelValidation.class);
        when(channelValidation1.getLastChecked()).thenReturn(readingDate1);
        ChannelValidation channelValidation2 = mock(ChannelValidation.class);
        when(channelValidation2.getLastChecked()).thenReturn(null);
        when(channelValidationFactory.find(eq("channel"), eq(channel1))).thenReturn(Arrays.asList(channelValidation1, channelValidation2));
        ReadingQualityRecord readingQuality = mock(ReadingQualityRecord.class);
        when(readingQuality.getReadingTimestamp()).thenReturn(readingDate1);
        ReadingQualityType readingQualityType = new ReadingQualityType("3.6.32131");
        when(readingQuality.getType()).thenReturn(readingQualityType);
        when(readingQuality.getTypeCode()).thenReturn("3.6.32131");
        when(channel1.findReadingQuality(eq(Range.closed(readingDate1, readingDate2)))).thenReturn(Arrays.asList(readingQuality));
        when(channel1.createReadingQuality(any(ReadingQualityType.class), eq(readingDate1))).thenReturn(mock(ReadingQualityRecord.class));
        setupValidationRuleSet(channelValidation1, channel1, true, readingQualityType);
        setupValidationRuleSet(channelValidation2, channel1, true);

// !! remark that the order of the reading is by purpose not chronological !!
        List<DataValidationStatus> validationStatus = validationService.getEvaluator().getValidationStatus(channel1, Arrays.asList(reading2, reading1));
        assertThat(validationStatus).hasSize(2);
        // reading2 has not be validated yet
        assertThat(validationStatus.get(0).getReadingTimestamp()).isEqualTo(readingDate2);
        assertThat(validationStatus.get(0).completelyValidated()).isFalse();
        assertThat(validationStatus.get(0).getReadingQualities()).isEmpty();
        // reading1 is not completely validated, but has already suspects
        assertThat(validationStatus.get(1).getReadingTimestamp()).isEqualTo(readingDate1);
        assertThat(validationStatus.get(1).completelyValidated()).isFalse();
        assertThat((Collection<ReadingQuality>) validationStatus.get(1).getReadingQualities()).containsOnly(readingQuality);
        assertThat(validationStatus.get(1).getOffendedValidationRule(readingQuality)).isNotEmpty();

    }

    @Test
    public void testGetValidationStatusWithSuspects() {
        Instant readingDate1 = ZonedDateTime.of(2012, 1, 2, 3, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        Instant readingDate2 = ZonedDateTime.of(2012, 1, 2, 5, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        BaseReading reading1 = mock(BaseReading.class);
        when(reading1.getTimeStamp()).thenReturn(readingDate1);
        BaseReading reading2 = mock(BaseReading.class);
        when(reading2.getTimeStamp()).thenReturn(readingDate2);


        ChannelValidation channelValidation1 = mock(ChannelValidation.class);
        when(channelValidation1.getLastChecked()).thenReturn(readingDate2);
        when(channelValidationFactory.find(eq("channel"), eq(channel1))).thenReturn(Arrays.asList(channelValidation1));
        ReadingQualityRecord readingQuality1 = mock(ReadingQualityRecord.class);
        when(readingQuality1.getReadingTimestamp()).thenReturn(readingDate1);
        ReadingQualityType readingQualityType1 = new ReadingQualityType("3.6.5164");
        when(readingQuality1.getType()).thenReturn(readingQualityType1);
        when(readingQuality1.getTypeCode()).thenReturn("3.6.5164");
        ReadingQualityRecord readingQuality2 = mock(ReadingQualityRecord.class);
        when(readingQuality2.getReadingTimestamp()).thenReturn(readingDate1);
        ReadingQualityType readingQualityType2 = new ReadingQualityType("3.6.9856");
        when(readingQuality2.getType()).thenReturn(readingQualityType2);
        when(readingQuality2.getTypeCode()).thenReturn("3.6.9856");
        when(channel1.findReadingQuality(eq(Range.closed(readingDate1, readingDate2)))).thenReturn(Arrays.asList(readingQuality1, readingQuality2));
        ReadingQualityRecord readingDate2ReadingQuality = mock(ReadingQualityRecord.class);
        when(channel1.createReadingQuality(any(ReadingQualityType.class), eq(readingDate2))).thenReturn(readingDate2ReadingQuality);
        when(channel1.getMainReadingType()).thenReturn(mock(ReadingType.class));
        setupValidationRuleSet(channelValidation1, channel1, true, readingQualityType1, readingQualityType2);

// !! remark that the order of the reading is by purpose not chronological !!
        List<DataValidationStatus> validationStatus = validationService.getEvaluator().getValidationStatus(channel1, Arrays.asList(reading2, reading1));
        assertThat(validationStatus).hasSize(2);
        // reading2 is OK
        assertThat(validationStatus.get(0).getReadingTimestamp()).isEqualTo(readingDate2);
        assertThat(validationStatus.get(0).completelyValidated()).isTrue();
        assertThat((Collection<ReadingQuality>) validationStatus.get(0).getReadingQualities()).containsExactly(readingDate2ReadingQuality);
        ArgumentCaptor<ReadingQualityType> readingQualitypCapture = ArgumentCaptor.forClass(ReadingQualityType.class);
        verify(channel1).createReadingQuality(readingQualitypCapture.capture(), eq(readingDate2));
        assertThat(readingQualitypCapture.getValue().getCode()).isEqualTo(ReadingQualityType.MDM_VALIDATED_OK_CODE);
        // reading1 has suspects
        assertThat(validationStatus.get(1).getReadingTimestamp()).isEqualTo(readingDate1);
        assertThat(validationStatus.get(1).completelyValidated()).isTrue();
        assertThat((Collection<ReadingQuality>) validationStatus.get(1).getReadingQualities()).containsOnly(readingQuality1, readingQuality2);
        assertThat(validationStatus.get(1).getOffendedValidationRule(readingQuality1)).isNotEmpty();
        assertThat(validationStatus.get(1).getOffendedValidationRule(readingQuality2)).isNotEmpty();


    }

    @Test
    public void testMeterValidationActivation() {
        Meter meter = mock(Meter.class);
        when(meter.getId()).thenReturn(ID);
        doReturn(Optional.of(meterActivation)).when(meter).getCurrentMeterActivation();
        when(meterValidationFactory.getOptional(ID)).thenReturn(Optional.<MeterValidationImpl>empty());
        when(dataModel.getInstance(MeterValidationImpl.class)).thenReturn(meterValidation);
        when(meterValidation.init(any(Meter.class))).thenReturn(meterValidation);
        when(validationRuleSetResolver.resolve(eq(meterActivation))).thenReturn(Collections.<ValidationRuleSet>emptyList());


        validationService.activateValidation(meter);

        //Check that a MeterValidation object is made
        verify(dataModel).getInstance(MeterValidationImpl.class);
        verify(meterValidation).init(eq(meter));
        verify(meterValidation).setActivationStatus(true);
        verify(meterValidation).save();

        // verify that the MeterActivationValidations are managed for the current MeterActivation
        verify(meter).getCurrentMeterActivation();
        verify(validationRuleSetResolver).resolve(meterActivation);

    }

    @Test
    public void testDeactivateMeterValidation() {
        Meter meter = mock(Meter.class);
        when(meter.getId()).thenReturn(ID);
        when(meterValidationFactory.getOptional(ID)).thenReturn(Optional.of(meterValidation));
        when(meterValidation.getActivationStatus()).thenReturn(true);


        validationService.deactivateValidation(meter);

        //Check that a MeterValidation object is made
        verify(meterValidation).setActivationStatus(false);
        verify(meterValidation).save();

    }

    @Test
    public void testDeactivateMeterValidationNoObject() {
        Meter meter = mock(Meter.class);
        when(meter.getId()).thenReturn(ID);
        when(meterValidationFactory.getOptional(ID)).thenReturn(Optional.<MeterValidationImpl>empty());

        validationService.deactivateValidation(meter);

        verify(meterValidation, never()).setActivationStatus(anyBoolean());
        verify(dataModel, never()).getInstance(MeterValidationImpl.class);

    }
}
