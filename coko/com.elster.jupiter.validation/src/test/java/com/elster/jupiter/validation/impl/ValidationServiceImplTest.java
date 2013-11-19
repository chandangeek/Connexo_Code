package com.elster.jupiter.validation.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.cache.ComponentCache;
import com.elster.jupiter.orm.cache.TypeCache;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.Validator;
import com.elster.jupiter.validation.ValidatorFactory;
import com.elster.jupiter.validation.ValidatorNotFoundException;
import com.google.common.base.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidationServiceImplTest {

    private static final String NAME = "name";
    private static final long ID = 561651L;
    private ValidationServiceImpl validationService;

    @Mock
    private ServiceLocator serviceLocator;
    @Mock
    private ComponentCache componentCache;
    @Mock
    private EventService eventService;
    @Mock
    private OrmClient ormClient;
    @Mock
    private ValidatorFactory factory;
    @Mock
    private Validator validator;
    @Mock
    private TypeCache<ValidationRuleSet> validationRuleSetFactory;
    @Mock
    private TypeCache<ValidationRule> validationRuleFactory;
    @Mock
    private MeterActivation meterActivation;
    @Mock
    private DataMapper<MeterActivationValidation> meterActivationValidationFactory;
    @Mock
    private Channel channel1, channel2;
    @Mock
    private DataMapper<ChannelValidation> channelValidationFactory;

    @Before
    public void setUp() {
        validationService = new ValidationServiceImpl();
        when(serviceLocator.getComponentCache()).thenReturn(componentCache);
        when(serviceLocator.getEventService()).thenReturn(eventService);
        when(serviceLocator.getValidationService()).thenReturn(validationService);
        when(serviceLocator.getOrmClient()).thenReturn(ormClient);
        when(factory.available()).thenReturn(Arrays.asList(validator.getClass().getName()));
        when(factory.create(validator.getClass().getName())).thenReturn(validator);
        when(ormClient.getValidationRuleSetFactory()).thenReturn(validationRuleSetFactory);
        when(ormClient.getValidationRuleFactory()).thenReturn(validationRuleFactory);
        when(ormClient.getMeterActivationValidationFactory()).thenReturn(meterActivationValidationFactory);
        when(ormClient.getChannelValidationFactory()).thenReturn(channelValidationFactory);

        Bus.setServiceLocator(serviceLocator);
    }

    @After
    public void tearDown() {
        Bus.clearServiceLocator(serviceLocator);
    }

    @Test
    public void testGetImplementation() {
        validationService.addResource(factory);

        Validator found = validationService.getValidator(validator.getClass().getName());

        assertThat(found).isNotNull().isEqualTo(validator);
    }

    @Test(expected = ValidatorNotFoundException.class)
    public void testGetValidatorThrowsNotFoundExceptionIfNoFactoryProvidesImplementation() {
        validationService.getValidator(validator.getClass().getName());
    }

    @Test
    public void testCreateRuleSet() {
        ValidationRuleSet validationRuleSet = validationService.createValidationRuleSet(NAME);

        assertThat(validationRuleSet.getName()).isEqualTo(NAME);
    }

    @Test
    public void testApplyRuleSet() {
        when(meterActivation.getId()).thenReturn(ID);
        when(meterActivationValidationFactory.get(ID)).thenReturn(Optional.<MeterActivationValidation>absent());

        ValidationRuleSet validationRuleSet = validationService.createValidationRuleSet(NAME);
        validationRuleSet.save();
        validationService.applyRuleSet(validationRuleSet, meterActivation);

        ArgumentCaptor<MeterActivationValidation> meterActivationValidationArgumentCaptor = ArgumentCaptor.forClass(MeterActivationValidation.class);
        verify(meterActivationValidationFactory).persist(meterActivationValidationArgumentCaptor.capture());

        MeterActivationValidation meterActivationValidation = meterActivationValidationArgumentCaptor.getValue();
        assertThat(meterActivationValidation.getId()).isEqualTo(ID);
        assertThat(meterActivationValidation.getRuleSet()).isEqualTo(validationRuleSet);
    }

    @Test
    public void testApplyRuleSetOnExisting() {
        MeterActivationValidation meterActivationValidation = mock(MeterActivationValidationImpl.class);
        when(meterActivationValidationFactory.get(ID)).thenReturn(Optional.of(meterActivationValidation));
        when(meterActivation.getId()).thenReturn(ID);

        ValidationRuleSet validationRuleSet = validationService.createValidationRuleSet(NAME);
        validationRuleSet.save();
        validationService.applyRuleSet(validationRuleSet, meterActivation);

        ArgumentCaptor<MeterActivationValidation> meterActivationValidationArgumentCaptor = ArgumentCaptor.forClass(MeterActivationValidation.class);
        verify(meterActivationValidation).setRuleSet(validationRuleSet);
        verify(meterActivationValidation).save();
    }

    @Test
    public void testApplyRuleSetWithChannels() {
        when(meterActivationValidationFactory.get(ID)).thenReturn(Optional.<MeterActivationValidation>absent());
        when(meterActivation.getId()).thenReturn(ID);
        when(meterActivation.getChannels()).thenReturn(Arrays.asList(channel1, channel2));
        when(channel1.getId()).thenReturn(1001L);
        when(channel2.getId()).thenReturn(1002L);
        when(channel1.getMeterActivation()).thenReturn(meterActivation);
        when(channel2.getMeterActivation()).thenReturn(meterActivation);

        ValidationRuleSet validationRuleSet = validationService.createValidationRuleSet(NAME);
        validationRuleSet.save();
        validationService.applyRuleSet(validationRuleSet, meterActivation);

        ArgumentCaptor<List> channelValidationArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(channelValidationFactory).persist(channelValidationArgumentCaptor.capture());

        List<ChannelValidation> channelValidations = channelValidationArgumentCaptor.getValue();
        ChannelValidation channelValidation = channelValidations.get(0);
        assertThat(channelValidation.getId()).isEqualTo(1001L);
        assertThat(channelValidation.getMeterActivationValidation().getId()).isEqualTo(ID);

        channelValidation = channelValidations.get(1);
        assertThat(channelValidation.getId()).isEqualTo(1002L);
        assertThat(channelValidation.getMeterActivationValidation().getId()).isEqualTo(ID);

    }
}
