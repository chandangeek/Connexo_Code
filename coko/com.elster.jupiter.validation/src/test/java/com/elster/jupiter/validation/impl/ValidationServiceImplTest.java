package com.elster.jupiter.validation.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQuality;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.readings.BaseReading;
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
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.time.ProgrammableClock;
import com.elster.jupiter.validation.*;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import org.joda.time.DateTime;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
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
    private DataMapper<MeterValidation> meterValidationFactory;
    @Mock
    private MeterActivation meterActivation;
    @Mock
    private DataMapper<IMeterActivationValidation> meterActivationValidationFactory;
    @Mock
    private Channel channel1, channel2;
    @Mock
    private DataMapper<ChannelValidation> channelValidationFactory;
    @Mock
    private MeterValidation meterValidation;
    @Mock
    private DataModel dataModel;
    @Mock
    private OrmService ormService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Table table;
    private Clock clock = new ProgrammableClock();
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

    @Before
    public void setUp() {
        when(ormService.newDataModel(anyString(), anyString())).thenReturn(dataModel);
        when(dataModel.addTable(anyString(), any(Class.class))).thenReturn(table);
        when(dataModel.mapper(IValidationRuleSet.class)).thenReturn(validationRuleSetFactory);
        when(dataModel.mapper(IValidationRule.class)).thenReturn(validationRuleFactory);
        when(dataModel.mapper(IMeterActivationValidation.class)).thenReturn(meterActivationValidationFactory);
        when(dataModel.mapper(ChannelValidation.class)).thenReturn(channelValidationFactory);
        when(dataModel.mapper(MeterValidation.class)).thenReturn(meterValidationFactory);
        when(nlsService.getThesaurus(anyString(), any(Layer.class))).thenReturn(thesaurus);
        when(dataModel.query(IValidationRule.class, IValidationRuleSet.class, ValidationRuleProperties.class)).thenReturn(validationRuleQueryExecutor);

        validationService = new ValidationServiceImpl();
        validationService.setOrmService(ormService);
        validationService.setNlsService(nlsService);
        validationService.addValidationRuleSetResolver(validationRuleSetResolver);

        when(factory.available()).thenReturn(Arrays.asList(validator.getClass().getName()));
        when(factory.create(validator.getClass().getName(), null)).thenReturn(validator);
        when(dataModel.getInstance(ValidationRuleSetImpl.class)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new ValidationRuleSetImpl(dataModel, eventService);
            }
        });
        when(dataModel.getInstance(MeterActivationValidationImpl.class)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new MeterActivationValidationImpl(dataModel, clock);
            }
        });
        when(dataModel.getInstance(ChannelValidationImpl.class)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new ChannelValidationImpl();
            }
        });
        when(dataModel.query(IMeterActivationValidation.class)).thenReturn(queryExecutor);
        when(queryExecutor.select(any(Condition.class))).thenReturn(Collections.<IMeterActivationValidation>emptyList());
        when(thesaurus.getFormat(any(MessageSeed.class))).thenReturn(nlsMessageFormat);
        when(dataModel.getValidatorFactory()).thenReturn(validatorFactory);
        when(dataModel.getValidatorFactory().getValidator()).thenReturn(javaxValidator);
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
        when(meterActivation.getChannels()).thenReturn(Arrays.asList(channel1, channel2));
        when(channel1.getId()).thenReturn(1001L);
        when(channel2.getId()).thenReturn(1002L);
        when(channel1.getMeterActivation()).thenReturn(meterActivation);
        when(channel2.getMeterActivation()).thenReturn(meterActivation);
        when(meterValidationFactory.getOptional(ID)).thenReturn(Optional.of(meterValidation));
        when(meterValidation.getActivationStatus()).thenReturn(true);

        ValidationRuleSet validationRuleSet = validationService.createValidationRuleSet(NAME);
        validationRuleSet.save();
        when(validationRuleSetResolver.resolve(eq(meterActivation), any(Interval.class))).thenReturn(Arrays.asList(validationRuleSet));
        validationService.validate(meterActivation, Interval.sinceEpoch());

        ArgumentCaptor<IMeterActivationValidation> meterActivationValidationCapture = ArgumentCaptor.forClass(IMeterActivationValidation.class);
        verify(meterActivationValidationFactory).persist(meterActivationValidationCapture.capture());

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
        }).toSet()).contains(channel1, channel2);

    }

    @Test
    public void testManageValidationActivations() {
        when(meterActivation.getId()).thenReturn(ID);
        when(meterActivation.getChannels()).thenReturn(Arrays.asList(channel1, channel2));
        when(channel1.getId()).thenReturn(1001L);
        when(channel2.getId()).thenReturn(1002L);
        when(channel1.getMeterActivation()).thenReturn(meterActivation);
        when(channel2.getMeterActivation()).thenReturn(meterActivation);
        when(meterValidationFactory.getOptional(ID)).thenReturn(Optional.of(meterValidation));
        when(meterValidation.getActivationStatus()).thenReturn(true);

        ValidationRuleSet validationRuleSet = validationService.createValidationRuleSet(NAME);
        validationRuleSet.save();

        when(validationRuleSetResolver.resolve(eq(meterActivation), any(Interval.class))).thenReturn(Arrays.asList(validationRuleSet));
        validationService.validate(meterActivation, Interval.sinceEpoch());

        List<MeterActivationValidation> meterActivationValidations = validationService.getMeterActivationValidations(meterActivation, Interval.sinceEpoch());
        assertThat(meterActivationValidations).hasSize(1);
        assertThat(meterActivationValidations.get(0).getMeterActivation()).isEqualTo(meterActivation);
        assertThat(meterActivationValidations.get(0).getRuleSet()).isEqualTo(validationRuleSet);
        MeterActivationValidation activationRuleSet1 = meterActivationValidations.get(0);

        ValidationRuleSet validationRuleSet2 = validationService.createValidationRuleSet(NAME);
        validationRuleSet2.save();

        when(validationRuleSetResolver.resolve(eq(meterActivation), any(Interval.class))).thenReturn(Arrays.asList(validationRuleSet, validationRuleSet2));
        validationService.validate(meterActivation, Interval.sinceEpoch());
        meterActivationValidations = validationService.getMeterActivationValidations(meterActivation, Interval.sinceEpoch());
        assertThat(meterActivationValidations).hasSize(2);
        assertThat(meterActivationValidations.get(0).getMeterActivation()).isEqualTo(meterActivation);
        assertThat(meterActivationValidations.get(1).getMeterActivation()).isEqualTo(meterActivation);
        assertThat(FluentIterable.from(meterActivationValidations).transform(new Function<MeterActivationValidation, ValidationRuleSet>() {
            @Override
            public ValidationRuleSet apply(MeterActivationValidation input) {
                return input.getRuleSet();
            }
        }).toSet()).contains(validationRuleSet, validationRuleSet2);

        when(validationRuleSetResolver.resolve(eq(meterActivation), any(Interval.class))).thenReturn(Arrays.asList(validationRuleSet2));
        validationService.validate(meterActivation, Interval.sinceEpoch());
        meterActivationValidations = validationService.getMeterActivationValidations(meterActivation, Interval.sinceEpoch());
        assertThat(meterActivationValidations).hasSize(1);
        assertThat(meterActivationValidations.get(0).getMeterActivation()).isEqualTo(meterActivation);
        assertThat(meterActivationValidations.get(0).getRuleSet()).isEqualTo(validationRuleSet2);
        assertThat(activationRuleSet1.isObsolete());
    }

    @Test
    public void testGetValidationStatusOnEmptyList() {
        assertThat(validationService.getValidationStatus(channel1, Collections.<BaseReading>emptyList())).isEmpty();
    }

    @Test
    public void testGetValidationStatusOnNonValidated() {
        Date readingDate = new Date();
        BaseReading reading = mock(BaseReading.class);
        when(reading.getTimeStamp()).thenReturn(readingDate);

        when(channelValidationFactory.find(eq("channel"), eq(channel1))).thenReturn(Collections.<ChannelValidation>emptyList());

        List<List<ReadingQuality>> validationStatus = validationService.getValidationStatus(channel1, Arrays.asList(reading));
        assertThat(validationStatus).hasSize(1);
        assertThat(validationStatus.get(0)).isEmpty();

        ChannelValidation channelValidation = mock(ChannelValidation.class);
        when(channelValidationFactory.find(eq("channel"), eq(channel1))).thenReturn(Arrays.asList(channelValidation));
        when(channelValidation.getLastChecked()).thenReturn(new Date(0));

        validationStatus = validationService.getValidationStatus(channel1, Arrays.asList(reading));
        assertThat(validationStatus).hasSize(1);
        assertThat(validationStatus.get(0)).isEmpty();

        ReadingQuality readingQuality = mock(ReadingQuality.class);
        when(readingQuality.getReadingTimestamp()).thenReturn(readingDate);
        when(channel1.findReadingQuality(any(Interval.class))).thenReturn(Arrays.asList(readingQuality));
        validationStatus = validationService.getValidationStatus(channel1, Arrays.asList(reading));
        assertThat(validationStatus).hasSize(1);
        assertThat(validationStatus.get(0)).isEmpty();
    }

    @Test
    public void testGetValidationStatus() {
        Date readingDate1 = new DateTime(2012, 1, 2, 3, 0, 0, 0).toDate();
        Date readingDate2 = new DateTime(2012, 1, 2, 5, 0, 0, 0).toDate();
        BaseReading reading1 = mock(BaseReading.class);
        when(reading1.getTimeStamp()).thenReturn(readingDate1);
        BaseReading reading2 = mock(BaseReading.class);
        when(reading2.getTimeStamp()).thenReturn(readingDate2);


        ChannelValidation channelValidation = mock(ChannelValidation.class);
        when(channelValidationFactory.find(eq("channel"), eq(channel1))).thenReturn(Arrays.asList(channelValidation));
        when(channelValidation.getLastChecked()).thenReturn(readingDate1);
        when(channel1.findReadingQuality(eq(new Interval(readingDate1, readingDate2)))).thenReturn(Collections.<ReadingQuality>emptyList());
        when(channel1.createReadingQuality(any(ReadingQualityType.class), eq(readingDate1))).thenReturn(mock(ReadingQuality.class));

// !! remark that the order of the reading is by purpose not chronological !!
        List<List<ReadingQuality>> validationStatus = validationService.getValidationStatus(channel1, Arrays.asList(reading2, reading1));
        assertThat(validationStatus).hasSize(2);
        assertThat(validationStatus.get(0)).isEmpty(); // reading2 has not be validated yet
        assertThat(validationStatus.get(1)).hasSize(1); // reading1 is ok
        ArgumentCaptor<ReadingQualityType> readingQualitypCapture = ArgumentCaptor.forClass(ReadingQualityType.class);
        verify(channel1).createReadingQuality(readingQualitypCapture.capture(), eq(readingDate1));
        assertThat(readingQualitypCapture.getValue().getCode()).isEqualTo(ReadingQualityType.MDM_VALIDATED_OK_CODE);
    }

    @Test
    public void testGetValidationStatusMultipleChannelValidations() {
        Date readingDate1 = new DateTime(2012, 1, 2, 3, 0, 0, 0).toDate();
        Date readingDate2 = new DateTime(2012, 1, 2, 5, 0, 0, 0).toDate();
        BaseReading reading1 = mock(BaseReading.class);
        when(reading1.getTimeStamp()).thenReturn(readingDate1);
        BaseReading reading2 = mock(BaseReading.class);
        when(reading2.getTimeStamp()).thenReturn(readingDate2);


        ChannelValidation channelValidation1 = mock(ChannelValidation.class);
        when(channelValidation1.getLastChecked()).thenReturn(readingDate1);
        ChannelValidation channelValidation2 = mock(ChannelValidation.class);
        when(channelValidation2.getLastChecked()).thenReturn(readingDate2);
        when(channelValidationFactory.find(eq("channel"), eq(channel1))).thenReturn(Arrays.asList(channelValidation1, channelValidation2));
        when(channel1.findReadingQuality(eq(new Interval(readingDate1, readingDate2)))).thenReturn(Collections.<ReadingQuality>emptyList());
        when(channel1.createReadingQuality(any(ReadingQualityType.class), eq(readingDate1))).thenReturn(mock(ReadingQuality.class));

// !! remark that the order of the reading is by purpose not chronological !!
        List<List<ReadingQuality>> validationStatus = validationService.getValidationStatus(channel1, Arrays.asList(reading2, reading1));
        assertThat(validationStatus).hasSize(2);
        assertThat(validationStatus.get(0)).isEmpty(); // reading2 has not be validated yet
        assertThat(validationStatus.get(1)).hasSize(1); // reading1 is ok
        ArgumentCaptor<ReadingQualityType> readingQualitypCapture = ArgumentCaptor.forClass(ReadingQualityType.class);
        verify(channel1).createReadingQuality(readingQualitypCapture.capture(), eq(readingDate1));
        assertThat(readingQualitypCapture.getValue().getCode()).isEqualTo(ReadingQualityType.MDM_VALIDATED_OK_CODE);
    }

    @Test
    public void testGetValidationStatusNewChannelValidations() {
        Date readingDate1 = new DateTime(2012, 1, 2, 3, 0, 0, 0).toDate();
        Date readingDate2 = new DateTime(2012, 1, 2, 5, 0, 0, 0).toDate();
        BaseReading reading1 = mock(BaseReading.class);
        when(reading1.getTimeStamp()).thenReturn(readingDate1);
        BaseReading reading2 = mock(BaseReading.class);
        when(reading2.getTimeStamp()).thenReturn(readingDate2);


        ChannelValidation channelValidation1 = mock(ChannelValidation.class);
        when(channelValidation1.getLastChecked()).thenReturn(readingDate1);
        ChannelValidation channelValidation2 = mock(ChannelValidation.class);
        when(channelValidation2.getLastChecked()).thenReturn(null);
        when(channelValidationFactory.find(eq("channel"), eq(channel1))).thenReturn(Arrays.asList(channelValidation1, channelValidation2));
        ReadingQuality readingQuality = mock(ReadingQuality.class);
        when(readingQuality.getReadingTimestamp()).thenReturn(readingDate1);
        when(channel1.findReadingQuality(eq(new Interval(readingDate1, readingDate2)))).thenReturn(Arrays.asList(readingQuality));
        when(channel1.createReadingQuality(any(ReadingQualityType.class), eq(readingDate1))).thenReturn(mock(ReadingQuality.class));

// !! remark that the order of the reading is by purpose not chronological !!
        List<List<ReadingQuality>> validationStatus = validationService.getValidationStatus(channel1, Arrays.asList(reading2, reading1));
        assertThat(validationStatus).hasSize(2);
        assertThat(validationStatus.get(0)).isEmpty(); // reading2 has not be validated yet
        assertThat(validationStatus.get(1)).isEmpty(); // reading1 has not be validated yet for rule set 2

    }

    @Test
    public void testGetValidationStatusWithSuspects() {
        Date readingDate1 = new DateTime(2012, 1, 2, 3, 0, 0, 0).toDate();
        Date readingDate2 = new DateTime(2012, 1, 2, 5, 0, 0, 0).toDate();
        BaseReading reading1 = mock(BaseReading.class);
        when(reading1.getTimeStamp()).thenReturn(readingDate1);
        BaseReading reading2 = mock(BaseReading.class);
        when(reading2.getTimeStamp()).thenReturn(readingDate2);


        ChannelValidation channelValidation1 = mock(ChannelValidation.class);
        when(channelValidation1.getLastChecked()).thenReturn(readingDate2);
        when(channelValidationFactory.find(eq("channel"), eq(channel1))).thenReturn(Arrays.asList(channelValidation1));
        ReadingQuality readingQuality1 = mock(ReadingQuality.class);
        when(readingQuality1.getReadingTimestamp()).thenReturn(readingDate1);
        ReadingQuality readingQuality2 = mock(ReadingQuality.class);
        when(readingQuality2.getReadingTimestamp()).thenReturn(readingDate1);
        when(channel1.findReadingQuality(eq(new Interval(readingDate1, readingDate2)))).thenReturn(Arrays.asList(readingQuality1, readingQuality2));
        when(channel1.createReadingQuality(any(ReadingQualityType.class), eq(readingDate2))).thenReturn(mock(ReadingQuality.class));

// !! remark that the order of the reading is by purpose not chronological !!
        List<List<ReadingQuality>> validationStatus = validationService.getValidationStatus(channel1, Arrays.asList(reading2, reading1));
        assertThat(validationStatus).hasSize(2);
        assertThat(validationStatus.get(0)).hasSize(1); // reading2 is OK
        ArgumentCaptor<ReadingQualityType> readingQualitypCapture = ArgumentCaptor.forClass(ReadingQualityType.class);
        verify(channel1).createReadingQuality(readingQualitypCapture.capture(), eq(readingDate2));
        assertThat(readingQualitypCapture.getValue().getCode()).isEqualTo(ReadingQualityType.MDM_VALIDATED_OK_CODE);
        assertThat(validationStatus.get(1)).hasSize(2); // reading1 has suspects
        assertThat(validationStatus.get(1)).containsExactly(readingQuality1, readingQuality2);

    }

    @Test
    public void testMeterValidation() {
        when(meterActivation.getId()).thenReturn(ID);
        when(dataModel.mapper(MeterValidation.class).getOptional(meterActivation.getId())).thenReturn(Optional.of(meterValidation));
        when(dataModel.getInstance(MeterValidationImpl.class)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new MeterValidationImpl(dataModel);
            }
        });
        when(MeterValidationImpl.from(dataModel, meterActivation)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new MeterValidationImpl(dataModel);
            }
        });
        when(meterValidation.getActivationStatus()).thenReturn(true);

        MeterValidation newMeterValidation = validationService.createMeterValidation(meterActivation);
        assertThat(newMeterValidation).isNotNull();
        assertThat(validationService.getMeterValidation(meterActivation).get().getActivationStatus()).isEqualTo(true);
        validationService.setMeterValidationStatus(meterActivation, false);
        verify(meterValidation).setActivationStatus(false);

    }
}
