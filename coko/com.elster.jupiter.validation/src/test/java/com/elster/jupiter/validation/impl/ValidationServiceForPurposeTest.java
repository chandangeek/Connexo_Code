/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.kpi.KpiService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.validation.ValidationService;

import org.osgi.framework.BundleContext;

import java.time.Clock;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidationServiceForPurposeTest {

    private static final long ID = 1L;
    private ValidationService validationService;
    private PurposeValidationImpl purposeValidation;

    @Mock
    private OrmService ormService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Table table;
    @Mock
    private DataModel dataModel;
    @Mock
    private DataMapper<PurposeValidationImpl> purposeValidationFactory;
    @Mock
    private ChannelsContainer channelsContainer;
    @Mock
    private javax.validation.ValidatorFactory validatorFactory;
    @Mock
    private javax.validation.Validator javaxValidator;

    @Before
    public void setUp() {

        when(ormService.newDataModel(anyString(), anyString())).thenReturn(dataModel);
        when(dataModel.addTable(anyString(), any())).thenReturn(table);
        when(dataModel.mapper(PurposeValidationImpl.class)).thenReturn(purposeValidationFactory);
        when(dataModel.getValidatorFactory()).thenReturn(validatorFactory);
        when(dataModel.getValidatorFactory().getValidator()).thenReturn(javaxValidator);
        when(channelsContainer.getId()).thenReturn(ID);

        validationService = new ValidationServiceImpl(
                mock(BundleContext.class),
                mock(Clock.class),
                mock(MessageService.class),
                mock(EventService.class),
                mock(TaskService.class),
                mock(MeteringService.class),
                mock(MeteringGroupsService.class),
                ormService,
                mock(QueryService.class),
                mock(NlsService.class),
                mock(UserService.class),
                mock(Publisher.class),
                mock(UpgradeService.class),
                mock(KpiService.class),
                mock(MetrologyConfigurationService.class),
                mock(SearchService.class));

        purposeValidation = new PurposeValidationImpl(dataModel);
        when(purposeValidationFactory.getOptional(ID)).thenReturn(Optional.of(purposeValidation));
    }

    @Test(expected = IllegalArgumentException.class)
    public void isValidationActive_whenContractIsNull_thenThrowException(){
        validationService.isValidationActive((ChannelsContainer) null);
    }

    @Test
    public void whenValidationIsNotPresent_thenReturnInactive(){
        when(purposeValidationFactory.getOptional(ID)).thenReturn(Optional.empty());

        boolean active = validationService.isValidationActive(channelsContainer);

        assertFalse(active);
    }

    @Test
    public void whenValidationIsPresent_thenReturnActiveStatus() {
        purposeValidation.setActivationStatus(true);

        boolean active = validationService.isValidationActive(channelsContainer);

        assertTrue(active);
    }


    @Test(expected = IllegalArgumentException.class)
    public void activateValidation_whenContractIsNull_thenThrowException(){
        validationService.activateValidation((ChannelsContainer) null);
    }

    @Test
    public void whenValidationIsNotPresent_thenCreateNewAndActivate() {
        when(purposeValidationFactory.getOptional(ID)).thenReturn(Optional.empty());

        validationService.activateValidation(channelsContainer);

        verify(dataModel).persist(any(PurposeValidationImpl.class));
    }

    @Test
    public void whenStatusIsInactive_thenActivate() {
        purposeValidation.setActivationStatus(false);

        validationService.activateValidation(channelsContainer);

        assertTrue(purposeValidation.getActivationStatus());
        verify(dataModel).update(purposeValidation);
    }

    @Test
    public void whenStatusIsActive_thenSkipActivate() {
        purposeValidation.setActivationStatus(true);

        validationService.activateValidation(channelsContainer);

        assertTrue(purposeValidation.getActivationStatus());
        verify(dataModel, times(0)).update(purposeValidation);

    }

    @Test(expected = IllegalArgumentException.class)
    public void deactivateValidation_whenContractIsNull_thenThrowException(){
        validationService.deactivateValidation((ChannelsContainer) null);
    }

    @Test
    public void whenStatusIsInactive_thenSkipDeactivate() {
        purposeValidation.setActivationStatus(false);

        validationService.deactivateValidation(channelsContainer);

        assertFalse(purposeValidation.getActivationStatus());
        verify(dataModel, times(0)).update(purposeValidation);
    }

    @Test
    public void whenStatusIsActive_thenDeactivate() {
        purposeValidation.setActivationStatus(true);

        validationService.deactivateValidation(channelsContainer);

        assertFalse(purposeValidation.getActivationStatus());
        verify(dataModel).update(purposeValidation);
    }

    @Test
    public void whenValidationIsNotPresent_thenSkipDeactivate() {
        when(purposeValidationFactory.getOptional(ID)).thenReturn(Optional.empty());

        validationService.deactivateValidation(channelsContainer);

        verify(dataModel, times(0)).persist(any(PurposeValidationImpl.class));
        verify(dataModel, times(0)).update(any(PurposeValidationImpl.class));
    }
}
