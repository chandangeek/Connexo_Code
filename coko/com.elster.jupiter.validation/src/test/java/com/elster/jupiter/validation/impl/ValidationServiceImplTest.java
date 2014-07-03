package com.elster.jupiter.validation.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
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
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetResolver;
import com.elster.jupiter.validation.Validator;
import com.elster.jupiter.validation.ValidatorFactory;
import com.elster.jupiter.validation.ValidatorNotFoundException;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
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
    private MeterActivation meterActivation;
    @Mock
    private DataMapper<MeterActivationValidation> meterActivationValidationFactory;
    @Mock
    private Channel channel1, channel2;
    @Mock
    private DataMapper<ChannelValidation> channelValidationFactory;
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
    private QueryExecutor<MeterActivationValidation> queryExecutor;

    @Before
    public void setUp() {
        when(ormService.newDataModel(anyString(), anyString())).thenReturn(dataModel);
        when(dataModel.addTable(anyString(), any(Class.class))).thenReturn(table);
        when(dataModel.mapper(IValidationRuleSet.class)).thenReturn(validationRuleSetFactory);
        when(dataModel.mapper(IValidationRule.class)).thenReturn(validationRuleFactory);
        when(dataModel.mapper(MeterActivationValidation.class)).thenReturn(meterActivationValidationFactory);
        when(dataModel.mapper(ChannelValidation.class)).thenReturn(channelValidationFactory);
        when(nlsService.getThesaurus(anyString(), any(Layer.class))).thenReturn(thesaurus);
        when(dataModel.query(IValidationRule.class)).thenReturn(validationRuleQueryExecutor);

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
        when(dataModel.query(MeterActivationValidation.class)).thenReturn(queryExecutor);
        when(queryExecutor.select(any(Condition.class))).thenReturn(Collections.<MeterActivationValidation>emptyList());
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

        ValidationRuleSet validationRuleSet = validationService.createValidationRuleSet(NAME);
        validationRuleSet.save();
        when(validationRuleSetResolver.resolve(eq(meterActivation), any(Interval.class))).thenReturn(Arrays.asList(validationRuleSet));
        validationService.validate(meterActivation, Interval.sinceEpoch());

        ArgumentCaptor<MeterActivationValidation> meterActivationValidationCapture = ArgumentCaptor.forClass(MeterActivationValidation.class);
        verify(meterActivationValidationFactory).persist(meterActivationValidationCapture.capture());

        final MeterActivationValidation meterActivationValidationCaptureValue = meterActivationValidationCapture.getValue();
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
}
